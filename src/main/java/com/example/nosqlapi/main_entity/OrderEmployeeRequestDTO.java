package com.example.nosqlapi.main_entity;

import java.util.UUID;

public class OrderEmployeeRequestDTO {
    private UUID orderNumber;
    private String employeeFullName;
    private String requestData;

    public UUID getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(UUID orderNumber) {
        this.orderNumber = orderNumber;
    }
    public String getEmployeeFullName() {
        return employeeFullName;
    }
    public void setEmployeeFullName(String employeeFullName) {
        this.employeeFullName = employeeFullName;
    }
    public String getRequestData() {
        return requestData;
    }
    public void setRequestData(Object requestData) {
        this.requestData = (String) requestData;
    }
}
