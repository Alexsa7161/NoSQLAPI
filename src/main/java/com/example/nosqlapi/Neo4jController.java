package com.example.nosqlapi;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Neo4jController {

    private final MyService myService;

    public Neo4jController(MyService myService) {
        this.myService = myService;
    }

    @GetMapping("/neo4j-test")
    public String neo4jTest() {
        return "";
    }
}
