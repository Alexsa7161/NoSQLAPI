package com.example.nosqlapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MongoController {

    private final MongoService mongoService;

    public MongoController(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    @GetMapping("/mongo-test")
    public String mongoTest() {
        return mongoService.testConnection();
    }

    @GetMapping("/mongo-create-collection")
    public String createCollection(@RequestParam String db,
                                   @RequestParam String collection) {
        try {
            mongoService.createCollection(db, collection);
            return "Collection '" + collection + "' created in database '" + db + "'";
        } catch (Exception e) {
            return "Error creating collection: " + e.getMessage();
        }
    }
}