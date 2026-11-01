/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Verify hint arguments consume placeholders in order and are passed through with other parameters.
 */
public class HintCommandTest extends AbstractJdbcTest {

    @Test
    public void hint_args_consumed_before_command_db_and_collection() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();

        AtomicReference<String> dbRef = new AtomicReference<>();
        AtomicReference<String> collRef = new AtomicReference<>();

        // Intercept MongoClient.getDatabase -> capture db name; mock getCollection/drop
        MongoCommandInterceptor.addInterceptor(MongoClient.class, createInvocationHandler("getDatabase", (name, args) -> {
            dbRef.set((String) args[0]);
            MongoDatabase mockDb = Mockito.mock(MongoDatabase.class);
            MongoCollection<?> mockColl = Mockito.mock(MongoCollection.class);
            Mockito.when(mockDb.getCollection(anyString())).thenAnswer(inv -> {
                collRef.set(inv.getArgument(0));
                return mockColl;
            });
            return mockDb;
        }));

        // hint has 1 placeholder + db + collection placeholders => 3 total
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement(); PreparedStatement ps = conn.prepareStatement("/*+abc=?,abc=null;abc=aaa*/ ?.?.drop();")) {
            stmt.execute("use mydb");
            ps.setString(1, "hint-val");
            ps.setString(2, "dbX");
            ps.setString(3, "colX");
            ps.execute();
        }

        Assertions.assertEquals("dbX", dbRef.get(), "db should come from 2nd placeholder (after hint)");
        Assertions.assertEquals("colX", collRef.get(), "collection should come from 3rd placeholder");
    }

    @Test
    public void multiple_hint_blocks_consume_placeholders_in_order() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();

        AtomicReference<String> dbRef = new AtomicReference<>();
        AtomicReference<String> collRef = new AtomicReference<>();

        MongoCommandInterceptor.addInterceptor(MongoClient.class, createInvocationHandler("getDatabase", (name, args) -> {
            dbRef.set((String) args[0]);
            MongoDatabase mockDb = Mockito.mock(MongoDatabase.class);
            MongoCollection<?> mockColl = Mockito.mock(MongoCollection.class);
            Mockito.when(mockDb.getCollection(anyString())).thenAnswer(inv -> {
                collRef.set(inv.getArgument(0));
                return mockColl;
            });
            return mockDb;
        }));

        // Two hint blocks, each with a single placeholder; then db and collection placeholders
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement(); PreparedStatement ps = conn.prepareStatement("/*+abc=?,abc=null;abc=aaa*//*+abc=?,abc=null;abc=aaa*/ ?.ccc.drop();")) {
            stmt.execute("use mydb");
            ps.setString(1, "hint1");
            ps.setString(2, "hint2");
            ps.setString(3, "dbY");
            ps.execute();
        }

        Assertions.assertEquals("dbY", dbRef.get(), "db should be consumed after both hint placeholders");
        Assertions.assertEquals("ccc", collRef.get(), "collection should be literal from command");
    }
}
