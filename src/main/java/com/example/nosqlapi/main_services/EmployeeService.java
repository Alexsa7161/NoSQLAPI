package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Employee;
import com.example.nosqlapi.main_repositories.EmployeeRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository; // Cassandra
    private final MongoTemplate mongoTemplate;           // MongoDB
    private final Neo4jClient neo4jClient;               // Neo4j

    public EmployeeService(EmployeeRepository employeeRepository,
                           MongoTemplate mongoTemplate,
                           Neo4jClient neo4jClient) {
        this.employeeRepository = employeeRepository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

    public Employee createEmployee(Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);
        saveEmployeeParametersToMongo(savedEmployee);
        createOrUpdateEmployeeInNeo4j(savedEmployee);
        return savedEmployee;
    }

    public Employee updateEmployee(UUID id, Employee employee) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found");
        }
        employee.setId(id);
        Employee updatedEmployee = employeeRepository.save(employee);
        saveEmployeeParametersToMongo(updatedEmployee);
        createOrUpdateEmployeeInNeo4j(updatedEmployee);
        return updatedEmployee;
    }

    public void deleteEmployee(UUID id) {
        employeeRepository.deleteById(id);

        // Удаление из MongoDB
        mongoTemplate.getCollection("employee_parameters")
                .deleteOne(Filters.eq("employee_id", id.toString()));

        // ❗Если есть процедура для удаления в Neo4j — можно вызвать её здесь:
        // neo4jClient.query("CALL employee.delete($id)")
        //     .bind(id.toString()).to("id").run();
    }

    public Optional<Employee> getEmployee(UUID id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    private void saveEmployeeParametersToMongo(Employee employee) {
        Document doc = new Document()
                .append("employee_id", employee.getId().toString())
                .append("full_name", employee.getFullName())
                .append("employee_type", employee.getEmployeeType());

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        mongoTemplate.getCollection("employee_parameters")
                .replaceOne(Filters.eq("employee_id", employee.getId().toString()), doc, options);
    }

    private void createOrUpdateEmployeeInNeo4j(Employee employee) {
        neo4jClient.query("CALL com.example.employee.createOrUpdate($id, $fullName, $employeeType)")
                .bind(employee.getId().toString()).to("id")
                .bind(employee.getFullName()).to("fullName")
                .bind(employee.getEmployeeType()).to("employeeType")
                .run();
    }

    /**
     * Связывает сотрудника с заказом в Neo4j через :PLACED
     */
    public void linkEmployeeWithOrder(UUID employeeId, String orderNumber) {
        neo4jClient.query("CALL com.example.employee.createPlacedOrderRelation($employeeId, $orderNumber)")
                .bind(employeeId.toString()).to("employeeId")
                .bind(orderNumber).to("orderNumber")
                .run();
    }

    /**
     * Связывает сотрудника с организацией в Neo4j через :WORKS_AT {shift_date}
     */
    public void linkEmployeeWithOrganization(UUID employeeId, String organizationId, String shiftDate) {
        neo4jClient.query("CALL com.example.employee.createWorksAtRelation($employeeId, $organizationId, $shiftDate)")
                .bind(employeeId.toString()).to("employeeId")
                .bind(organizationId).to("organizationId")
                .bind(shiftDate).to("shiftDate")
                .run();
    }
}
