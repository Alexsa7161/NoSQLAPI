package com.example.nosqlapi.main_entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("request")
public class Request {

    @PrimaryKey
    private UUID request_id;

    public UUID getRequest_id() {
        return request_id;
    }

    public void setRequest_id(UUID request_id) {
        this.request_id = request_id;
    }

    @Getter @Setter
    private UUID supplier_id;

    public UUID getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(UUID supplier_id) {
        this.supplier_id = supplier_id;
    }

    @Getter @Setter
    private UUID product_id;

    public UUID getProduct_id() {
        return product_id;
    }

    public void setProduct_id(UUID product_id) {
        this.product_id = product_id;
    }

    @Getter @Setter
    private Instant request_date;

    public Instant getRequest_date() {
        return request_date;
    }

    public void setRequest_date(Instant request_date) {
        this.request_date = request_date;
    }

    @Getter @Setter
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
