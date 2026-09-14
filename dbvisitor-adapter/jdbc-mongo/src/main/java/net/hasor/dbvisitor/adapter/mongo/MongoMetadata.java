/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import net.hasor.dbvisitor.driver.MetadataNode;
import net.hasor.dbvisitor.driver.MetadataPath;
import net.hasor.dbvisitor.driver.MetadataSupport;
import net.hasor.dbvisitor.driver.MetadataType;
import org.bson.Document;

/** Databases and collections from MongoDB; document fields are not inferred. */
final class MongoMetadata implements MetadataSupport {
    private final MongoClient client;

    MongoMetadata(MongoClient client) {
        this.client = client;
    }

    @Override
    public Set<MetadataType> supportedTypes() {
        return Set.of(MetadataType.CATALOG, MetadataType.TABLE, MetadataType.VIEW);
    }

    @Override
    public List<MetadataNode> query(MetadataPath path) throws SQLException {
        try {
            if (path.type() == MetadataType.CATALOG && path.levels().isEmpty()) {
                List<MetadataNode> nodes = new ArrayList<>();
                try (MongoCursor<String> cursor = client.listDatabaseNames().iterator()) {
                    while (cursor.hasNext()) {
                        nodes.add(new MetadataNode(MetadataType.CATALOG, cursor.next()));
                    }
                }
                return nodes;
            }
            String database = path.name(MetadataType.CATALOG);
            if (database == null || path.levels().size() != 1 || (path.type() != MetadataType.TABLE && path.type() != MetadataType.VIEW)) {
                return List.of();
            }
            List<MetadataNode> nodes = new ArrayList<>();
            try (MongoCursor<Document> cursor = client.getDatabase(database).listCollections().iterator()) {
                while (cursor.hasNext()) {
                    Document collection = cursor.next();
                    String name = collection.getString("name");
                    MetadataType type = "view".equals(collection.getString("type")) ? MetadataType.VIEW : MetadataType.TABLE;
                    if (name != null && path.type() == type) {
                        nodes.add(new MetadataNode(type, name));
                    }
                }
            }
            return nodes;
        } catch (MongoException e) {
            throw new SQLException("Unable to read MongoDB metadata.", e);
        }
    }
}
