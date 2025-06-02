package com.example.nosqlapi.main_repositories;


import com.example.nosqlapi.main_entity.Payment;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface PaymentRepository extends CassandraRepository<Payment, UUID> {
}
