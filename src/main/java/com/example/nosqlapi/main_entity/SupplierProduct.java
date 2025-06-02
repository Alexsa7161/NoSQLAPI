package com.example.nosqlapi.main_entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("supplier_product")
public class SupplierProduct {
    @PrimaryKey
    @Getter @Setter
    private SupplierProductKey key;
    @Getter @Setter
    private double price;

    public SupplierProductKey getKey() {
        return key;
    }

    public void setKey(SupplierProductKey key) {
        this.key = key;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
