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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

public class InsertConflictTest extends AbstractJdbcTest {
    @Test
    public void updatePreservesParameterOrderAndReturnsOnlyNewKeys() throws Exception {
        String payload = "name' }, $where: 'malicious";
        ObjectId id = new ObjectId();
        mockBulk(0, Collections.singletonList(new BulkWriteUpsert(0, new BsonObjectId(id))), models -> {
            assertEquals(1, models.size());
            UpdateOneModel<Document> update = (UpdateOneModel<Document>) models.get(0);
            assertEquals(new Document("tenant", new Document("$eq", "east")).append("id", new Document("$eq", 42)), update.getFilter());
            assertEquals(new Document("$set", new Document("name", payload).append("age", 18)), update.getUpdate());
            assertTrue(update.getOptions().isUpsert());
        });
        try (Connection connection = redisConnection("test"); PreparedStatement statement = connection.prepareStatement(
                "/*+mongo_duplicate_strategy='update',mongo_primary_keys='dGVuYW50.aWQ'*/db.people.insertMany([{name: ?, tenant: ?, age: ?, id: ?}])",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, payload);
            statement.setString(2, "east");
            statement.setInt(3, 18);
            statement.setInt(4, 42);
            assertEquals(1, statement.executeUpdate());
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                assertEquals(id.toHexString(), keys.getString(1));
                assertFalse(keys.next());
            }
        }
    }

    @Test
    public void ignoreUsesSetOnInsertAndReportsNoChangeForExistingRows() throws Exception {
        mockBulk(0, Collections.emptyList(), models -> {
            UpdateOneModel<Document> update = (UpdateOneModel<Document>) models.get(0);
            assertEquals(new Document("id", new Document("$eq", 42L)), update.getFilter());
            assertEquals(new Document("$setOnInsert", new Document("name", "new")), update.getUpdate());
        });
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("/*+mongo_duplicate_strategy='ignore',mongo_primary_keys='aWQ'*/db.people.insertMany([{id: 42,name: 'new'}])"));
        }
    }

    @Test
    public void keyOnlyUpdateNeverOverwritesThePrimaryKey() throws Exception {
        mockBulk(0, Collections.emptyList(), models -> {
            UpdateOneModel<Document> update = (UpdateOneModel<Document>) models.get(0);
            assertEquals(new Document("$setOnInsert", new Document()), update.getUpdate());
        });
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("/*+mongo_duplicate_strategy='update',mongo_primary_keys='aWQ'*/db.people.insertMany([{id: 42}])"));
        }
    }

    @Test
    public void documentValuedKeyRemainsAnEqualityValue() throws Exception {
        Document value = new Document("$ne", null);
        mockBulk(0, Collections.emptyList(), models -> {
            UpdateOneModel<Document> update = (UpdateOneModel<Document>) models.get(0);
            assertEquals(new Document("id", new Document("$eq", value)), update.getFilter());
        });
        try (Connection connection = redisConnection("test"); PreparedStatement statement = connection.prepareStatement(
                "/*+mongo_duplicate_strategy='update',mongo_primary_keys='aWQ'*/db.people.insertMany([{id: ?,name: 'new'}])")) {
            statement.setObject(1, value);
            assertEquals(0, statement.executeUpdate());
        }
    }

    @Test
    public void missingPrimaryKeyFailsBeforeWriting() throws Exception {
        mockBulk(0, Collections.emptyList(), models -> fail("Invalid keys must not reach bulkWrite"));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("/*+mongo_duplicate_strategy='update',mongo_primary_keys='aWQ'*/db.people.insertMany([{name: 'new'}])");
                fail("Missing primary key must be reported");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("primary key"));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void mockBulk(int modified, List<BulkWriteUpsert> upserts, Consumer<List<WriteModel<Document>>> assertion) {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (method, args) -> {
            MongoCollection<Document> collection = PowerMockito.mock(MongoCollection.class);
            BulkWriteResult result = PowerMockito.mock(BulkWriteResult.class);
            PowerMockito.when(result.getModifiedCount()).thenReturn(modified);
            PowerMockito.when(result.getUpserts()).thenReturn(upserts);
            PowerMockito.when(collection.bulkWrite(anyList(), any(BulkWriteOptions.class))).thenAnswer(invocation -> {
                assertion.accept(invocation.getArgument(0));
                return result;
            });
            return collection;
        }));
    }
}
