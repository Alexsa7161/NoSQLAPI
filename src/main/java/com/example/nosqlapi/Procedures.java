package com.example.nosqlapi;


import org.neo4j.procedure.*;
import org.neo4j.graphdb.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class Procedures {

    @Context
    public GraphDatabaseService db;

    public static class Result {
        public String message;
        public Result(String message) { this.message = message; }
    }

    @Procedure(name = "employee.createOrUpdate", mode = Mode.WRITE)
    @Description("Создать или обновить узел Employee")
    public Stream<Result> createOrUpdateEmployee(@Name("id") String id, @Name("fullName") String fullName, @Name("employeeType") String employeeType) {
        if (id == null || fullName == null || employeeType == null) {
            throw new IllegalArgumentException("Parameters id, fullName, and employeeType must not be null");
        }
        Node employee = db.findNode(Label.label("Employee"), "id", id);
        if (employee == null) {
            employee = db.createNode(Label.label("Employee"));
            employee.setProperty("id", id);
        }
        employee.setProperty("fullName", fullName);
        employee.setProperty("employeeType", employeeType);
        return Stream.of(new Result("Employee created/updated"));
    }

    @Procedure(name = "employee.createPlacedOrderRelation", mode = Mode.WRITE)
    @Description("Создать связь Employee-[:PLACED]->Order")
    public Stream<Result> createPlacedOrderRelation(@Name("employeeId") String employeeId, @Name("orderNumber") String orderNumber) {
        if (employeeId == null || orderNumber == null) {
            throw new IllegalArgumentException("Parameters employeeId and orderNumber must not be null");
        }
        Node employee = db.findNode(Label.label("Employee"), "id", employeeId);
        Node order = db.findNode(Label.label("Order"), "orderNumber", orderNumber);
        if (employee == null || order == null) {
            return Stream.of(new Result("Employee or Order not found"));
        }
        // Проверим, чтобы связь не дублировалась (опционально)
        boolean relationExists = false;
        for (Relationship rel : employee.getRelationships(Direction.OUTGOING, RelationshipType.withName("PLACED"))) {
            if (rel.getEndNode().equals(order)) {
                relationExists = true;
                break;
            }
        }
        if (!relationExists) {
            employee.createRelationshipTo(order, RelationshipType.withName("PLACED"));
        }
        return Stream.of(new Result("PLACED relation created or already exists"));
    }

    @Procedure(name = "employee.createWorksAtRelation", mode = Mode.WRITE)
    @Description("Создать связь Employee-[:WORKS_AT {shift_date}]->TradingOrganization")
    public Stream<Result> createWorksAtRelation(@Name("employeeId") String employeeId, @Name("tradingOrganizationId") String tradingOrganizationId, @Name("shiftDate") String shiftDate) {
        if (employeeId == null || tradingOrganizationId == null || shiftDate == null) {
            throw new IllegalArgumentException("Parameters employeeId, tradingOrganizationId, and shiftDate must not be null");
        }
        Node employee = db.findNode(Label.label("Employee"), "id", employeeId);
        Node tradingOrg = db.findNode(Label.label("TradingOrganization"), "id", tradingOrganizationId);
        if (employee == null || tradingOrg == null) {
            return Stream.of(new Result("Employee or TradingOrganization not found"));
        }
        // Проверяем, есть ли уже такая связь с таким shiftDate
        boolean relationExists = false;
        for (Relationship rel : employee.getRelationships(Direction.OUTGOING, RelationshipType.withName("WORKS_AT"))) {
            if (rel.getEndNode().equals(tradingOrg) && shiftDate.equals(rel.getProperty("shift_date", null))) {
                relationExists = true;
                break;
            }
        }
        if (!relationExists) {
            Relationship rel = employee.createRelationshipTo(tradingOrg, RelationshipType.withName("WORKS_AT"));
            rel.setProperty("shift_date", shiftDate);
        }
        return Stream.of(new Result("WORKS_AT relation created or already exists"));
    }

    @Procedure(name = "payment.createMadePaymentRelation", mode = Mode.WRITE)
    @Description("Создать связь TradingOrganization-[:MADE_PAYMENT]->Payment")
    public Stream<Result> createMadePaymentRelation(
            @Name("tradingOrganizationId") String tradingOrganizationId,
            @Name("paymentId") String paymentId,
            @Name("amount") double amount,                // изменить с BigDecimal на double
            @Name("paymentType") String paymentType) {

        Node tradingOrg = db.findNode(Label.label("TradingOrganization"), "id", tradingOrganizationId);
        Node payment = db.findNode(Label.label("Payment"), "id", paymentId);

        if (tradingOrg == null || payment == null) {
            return Stream.of(new Result("TradingOrganization or Payment not found"));
        }

        Relationship rel = tradingOrg.createRelationshipTo(payment, RelationshipType.withName("MADE_PAYMENT"));
        rel.setProperty("amount", amount);
        rel.setProperty("paymentType", paymentType);

        return Stream.of(new Result("MADE_PAYMENT relation created"));
    }


    @Procedure(name = "organization.createOrUpdate", mode = Mode.WRITE)
    @Description("Создать или обновить узел TradingOrganization")
    public Stream<Result> createOrUpdateTradingOrg(@Name("id") String id, @Name("name") String name, @Name("type") String type) {
        if (id == null || name == null || type == null) {
            throw new IllegalArgumentException("Parameters id, name, and type must not be null");
        }
        Node org = db.findNode(Label.label("TradingOrganization"), "id", id);
        if (org == null) {
            org = db.createNode(Label.label("TradingOrganization"));
            org.setProperty("id", id);
        }
        org.setProperty("name", name);
        org.setProperty("type", type);
        return Stream.of(new Result("TradingOrganization created/updated"));
    }

    @Procedure(name = "supplier.createOfferRelation", mode = Mode.WRITE)
    @Description("Создать узлы Supplier, Product и связь [:OFFERS {price}]")
    public Stream<Result> createOfferRelation(
            @Name("supplierId") String supplierId,
            @Name("productId") String productId,
            @Name("price") double price) {

        if (supplierId == null || productId == null) {
            throw new IllegalArgumentException("Parameters supplierId and productId must not be null");
        }

        Node supplier = db.findNode(Label.label("Supplier"), "id", supplierId);
        if (supplier == null) {
            supplier = db.createNode(Label.label("Supplier"));
            supplier.setProperty("id", supplierId);
        }

        Node product = db.findNode(Label.label("Product"), "id", productId);
        if (product == null) {
            product = db.createNode(Label.label("Product"));
            product.setProperty("id", productId);
        }

        // Проверяем, что связь OFFERS с таким price не дублируется (опционально)
        boolean relationExists = false;
        for (Relationship rel : supplier.getRelationships(Direction.OUTGOING, RelationshipType.withName("OFFERS"))) {
            if (rel.getEndNode().equals(product) && Double.compare(price, (double) rel.getProperty("price", 0.0)) == 0) {
                relationExists = true;
                break;
            }
        }

        if (!relationExists) {
            Relationship offers = supplier.createRelationshipTo(product, RelationshipType.withName("OFFERS"));
            offers.setProperty("price", price);
        }

        return Stream.of(new Result("OFFERS relation created or already exists"));
    }

    @Procedure(name = "order.create", mode = Mode.WRITE)
    @Description("Создать узел Order, если не существует")
    public Stream<Result> createOrder(@Name("orderNumber") String orderNumber) {
        if (orderNumber == null) {
            throw new IllegalArgumentException("Parameter orderNumber must not be null");
        }
        Node order = db.findNode(Label.label("Order"), "orderNumber", orderNumber);
        if (order == null) {
            order = db.createNode(Label.label("Order"));
            order.setProperty("orderNumber", orderNumber);
            return Stream.of(new Result("Order created"));
        }
        return Stream.of(new Result("Order already exists"));
    }

}

