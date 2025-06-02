package com.example.nosqlapi.main_repositories;

import com.example.nosqlapi.main_entity.Employee;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends CassandraRepository<Employee, UUID> {
}
