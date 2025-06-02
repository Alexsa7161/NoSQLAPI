package com.example.nosqlapi.main_entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Setter
@Getter
@Table("shift")
public class Shift {
    @PrimaryKey
    private ShiftKey key;
    private UUID trading_organization_id;

    public ShiftKey getKey() {
        return key;
    }

    public void setKey(ShiftKey key) {
        this.key = key;
    }

    public UUID getTrading_organization_id() {
        return trading_organization_id;
    }

    public void setTrading_organization_id(UUID trading_organization_id) {
        this.trading_organization_id = trading_organization_id;
    }
}