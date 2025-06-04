package com.example.nosqlapi.main_entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;

@Table("supplier_product")
public class SupplierProduct {
    @PrimaryKey
    @Getter @Setter
    private SupplierProductKey key;
    @Getter @Setter
    private BigDecimal price;


    public SupplierProductKey getKey() {
        return key;
    }

    public void setKey(SupplierProductKey key) {
        this.key = key;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
