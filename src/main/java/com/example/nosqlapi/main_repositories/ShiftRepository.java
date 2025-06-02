package com.example.nosqlapi.main_repositories;

import com.example.nosqlapi.main_entity.Shift;
import com.example.nosqlapi.main_entity.ShiftKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface ShiftRepository extends CassandraRepository<Shift, ShiftKey> {
}