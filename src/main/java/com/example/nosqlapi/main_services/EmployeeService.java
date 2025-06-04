package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Employee;
import com.example.nosqlapi.main_repositories.EmployeeRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository; 
    private final MongoTemplate mongoTemplate;           
    private final Neo4jClient neo4jClient;               

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

        mongoTemplate.getCollection("employee_parameters")
                .deleteOne(Filters.eq("employee_id", id.toString()));

        neo4jClient.query("""
        MATCH (e:Employee {id: $id})
        DETACH DELETE e
        """)
                .bind(id.toString()).to("id")
                .run();
    }
    public void createOrUpdateEmployeeInNeo4j(Employee employee) {
        String query = """
        MERGE (e:Employee {employee_id: $employee_id})
        SET e.full_name = $full_name
        RETURN e
    """;

        neo4jClient.query(query)
                .bind(employee.getId().toString()).to("employee_id")
                .bind(employee.getFull_name()).to("full_name")
                .run();
    }
    public Optional<Employee> getEmployee(UUID id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public void saveEmployeeParametersToMongo(Employee employee) {
        Query query = new Query(Criteria.where("employee_id").is(employee.getId().toString()));

        Update update = new Update()
                .set("full_name", employee.getFullName())
                .set("employee_type", employee.getEmployeeType());

        mongoTemplate.upsert(query, update, "employee_parameters");
    }

    
    public void linkEmployeeWithOrder(UUID employeeId, String orderNumber) {
        com.example.nosqlapi.Procedures.createPlacedOrderRelation(String.valueOf(employeeId),orderNumber);
    }

    
    public void linkEmployeeWithOrganization(UUID employeeId, String organizationId, String shiftDate) {
        com.example.nosqlapi.Procedures.createWorksAtRelation(String.valueOf(employeeId),organizationId,shiftDate);
    }
}
