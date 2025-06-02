package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.TradingOrganization;
import com.example.nosqlapi.main_repositories.TradingOrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TradingOrganizationService {

    private final TradingOrganizationRepository repository;

    public TradingOrganizationService(TradingOrganizationRepository repository) {
        this.repository = repository;
    }

    public TradingOrganization create(TradingOrganization org) {
        return repository.save(org);
    }

    public TradingOrganization update(UUID id, TradingOrganization updated) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("TradingOrganization not found");
        }
        updated.setId(id);
        return repository.save(updated);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Optional<TradingOrganization> get(UUID id) {
        return repository.findById(id);
    }

    public List<TradingOrganization> getAll() {
        return repository.findAll();
    }
}
