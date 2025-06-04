package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Shift;
import com.example.nosqlapi.main_entity.ShiftKey;
import com.example.nosqlapi.main_repositories.ShiftRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final MongoTemplate mongoTemplate;
    private final Neo4jClient neo4jClient;

    public ShiftService(ShiftRepository shiftRepository,
                        MongoTemplate mongoTemplate,
                        Neo4jClient neo4jClient) {
        this.shiftRepository = shiftRepository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

    public Shift createShift(Shift shift) {
        Shift saved = shiftRepository.save(shift);

        return saved;
    }

    public Shift updateShift(ShiftKey key, Shift shift) {
        if (!shiftRepository.existsById(key)) {
            throw new RuntimeException("Shift not found");
        }
        shift.setKey(key);
        Shift updated = shiftRepository.save(shift);

        return updated;
    }

    public void deleteShift(ShiftKey key) {
        shiftRepository.deleteById(key);
    }

    public Optional<Shift> getShift(ShiftKey key) {
        return shiftRepository.findById(key);
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

}
