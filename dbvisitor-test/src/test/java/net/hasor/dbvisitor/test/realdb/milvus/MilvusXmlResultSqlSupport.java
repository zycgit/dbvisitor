/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import static org.junit.Assert.*;

public abstract class MilvusXmlResultSqlSupport extends MilvusSqlContractSupport {

    protected Map<String, Object> parameters(int minId) {
        return Map.of("collection", this.collection, "minId", minId, "vector", new float[] { 0, 0 });
    }

    protected Session prepareSession() throws Exception {
        createCollection("id INT64 PRIMARY KEY, name VARCHAR(32), age INT32 NULL, email VARCHAR(64) NULL, create_time VARCHAR(64) NULL, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, name, v, age, email, create_time) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int i = 1; i <= 3; i++) {
                insert.setLong(1, i);
                insert.setString(2, "name-" + i);
                insert.setObject(3, new float[] { i, 0 });
                insert.setInt(4, 20 + i);
                insert.setString(5, "row" + i + "@test.com");
                insert.setTimestamp(6, new Timestamp(1700000000123L));
                assertEquals(1, insert.executeUpdate());
            }
        }
        Configuration configuration = new Configuration();
        configuration.loadMapper("/realdb/milvus/mapper/ResultHandlerMapper.xml");
        return configuration.newSession(newAdapterConnection());
    }
}
