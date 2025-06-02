package com.example.nosqlapi.main_repositories;

import com.example.nosqlapi.main_entity.SupplierProduct;
import com.example.nosqlapi.main_entity.SupplierProductKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierProductRepository extends CassandraRepository<SupplierProduct, SupplierProductKey> {
}
