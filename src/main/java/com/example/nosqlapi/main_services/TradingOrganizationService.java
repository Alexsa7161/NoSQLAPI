package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.TradingOrganization;
import com.example.nosqlapi.main_repositories.TradingOrganizationRepository;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TradingOrganizationService {

    private final TradingOrganizationRepository repository;
    private final MongoTemplate mongoTemplate;
    private final Neo4jClient neo4jClient;

    public TradingOrganizationService(TradingOrganizationRepository repository,
                                      MongoTemplate mongoTemplate,
                                      Neo4jClient neo4jClient) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

    public TradingOrganization create(TradingOrganization org) {
        TradingOrganization saved = repository.save(org);
        saveToMongo(saved);
        createOrUpdateInNeo4j(saved);
        return saved;
    }

    public TradingOrganization update(UUID id, TradingOrganization updated) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("TradingOrganization not found");
        }
        updated.setId(id);
        TradingOrganization saved = repository.save(updated);
        saveToMongo(saved);
        createOrUpdateInNeo4j(saved);
        return saved;
    }

    public void delete(UUID id) {
        repository.deleteById(id);

        mongoTemplate.getCollection("organization_parameters")
                .deleteOne(Filters.eq("organization_id", id.toString()));

        neo4jClient.query("""
            MATCH (o:TradingOrganization {id: $id})
            DETACH DELETE o
        """)
                .bind(id.toString()).to("id")
                .run();
    }

    public Optional<TradingOrganization> get(UUID id) {
        return repository.findById(id);
    }

    public List<TradingOrganization> getAll() {
        return repository.findAll();
    }

    private void saveToMongo(TradingOrganization org) {
        Document doc = new Document()
                .append("trading_organization_id", org.getId().toString())
                .append("name", org.getName())
                .append("type", org.getType())
                .append("rent_payment", org.getRent_payment().longValue())
                .append("utilities_payment", org.getUtilities_payment().longValue());

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        mongoTemplate.getCollection("organization_parameters")
                .replaceOne(Filters.eq("organization_id", org.getId().toString()), doc, options);
    }

    private void createOrUpdateInNeo4j(TradingOrganization org) {
        neo4jClient.query("""
            MERGE (o:TradingOrganization {trading_organization_id: $trading_organization_id})
            SET o.name = $name,
                o.type = $type,
                o.rent_payment = $rent_payment,
                o.utilities_payment = $utilities_payment
        """)
                .bind(org.getId().toString()).to("trading_organization_id")
                .bind(org.getName()).to("name")
                .bind(org.getType()).to("type")
                .bind(org.getRent_payment().longValue()).to("rent_payment")
                .bind(org.getUtilities_payment().longValue()).to("utilities_payment")
                .run();
    }
}
