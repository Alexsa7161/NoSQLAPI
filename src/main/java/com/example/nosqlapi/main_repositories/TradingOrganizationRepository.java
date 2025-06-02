package com.example.nosqlapi.main_repositories;

import com.example.nosqlapi.main_entity.TradingOrganization;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradingOrganizationRepository extends CassandraRepository<TradingOrganization, UUID> {
}
