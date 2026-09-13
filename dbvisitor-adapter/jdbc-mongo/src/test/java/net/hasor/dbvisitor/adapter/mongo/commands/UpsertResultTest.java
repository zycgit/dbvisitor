/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

public class UpsertResultTest extends AbstractJdbcTest {
    @Test
    public void insertedUpsertCountsAsOneAndReturnsItsKey() throws Exception {
        ObjectId id = new ObjectId();
        verifyResults(UpdateResult.acknowledged(0, 0L, new BsonObjectId(id)), 1, id.toHexString());
    }

    @Test
    public void unchangedMatchedDocumentHasNoAffectedRowsOrGeneratedKey() throws Exception {
        verifyResults(UpdateResult.acknowledged(1, 0L, null), 0, null);
    }

    @Test
    public void modifiedDocumentCountsAsOneWithoutInventingAKey() throws Exception {
        verifyResults(UpdateResult.acknowledged(1, 1L, null), 1, null);
    }

    @SuppressWarnings("unchecked")
    private void verifyResults(UpdateResult result, int count, String key) throws Exception {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (method, args) -> {
            MongoCollection<Document> collection = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(collection.updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(result);
            PowerMockito.when(collection.updateMany(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(result);
            PowerMockito.when(collection.replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class))).thenReturn(result);
            return collection;
        }));
        // @formatter:off
        String[] commands = {
            "db.people.update({id: 1}, {$set: {name: 'new'}}, {upsert: true})",
            "db.people.updateOne({id: 1}, {$set: {name: 'new'}}, {upsert: true})",
            "db.people.updateMany({id: 1}, {$set: {name: 'new'}}, {upsert: true})",
            "db.people.replaceOne({id: 1}, {id: 1, name: 'new'}, {upsert: true})"
        };
        // @formatter:on
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement()) {
            for (String command : commands) {
                assertEquals(command, count, statement.executeUpdate(command, Statement.RETURN_GENERATED_KEYS));
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (key != null) {
                        assertTrue(command, keys.next());
                        assertEquals(command, key, keys.getString(1));
                    }
                    assertFalse(command, keys.next());
                }
            }
        }
    }
}
