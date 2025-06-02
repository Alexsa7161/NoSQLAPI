package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Order;
import com.example.nosqlapi.main_repositories.OrderRepository;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;
    private final Neo4jClient neo4jClient;

    public OrderService(OrderRepository orderRepository,
                        MongoTemplate mongoTemplate,
                        Neo4jClient neo4jClient) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

    public Order createOrder(Order order, UUID employeeId) {
        // Сохраняем в Cassandra
        Order savedOrder = orderRepository.save(order);

        // Сохраняем параметры в MongoDB
        saveOrderParametersToMongo(savedOrder);

        // Создаём/обновляем узел Order в Neo4j
        createOrUpdateOrderInNeo4j(savedOrder);

        // Создаём связь (:Employee)-[:PLACED]->(:Order)
        callNeo4jCreatePlacedOrderRelation(employeeId, savedOrder.getOrder_number().toString());

        return savedOrder;
    }

    public Order updateOrder(String orderNumber, Order order) {
        UUID orderId = UUID.fromString(orderNumber);

        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found");
        }

        order.setOrder_number(orderId);
        Order updatedOrder = orderRepository.save(order);

        saveOrderParametersToMongo(updatedOrder);
        createOrUpdateOrderInNeo4j(updatedOrder);

        // Связи можно обновлять отдельно, если необходимо
        return updatedOrder;
    }

    public void deleteOrder(String orderNumber) {
        UUID orderId = UUID.fromString(orderNumber);

        orderRepository.deleteById(orderId);

        // Удаление параметров из MongoDB
        mongoTemplate.getCollection("order_parameters")
                .deleteOne(Filters.eq("order_number", orderId));

        neo4jClient.query("CALL order.delete($orderNumber)")
        .bind(orderNumber).to("orderNumber")
        .run();
    }

    public Order getOrder(String orderNumber) {
        return orderRepository.findById(UUID.fromString(orderNumber)).orElse(null);
    }

    private void saveOrderParametersToMongo(Order order) {
        Document doc = new Document()
                .append("order_number", order.getOrder_number().toString());

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        mongoTemplate.getCollection("order_parameters")
                .replaceOne(Filters.eq("order_number", order.getOrder_number().toString()), doc, options);
    }

    private void createOrUpdateOrderInNeo4j(Order order) {
        neo4jClient.query("CALL com.example.employee.EmployeeProcedures.createOrUpdate($orderNumber)")
                .bind(order.getOrder_number().toString()).to("order_number")
                .run();
    }

    private void callNeo4jCreatePlacedOrderRelation(UUID employeeId, String orderNumber) {
        neo4jClient.query("CALL com.example.employee.EmployeeProcedures.createPlacedOrderRelation($employeeId, $orderNumber)")
                .bind(employeeId.toString()).to("employeeId")
                .bind(orderNumber).to("orderNumber")
                .run();
    }
}
