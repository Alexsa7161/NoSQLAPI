package com.example.nosqlapi;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

@Configuration
public class CassandraConfig {

    @Bean
    public CqlTemplate cqlTemplate(CqlSession cqlSession) {
        return new CqlTemplate(cqlSession);
    }
}
