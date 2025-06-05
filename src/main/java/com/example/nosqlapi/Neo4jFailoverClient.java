package com.example.nosqlapi;

import jakarta.annotation.PreDestroy;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Neo4jFailoverClient {
    private final List<String> uriList;
    private volatile Driver driver;
    private volatile String currentUri;
    private final Object connectionLock = new Object();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    public Neo4jFailoverClient(List<String> uriList) {
        if (uriList == null || uriList.isEmpty()) {
            throw new IllegalArgumentException("URI list cannot be null or empty");
        }
        this.uriList = List.copyOf(uriList);
        initializeConnection();
    }

    private void initializeConnection() {
        connectToAvailableNode();
        startHealthCheck();
    }

    private void connectToAvailableNode() {
        synchronized (connectionLock) {
            for (String uri : uriList) {
                try {
                    System.out.println("[Neo4j] Connecting to: " + uri);
                    Driver newDriver = GraphDatabase.driver(uri);

                    try (Session session = newDriver.session()) {
                        session.run("RETURN 1").consume();
                    }

                    closeDriver();
                    driver = newDriver;
                    currentUri = uri;
                    System.out.println("[Neo4j] Connected to: " + uri);
                    return;
                } catch (Exception e) {
                    System.err.println("[Neo4j] Connection failed to " + uri + ": " + e.getMessage());
                }
            }
            throw new RuntimeException("All Neo4j nodes are unreachable");
        }
    }

    public void runQuery(String cypher, Map<String, Object> parameters) {
        if (isShuttingDown.get()) {
            throw new IllegalStateException("Client is shutting down");
        }

        Driver localDriver;
        synchronized (connectionLock) {
            localDriver = driver;
        }

        if (localDriver == null) {
            throw new IllegalStateException("No available Neo4j connection");
        }

        try (Session session = localDriver.session()) {
            session.run(cypher, parameters).consume();
        }
    }

    private boolean isConnectionError(Exception e) {
        return e instanceof ServiceUnavailableException ||
                e instanceof SessionExpiredException ||
                e.getMessage() != null && (
                        e.getMessage().contains("Unable to connect") ||
                                e.getMessage().contains("connection reset") ||
                                e.getMessage().contains("terminated")
                );
    }

    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isShuttingDown.get()) return;

            synchronized (connectionLock) {
                try {
                    if (driver == null) {
                        connectToAvailableNode();
                        return;
                    }

                    try (Session session = driver.session()) {
                        session.run("RETURN 1").consume();
                    }
                } catch (Exception e) {
                    System.err.println("[HealthCheck] Failed: " + e.getMessage());
                    connectToAvailableNode();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void closeDriver() {
        if (driver != null) {
            try {
                driver.close();
            } catch (Exception e) {
                System.err.println("Error closing driver: " + e.getMessage());
            }
            driver = null;
            currentUri = null;
        }
    }

    @PreDestroy
    public void close() {
        isShuttingDown.set(true);
        scheduler.shutdownNow();
        closeDriver();
    }
}