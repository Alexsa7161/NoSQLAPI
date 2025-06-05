package com.example.nosqlapi;

import com.example.nosqlapi.MongoFailoverClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoFailoverClient mongoFailoverClient() {
        return new MongoFailoverClient(
                List.of(
                        "mongodb://localhost:27017",
                        "mongodb://localhost:27018",
                        "mongodb://localhost:27019"
                )
        );
    }
}