package com.example.nosqlapi;

import org.springframework.stereotype.Service;

@Service
public class MyService {
    private final Neo4jFailoverClient client;

    public MyService(Neo4jFailoverClient client) {
        this.client = client;
    }
}

