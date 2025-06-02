package com.example.nosqlapi.main_entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;
@Setter
@Getter
@PrimaryKeyClass
public class ShiftKey implements Serializable {
    @PrimaryKeyColumn(name = "employee_id", type = PARTITIONED)
    private UUID employee_id;

    public UUID getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(UUID employee_id) {
        this.employee_id = employee_id;
    }

    @PrimaryKeyColumn(name = "shift_date")
    private LocalDate shift_date;

    public LocalDate getShift_date() {
        return shift_date;
    }

    public void setShift_date(LocalDate shift_date) {
        this.shift_date = shift_date;
    }
}
