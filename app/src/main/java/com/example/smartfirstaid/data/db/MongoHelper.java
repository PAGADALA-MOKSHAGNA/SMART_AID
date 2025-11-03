package com.example.smartfirstaid.data.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import java.util.Collections;

public final class MongoHelper {
    private static MongoClient client;
    private static MongoDatabase db;

    private MongoHelper() {}

    // We can call this module whenever we need to establish a connection
    public static MongoCollection<Document> procedures() {
        if (client == null) {
            client = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(b ->
                                    b.hosts(Collections.singletonList(
                                            // Emulator -> host PC
                                            new ServerAddress("10.0.2.2", 27017)
                                    )))
                            .build());
            db = client.getDatabase("Smart_First_Aid");
        }
        return db.getCollection("procedures");
    }
    public static MongoCollection<org.bson.Document> userDetails() {
        if (client == null) {
            client = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(b -> b.hosts(
                                    java.util.Collections.singletonList(
                                            // Emulator → 10.0.2.2 | Real device → Your Laptop IPv4 (e.g., 192.168.1.23)
                                            new ServerAddress("10.0.2.2", 27017)
                                    )))
                            .build());
            db = client.getDatabase("Smart_First_Aid");
        }
        return db.getCollection("UserDetails");
    }

    /** Optional: close when app quits (not strictly required during dev). */
    public static void close() {
        try { if (client != null) client.close(); } catch (Exception ignore) {}
        client = null; db = null;
    }
}
