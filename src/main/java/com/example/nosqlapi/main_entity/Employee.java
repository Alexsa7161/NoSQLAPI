package com.example.nosqlapi.main_entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("employee")
public class Employee {
    @Setter
    @Getter
    @PrimaryKey
    private UUID id;
    private String full_name;
    private String employee_type;

    public Object getFullName() {return full_name;}
    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}
    public Object getEmployeeType() {return employee_type;}

    public void setFullName(Object fullName) {this.full_name = (String) fullName;}

    public void setEmployeeType(Object employeeType) {this.employee_type = (String) employeeType;}

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getEmployee_type() {
        return employee_type;
    }

    public void setEmployee_type(String employee_type) {
        this.employee_type = employee_type;
    }
}