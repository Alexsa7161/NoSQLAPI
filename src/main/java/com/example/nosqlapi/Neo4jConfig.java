package com.example.nosqlapi;

import com.example.nosqlapi.Neo4jFailoverClient;
import org.neo4j.driver.AuthTokens;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class Neo4jConfig {

    @Bean
    public Neo4jFailoverClient neo4jFailoverClient() {
        return new Neo4jFailoverClient(
                List.of(
                        "bolt://localhost:7681",
                        "bolt://localhost:7682",
                        "bolt://localhost:7683"
                )
        );
    }
}

