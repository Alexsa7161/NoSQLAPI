package com.example.nosqlapi.main_entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@Table("payment")
public class Payment {
    @PrimaryKey
    private UUID id;
    private UUID trading_organization_id;
    private String payment_type;
    private BigDecimal amount;
    public UUID getId() {return id;}
    public UUID setId(UUID id) {return this.id = id;}
    public UUID getTrading_organization_id() {return trading_organization_id;}
    public UUID setTradingOrganizationId(UUID tradingOrganizationId) {return this.trading_organization_id = tradingOrganizationId;}
    public String getPayment_type() {return payment_type;}
    public BigDecimal getAmount() {return amount;}
    public Payment setAmount(BigDecimal amount) {this.amount = amount;return this;}
    public Payment setPayment_type(String payment_type) {this.payment_type = payment_type;return this;}
}
