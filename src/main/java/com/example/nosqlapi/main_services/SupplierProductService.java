package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.SupplierProduct;
import com.example.nosqlapi.main_entity.SupplierProductKey;
import com.example.nosqlapi.main_repositories.SupplierProductRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SupplierProductService {

    private final SupplierProductRepository repository;
    private final Neo4jClient neo4jClient;

    public SupplierProductService(SupplierProductRepository repository, Neo4jClient neo4jClient) {
        this.repository = repository;
        this.neo4jClient = neo4jClient;
    }

    public SupplierProduct create(SupplierProduct supplierProduct) {
        // Сохраняем в Cassandra
        SupplierProduct saved = repository.save(supplierProduct);

        // Создаём/обновляем узлы и связь в Neo4j
        createOrUpdateInNeo4j(saved);

        return saved;
    }

    public SupplierProduct update(UUID supplierId, UUID productId, SupplierProduct supplierProduct) {
        SupplierProductKey key = new SupplierProductKey();
        key.setSupplier_id(supplierId);
        key.setProduct_id(productId);

        if (!repository.existsById(key)) {
            throw new RuntimeException("SupplierProduct not found");
        }

        supplierProduct.setKey(key);
        SupplierProduct updated = repository.save(supplierProduct);

        createOrUpdateInNeo4j(updated);

        return updated;
    }

    public void delete(UUID supplierId, UUID productId) {
        SupplierProductKey key = new SupplierProductKey();
        key.setSupplier_id(supplierId);
        key.setProduct_id(productId);

        repository.deleteById(key);

        // Удаляем только связь, а не узлы
        neo4jClient.query("""
            MATCH (s:Supplier {supplier_id: $supplier_id})-[r:OFFERS]->(p:Product {product_id: $product_id})
            DELETE r
        """)
                .bind(supplierId.toString()).to("supplier_id")
                .bind(productId.toString()).to("product_id")
                .run();
    }

    public SupplierProduct get(UUID supplierId, UUID productId) {
        SupplierProductKey key = new SupplierProductKey();
        key.setSupplier_id(supplierId);
        key.setProduct_id(productId);

        return repository.findById(key).orElse(null);
    }

    private void createOrUpdateInNeo4j(SupplierProduct supplierProduct) {
        UUID supplierId = supplierProduct.getKey().getSupplier_id();
        UUID productId = supplierProduct.getKey().getProduct_id();
        double price = supplierProduct.getPrice().doubleValue();

        // Узлы
        String nodeQuery = """
            MERGE (s:Supplier {supplier_id: $supplier_id})
            MERGE (p:Product {product_id: $product_id})
        """;

        neo4jClient.query(nodeQuery)
                .bind(supplierId.toString()).to("supplier_id")
                .bind(productId.toString()).to("product_id")
                .run();

        // Связь с атрибутом цены
        String relationQuery = """
            MATCH (s:Supplier {supplier_id: $supplier_id}), (p:Product {product_id: $product_id})
            MERGE (s)-[r:OFFERS]->(p)
            SET r.price = $price
        """;

        neo4jClient.query(relationQuery)
                .bind(supplierId.toString()).to("supplier_id")
                .bind(productId.toString()).to("product_id")
                .bind(price).to("price")
                .run();
    }
}
