package com.example.nosqlapi;


import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
public class MongoService {
    private final MongoFailoverClient client;

    public MongoService(MongoFailoverClient client) {
        this.client = client;
    }

    public String testConnection() {
        try {
            // Выполняем простую команду ping для проверки соединения
            Document result = client.runCommand("admin", new Document("ping", 1));
            return "MongoDB connection successful. Ping result: " + result.toJson();
        } catch (Exception e) {
            return "MongoDB connection failed: " + e.getMessage();
        }
    }

    // Дополнительные методы для работы с MongoDB
    public void createCollection(String dbName, String collectionName) {
        client.runCommand(dbName, new Document("create", collectionName));
    }
}