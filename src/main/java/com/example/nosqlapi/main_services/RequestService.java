package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Request;
import com.example.nosqlapi.main_repositories.RequestRepository;
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
public class RequestService {

    private final RequestRepository requestRepository;  // Cassandra
    private final MongoTemplate mongoTemplate;          // MongoDB
    private final Neo4jClient neo4jClient;               // Neo4j

    public RequestService(RequestRepository requestRepository,
                          MongoTemplate mongoTemplate,
                          Neo4jClient neo4jClient) {
        this.requestRepository = requestRepository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

    public Request createRequest(Request request) {
        Request savedRequest = requestRepository.save(request);

        saveRequestParametersToMongo(savedRequest);

        callNeo4jProcedureForRequest(savedRequest);

        return savedRequest;
    }

    public Request updateRequest(UUID id, Request request) {
        if (!requestRepository.existsById(id)) {
            throw new RuntimeException("Request not found");
        }
        request.setRequest_id(id);
        Request updatedRequest = requestRepository.save(request);

        saveRequestParametersToMongo(updatedRequest);

        callNeo4jProcedureForRequest(updatedRequest);

        return updatedRequest;
    }

    public void deleteRequest(UUID id) {
        requestRepository.deleteById(id);


        mongoTemplate.getCollection("request_parameters")
                .deleteOne(Filters.eq("request_id", id.toString()));

        neo4jClient.query("CALL com.example.request.deleteRequest($requestId)")
        .bind(id.toString()).to("requestId")
        .run();
    }

    public Optional<Request> getRequest(UUID id) {
        return requestRepository.findById(id);
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    private void saveRequestParametersToMongo(Request request) {
        Document doc = new Document()
                .append("request_id", request.getRequest_id().toString())
                .append("supplier_id", request.getSupplier_id().toString())
                .append("product_id", request.getProduct_id().toString())
                .append("request_date", request.getRequest_date())
                .append("quantity", request.getQuantity());

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        mongoTemplate.getCollection("request_parameters")
                .replaceOne(Filters.eq("request_id", request.getRequest_id().toString()), doc, options);
    }

    private void callNeo4jProcedureForRequest(Request request) {
        neo4jClient.query("CALL com.example.request.createRequestRelationships($requestId, $supplierId, $productId, $requestDate, $quantity)")
                .bind(request.getRequest_id().toString()).to("requestId")
                .bind(request.getSupplier_id().toString()).to("supplierId")
                .bind(request.getProduct_id().toString()).to("productId")
                .bind(request.getRequest_date()).to("requestDate")
                .bind(request.getQuantity()).to("quantity")
                .run();
    }
}
