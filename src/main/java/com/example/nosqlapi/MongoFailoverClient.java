package com.example.nosqlapi;


import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MongoFailoverClient {
    private final List<String> connectionStrings;

    private volatile MongoClient mongoClient;
    private volatile String currentConnectionString;
    private final Object connectionLock = new Object();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    public MongoFailoverClient(List<String> connectionStrings) {
        if (connectionStrings == null || connectionStrings.isEmpty()) {
            throw new IllegalArgumentException("Connection strings list cannot be null or empty");
        }
        this.connectionStrings = List.copyOf(connectionStrings);

        initializeConnection();
    }

    private void initializeConnection() {
        connectToAvailableNode();
        startHealthCheck();
    }

    private void connectToAvailableNode() {
        synchronized (connectionLock) {
            for (String connectionString : connectionStrings) {
                try {
                    System.out.println("[MongoDB] Connecting to: " + connectionString);

                    MongoClientSettings settings = MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(connectionString))
                            .applyToClusterSettings(builder ->
                                    builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                            .build();

                    MongoClient newClient = MongoClients.create(settings);


                    closeClient();
                    mongoClient = newClient;
                    currentConnectionString = connectionString;
                    System.out.println("[MongoDB] Connected to: " + connectionString);
                    return;
                } catch (Exception e) {
                    System.err.println("[MongoDB] Connection failed to " + connectionString +
                            ": " + e.getMessage());
                }
            }
            throw new RuntimeException("All MongoDB nodes are unreachable");
        }
    }

    public Document runCommand(String databaseName, Document command) {
        if (isShuttingDown.get()) {
            throw new IllegalStateException("Client is shutting down");
        }

        int maxAttempts = connectionStrings.size() * 2;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                MongoClient localClient;
                String localConnectionString;

                synchronized (connectionLock) {
                    localClient = mongoClient;
                    localConnectionString = currentConnectionString;
                }

                if (localClient == null) {
                    connectToAvailableNode();
                    continue;
                }

                MongoDatabase db = localClient.getDatabase(databaseName);
                return db.runCommand(command);
            } catch (Exception e) {
                System.err.printf("[MongoDB] Attempt %d/%d failed: %s%n",
                        attempt + 1, maxAttempts, e.getMessage());

                if (isConnectionError(e)) {
                    connectToAvailableNode();
                }

                try {
                    Thread.sleep(1000 * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        throw new RuntimeException("Failed after " + maxAttempts + " attempts");
    }

    private boolean isConnectionError(Exception e) {
        return e instanceof MongoSocketException ||
                e instanceof MongoTimeoutException ||
                e instanceof MongoNotPrimaryException ||
                e instanceof MongoNodeIsRecoveringException ||
                e.getMessage() != null && (
                        e.getMessage().contains("Unable to connect") ||
                                e.getMessage().contains("connection reset") ||
                                e.getMessage().contains("terminated") ||
                                e.getMessage().contains("not master")
                );
    }

    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isShuttingDown.get()) return;

            synchronized (connectionLock) {
                try {
                    if (mongoClient == null) {
                        connectToAvailableNode();
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("[HealthCheck] Failed: " + e.getMessage());
                    connectToAvailableNode();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void closeClient() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                System.err.println("Error closing MongoDB client: " + e.getMessage());
            }
            mongoClient = null;
            currentConnectionString = null;
        }
    }

    @PreDestroy
    public void close() {
        isShuttingDown.set(true);
        scheduler.shutdownNow();
        closeClient();
    }
}