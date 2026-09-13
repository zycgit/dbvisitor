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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import net.hasor.dbvisitor.driver.AdapterCursor;
import net.hasor.dbvisitor.driver.AdapterMetadata;
import org.bson.Document;

/** MongoDB databases are JDBC catalogs; collections have no separate SQL schema. */
final class MongoMetadata {
    private MongoMetadata() {
    }

    static AdapterCursor tables(MongoClient client, String catalog, String schemaPattern, String tablePattern, String[] types) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!AdapterMetadata.matchesPattern("", schemaPattern) || (types != null && types.length == 0) || "".equals(catalog)) {
            return AdapterMetadata.tables(rows);
        }
        try {
            List<String> databases = new ArrayList<>();
            if (catalog != null) {
                databases.add(catalog);
            } else {
                try (MongoCursor<String> cursor = client.listDatabaseNames().iterator()) {
                    while (cursor.hasNext()) {
                        databases.add(cursor.next());
                    }
                }
            }
            for (String database : databases) {
                try (MongoCursor<Document> cursor = client.getDatabase(database).listCollections().iterator()) {
                    while (cursor.hasNext()) {
                        Document collection = cursor.next();
                        String name = collection.getString("name");
                        String type = "view".equals(collection.getString("type")) ? "VIEW" : "TABLE";
                        if (name == null || !AdapterMetadata.matchesPattern(name, tablePattern) || (types != null && !Arrays.asList(types).contains(type))) {
                            continue;
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("TABLE_CAT", database);
                        row.put("TABLE_NAME", name);
                        row.put("TABLE_TYPE", type);
                        rows.add(row);
                    }
                }
            }
        } catch (MongoException e) {
            throw new SQLException("Unable to read MongoDB collection metadata.", e);
        }
        rows.sort(Comparator.comparing((Map<String, Object> row) -> row.get("TABLE_TYPE").toString())
                .thenComparing(row -> row.get("TABLE_CAT").toString()).thenComparing(row -> row.get("TABLE_NAME").toString()));
        return AdapterMetadata.tables(rows);
    }

}
