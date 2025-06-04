package com.example.nosqlapi.main_controllers;

import com.example.nosqlapi.main_entity.Employee;
import com.example.nosqlapi.main_services.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable UUID id, @RequestBody Employee employee) {
        Employee updated = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable UUID id) {
        return employeeService.getEmployee(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @PostMapping("/{employeeId}/link-order")
    public ResponseEntity<Void> linkEmployeeWithOrder(@PathVariable UUID employeeId,
                                                      @RequestParam String orderNumber) {
        employeeService.linkEmployeeWithOrder(employeeId, orderNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{employeeId}/link-organization")
    public ResponseEntity<Void> linkEmployeeWithOrganization(@PathVariable UUID employeeId,
                                                             @RequestParam String organizationId,
                                                             @RequestParam String shiftDate) {
        employeeService.linkEmployeeWithOrganization(employeeId, organizationId, shiftDate);
        return ResponseEntity.ok().build();
    }
}
