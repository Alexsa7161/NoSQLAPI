package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Payment;
import com.example.nosqlapi.main_repositories.PaymentRepository;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;  // Cassandra
    private final MongoTemplate mongoTemplate;          // MongoDB
    private final Neo4jClient neo4jClient;               // Neo4j

    public PaymentService(PaymentRepository paymentRepository,
                          MongoTemplate mongoTemplate,
                          Neo4jClient neo4jClient) {
        this.paymentRepository = paymentRepository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

    public Payment createPayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);

        savePaymentParametersToMongo(savedPayment);

        callNeo4jProcedureForPayment(savedPayment);

        return savedPayment;
    }

    public Payment updatePayment(UUID id, Payment payment) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found");
        }
        payment.setId(id);
        Payment updatedPayment = paymentRepository.save(payment);

        savePaymentParametersToMongo(updatedPayment);

        callNeo4jProcedureForPayment(updatedPayment);

        return updatedPayment;
    }

    public void deletePayment(UUID id) {
        paymentRepository.deleteById(id);

        // Удаляем из MongoDB
        mongoTemplate.getCollection("payment_parameters")
                .deleteOne(Filters.eq("payment_id", id.toString()));

        neo4jClient.query("CALL payment.delete($paymentId)")
        .bind(id.toString()).to("paymentId")
        .run();
    }

    public Optional<Payment> getPayment(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private void savePaymentParametersToMongo(Payment payment) {
        Document doc = new Document()
                .append("payment_id", payment.getId().toString())
                .append("trading_organization_id", payment.getTrading_organization_id().toString())
                .append("payment_type", payment.getPayment_type())
                .append("amount", payment.getAmount());

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        mongoTemplate.getCollection("payment_parameters")
                .replaceOne(Filters.eq("payment_id", payment.getId().toString()), doc, options);
    }

    private void callNeo4jProcedureForPayment(Payment payment) {
        // Вызов Neo4j процедуры, создающей связь TradingOrganization -[:MADE_PAYMENT]-> Payment
        neo4jClient.query("CALL com.example.payment.createMadePaymentRelation($tradingOrganizationId, $paymentId, $amount, $paymentType)")
                .bind(payment.getTrading_organization_id().toString()).to("tradingOrganizationId")
                .bind(payment.getId().toString()).to("paymentId")
                .bind(payment.getAmount()).to("amount")
                .bind(payment.getPayment_type()).to("paymentType")
                .run();
    }
}
