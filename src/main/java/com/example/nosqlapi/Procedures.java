package com.example.nosqlapi;

import org.neo4j.driver.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class Procedures {
    private static Driver driver = null;

    public Procedures(Driver driver) {
        this.driver = driver;
    }

    public String createRequestAndRelationships(String requestId, String supplierId, String productId, ZonedDateTime requestDate, long quantity) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MERGE (r:Request {id: $requestId})
                    SET r.requestDate = $requestDate, r.quantity = $quantity
                    WITH r
                    OPTIONAL MATCH (s:Supplier {id: $supplierId})
                    OPTIONAL MATCH (p:Product {id: $productId})
                    FOREACH (_ IN CASE WHEN s IS NOT NULL THEN [1] ELSE [] END | MERGE (r)-[:REQUESTED_FROM]->(s))
                    FOREACH (_ IN CASE WHEN p IS NOT NULL THEN [1] ELSE [] END | MERGE (r)-[:CONTAINS]->(p))
                    """,
                        Values.parameters(
                                "requestId", requestId,
                                "requestDate", requestDate.toString(),
                                "quantity", quantity,
                                "supplierId", supplierId,
                                "productId", productId
                        ));
                return null;
            });
        }
        return "Request and relationships created";
    }

    public String createOrUpdateEmployee(String id, String fullName, String employeeType) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MERGE (e:Employee {id: $id})
                    SET e.fullName = $fullName, e.employeeType = $employeeType
                    """,
                        Values.parameters("id", id, "fullName", fullName, "employeeType", employeeType));
                return null;
            });
        }
        return "Employee created/updated";
    }

    public static String createPlacedOrderRelation(String employeeId, String orderNumber) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MATCH (e:Employee {id: $employeeId}), (o:Order {orderNumber: $orderNumber})
                    MERGE (e)-[:PLACED]->(o)
                    """,
                        Values.parameters("employeeId", employeeId, "orderNumber", orderNumber));
                return null;
            });
        }
        return "PLACED relation created or already exists";
    }

    public static String createWorksAtRelation(String employeeId, String tradingOrgId, String shiftDate) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MATCH (e:Employee {id: $employeeId}), (t:TradingOrganization {id: $tradingOrgId})
                    MERGE (e)-[r:WORKS_AT {shift_date: $shiftDate}]->(t)
                    """,
                        Values.parameters("employeeId", employeeId, "tradingOrgId", tradingOrgId, "shiftDate", shiftDate));
                return null;
            });
        }
        return "WORKS_AT relation created";
    }

    public static String createMadePaymentRelation(UUID tradingOrganizationId, String paymentId, long amount, String paymentType) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                MERGE (p:Payment {id: $paymentId})
                ON CREATE SET p.amount = $amount, p.payment_type = $paymentType
                MERGE (t:TradingOrganization {id: $tradingOrgId})
                MERGE (t)-[r:MADE_PAYMENT]->(p)
                SET r.amount = $amount, r.payment_type = $paymentType
                """,
                        Values.parameters(
                                "tradingOrgId", tradingOrganizationId.toString(),
                                "paymentId", paymentId,
                                "amount", amount,
                                "paymentType", paymentType));
                return null;
            });
        }
        return "MADE_PAYMENT relation created";
    }
    public static void createRequestRelation(UUID requestId, UUID orderId, Instant requestDate, int quantity) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                MERGE (r:Request {request_id: $requestId})
                ON CREATE SET  r.quantity = $quantity
                MERGE (o:Order {order_id: $orderId})
                MERGE (r)-[:RELATED_TO_ORDER]->(o)
                """,
                        Values.parameters(
                                "requestId", requestId.toString(),
                                "orderId", orderId.toString(),
                                "requestDate", requestDate,
                                "quantity", quantity));
                return null;
            });
        }
    }

    public String createOrUpdateTradingOrg(String id, String name, String type) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MERGE (t:TradingOrganization {id: $id})
                    SET t.name = $name, t.type = $type
                    """,
                        Values.parameters("id", id, "name", name, "type", type));
                return null;
            });
        }
        return "TradingOrganization created/updated";
    }

    public String createOfferRelation(String supplierId, String productId, double price) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MERGE (s:Supplier {id: $supplierId})
                    MERGE (p:Product {id: $productId})
                    MERGE (s)-[r:OFFERS]->(p)
                    SET r.price = $price
                    """,
                        Values.parameters("supplierId", supplierId, "productId", productId, "price", price));
                return null;
            });
        }
        return "OFFERS relation created or updated";
    }

    public String createOrder(String orderNumber) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("""
                    MERGE (o:Order {orderNumber: $orderNumber})
                    """,
                        Values.parameters("orderNumber", orderNumber));
                return null;
            });
        }
        return "Order created or already exists";
    }
}
