package com.example.nosqlapi.main_entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.util.UUID;

@Table("trading_organization")
public class TradingOrganization {
    @PrimaryKey @Setter @Getter
    private UUID id;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String type;
    @Setter
    @Getter
    private double rent_payment;
    @Setter
    @Getter
    private double utilities_payment;

    public double getRent_payment() {
        return rent_payment;
    }

    public void setRent_payment(double rent_payment) {
        this.rent_payment = rent_payment;
    }

    public double getUtilities_payment() {
        return utilities_payment;
    }

    public void setUtilities_payment(double utilities_payment) {
        this.utilities_payment = utilities_payment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}