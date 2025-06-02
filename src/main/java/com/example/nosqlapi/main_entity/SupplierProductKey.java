package com.example.nosqlapi.main_entity;

import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

@PrimaryKeyClass
public class SupplierProductKey implements Serializable {
    @Setter
    @Getter
    @PrimaryKeyColumn(name = "supplier_id", type = PARTITIONED)
    private UUID supplier_id;

    public UUID getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(UUID supplier_id) {
        this.supplier_id = supplier_id;
    }

    @PrimaryKeyColumn(name = "product_id", type = PARTITIONED)
    private UUID product_id;

    public UUID getProduct_id() {
        return product_id;
    }

    public void setProduct_id(UUID product_id) {
        this.product_id = product_id;
    }
}