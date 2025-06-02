package com.example.nosqlapi.main_controllers;

import com.example.nosqlapi.main_entity.TradingOrganization;
import com.example.nosqlapi.main_services.TradingOrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trading-organizations")
public class TradingOrganizationController {

    private final TradingOrganizationService service;

    public TradingOrganizationController(TradingOrganizationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TradingOrganization> create(@RequestBody TradingOrganization org) {
        return ResponseEntity.ok(service.create(org));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TradingOrganization> update(@PathVariable UUID id, @RequestBody TradingOrganization updated) {
        return ResponseEntity.ok(service.update(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradingOrganization> get(@PathVariable UUID id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<TradingOrganization> getAll() {
        return service.getAll();
    }
}
