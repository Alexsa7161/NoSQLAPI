package com.example.nosqlapi.main_entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;
@Setter
@Getter
@Table("order")
public class Order {
    @PrimaryKey
    private UUID order_number;
    private UUID employee_id;
    public UUID getOrder_number() {return order_number;}
    public void setOrder_number(UUID order_number) {this.order_number = order_number;}
}