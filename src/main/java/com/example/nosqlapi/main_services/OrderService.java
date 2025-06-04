package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Order;
import com.example.nosqlapi.main_entity.OrderEmployeeRequestDTO;
import com.example.nosqlapi.main_repositories.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.neo4j.driver.types.Node;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    public Order createOrder(Order order, UUID employee_id) {
        
        Order savedOrder = orderRepository.save(order);

        
        saveOrderParametersToMongo(savedOrder);

        
        createOrUpdateOrderInNeo4j(savedOrder);

        
        callNeo4jCreatePlacedOrderRelation(employee_id, savedOrder.getOrder_number().toString());

        return savedOrder;
    }

    public Order updateOrder(String order_number, Order order) {
        UUID orderId = UUID.fromString(order_number);

        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found");
        }

        order.setOrder_number(orderId);
        Order updatedOrder = orderRepository.save(order);

        saveOrderParametersToMongo(updatedOrder);
        createOrUpdateOrderInNeo4j(updatedOrder);

        return updatedOrder;
    }
    public void createOrUpdateOrderInNeo4j(Order savedOrder) {
        String query = """
        MERGE (o:Order {order_number: $order_number})
        SET o.employee_id = $employee_id
        RETURN o
    """;

        neo4jClient.query(query)
                .bind(savedOrder.getOrder_number().toString()).to("order_number")
                .bind(savedOrder.getEmployee_id().toString()).to("employee_id")
                .run();

        if (savedOrder.getEmployee_id() != null) {
            String relationQuery = """
            MATCH (e:Employee {employee_id: $employee_id}), (o:Order {order_number: $order_number})
            MERGE (e)-[:PLACED]->(o)
            RETURN e, o
        """;

            neo4jClient.query(relationQuery)
                    .bind(savedOrder.getEmployee_id().toString()).to("employee_id")
                    .bind(savedOrder.getOrder_number().toString()).to("order_number")
                    .run();
        }
    }

    public void deleteOrder(String order_number) {
        UUID orderId = UUID.fromString(order_number);
        orderRepository.deleteById(orderId);


        neo4jClient.query("""
        MATCH (o:Order {order_number: $order_number})
        DETACH DELETE o
        """)
                .bind(order_number).to("order_number")
                .run();
    }


    public Order getOrder(String order_number) {
        return orderRepository.findById(UUID.fromString(order_number)).orElse(null);
    }

    private void saveOrderParametersToMongo(Order order) {
        Document doc = new Document()
                .append("order_number", order.getOrder_number().toString());

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        mongoTemplate.getCollection("order_parameters")
                .replaceOne(Filters.eq("order_number", order.getOrder_number().toString()), doc, options);
    }


    private void callNeo4jCreatePlacedOrderRelation(UUID employee_id, String order_number) {
        com.example.nosqlapi.Procedures.createPlacedOrderRelation(String.valueOf(employee_id),order_number);
    }



    public OrderEmployeeRequestDTO getAggregatedOrderData(UUID orderNumber) throws JsonProcessingException {
        Order order = orderRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        UUID employeeId = order.getEmployee_id();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found in order");
        }

        
        Query query = new Query(Criteria.where("employee_id").is(employeeId.toString()));
        Document employeeDoc = mongoTemplate.findOne(query, Document.class, "employee_parameters");
        String fullName = employeeDoc != null ? (String) employeeDoc.get("full_name") : "Unknown";

        
        String neo4jQuery = """
            MATCH (o:Order {order_number: $order_number})-[:HAS_REQUEST]->(r:Request)
            RETURN r
        """;

        List<Map<String, Object>> requests = (List<Map<String, Object>>) neo4jClient.query(neo4jQuery)
                .bind(orderNumber.toString()).to("order_number")
                .fetch()
                .all();


        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String requestJson = null;

        if (!requests.isEmpty()) {
            Object rawRequestNode = requests.get(0).get("r");

            if (rawRequestNode instanceof Node requestNode) {
                Map<String, Object> requestProps = requestNode.asMap();
                requestJson = mapper.writeValueAsString(requestProps); 
            }
        }
        
        OrderEmployeeRequestDTO dto = new OrderEmployeeRequestDTO();
        dto.setOrderNumber(orderNumber);
        dto.setEmployeeFullName(fullName);
        dto.setRequestData(requestJson);

        return dto;
    }
}
