package com.example.nosqlapi.main_repositories;

import com.example.nosqlapi.main_entity.Order;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends CassandraRepository<Order, UUID> {
}
