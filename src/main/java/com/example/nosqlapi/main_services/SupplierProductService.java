package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.SupplierProduct;
import com.example.nosqlapi.main_entity.SupplierProductKey;
import com.example.nosqlapi.main_repositories.SupplierProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierProductService {

    private final SupplierProductRepository repository;

    public SupplierProductService(SupplierProductRepository repository) {
        this.repository = repository;
    }

    public SupplierProduct create(SupplierProduct supplierProduct) {
        return repository.save(supplierProduct);
    }

    public SupplierProduct update(SupplierProductKey key, SupplierProduct updated) {
        if (!repository.existsById(key)) {
            throw new RuntimeException("SupplierProduct not found");
        }
        updated.setKey(key);
        return repository.save(updated);
    }

    public void delete(SupplierProductKey key) {
        repository.deleteById(key);
    }

    public Optional<SupplierProduct> get(SupplierProductKey key) {
        return repository.findById(key);
    }

    public List<SupplierProduct> getAll() {
        return repository.findAll();
    }
}
