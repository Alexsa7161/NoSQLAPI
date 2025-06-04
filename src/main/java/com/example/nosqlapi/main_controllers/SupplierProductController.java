package com.example.nosqlapi.main_controllers;


import com.example.nosqlapi.main_entity.SupplierProduct;
import com.example.nosqlapi.main_entity.SupplierProductKey;
import com.example.nosqlapi.main_services.SupplierProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/supplier-products")
public class SupplierProductController {

    private final SupplierProductService service;

    public SupplierProductController(SupplierProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SupplierProduct> create(@RequestBody SupplierProduct supplierProduct) {
        return ResponseEntity.ok(service.create(supplierProduct));
    }

    @PutMapping("/{supplierId}/{productId}")
    public ResponseEntity<SupplierProduct> update(
            @PathVariable UUID supplierId,
            @PathVariable UUID productId,
            @RequestBody SupplierProduct supplierProduct) {

        SupplierProductKey key = new SupplierProductKey();
        key.setSupplier_id(supplierId);
        key.setProduct_id(productId);
        return ResponseEntity.ok(service.update(key.getSupplier_id(), key.getProduct_id(), supplierProduct));
    }

    @DeleteMapping("/{supplierId}/{productId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID supplierId,
            @PathVariable UUID productId) {

        SupplierProductKey key = new SupplierProductKey();
        key.setSupplier_id(supplierId);
        key.setProduct_id(productId);
        service.delete(key.getSupplier_id(), key.getProduct_id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{supplierId}/{productId}")
    public ResponseEntity<SupplierProduct> get(
            @PathVariable UUID supplierId,
            @PathVariable UUID productId) {

        SupplierProduct result = service.get(supplierId, productId);

        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
