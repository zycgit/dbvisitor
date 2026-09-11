/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.BeforeClass;
import org.junit.Test;

public class RealMongoTest {
    @BeforeClass
    public static void assumeDataSource() {
        OneApiDataSourceManager.assumeCurrentDataSource("mongo");
    }

    @Test
    public void test_01() throws Exception {
        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
            // 1. clean
            try (Statement s = c.createStatement()) {
                try {
                    s.execute("test.user_info.drop()");
                } catch (Exception e) {
                    // ignore
                }
            }

            // 2. insert
            try (Statement s = c.createStatement()) {
                s.execute("test.user_info.insert({name: 'mali', age: 26})");
            }
            try (Statement s = c.createStatement()) {
                s.execute("test.user_info.insert({name: 'dative', age: 32})");
            }
            try (Statement s = c.createStatement()) {
                s.execute("test.user_info.insert({name: 'jon wes', age: 41})");
            }

            // 3. query
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("test.user_info.find({name: 'mali'})")) {
                    if (rs.next()) {
                        String json = rs.getString("_JSON");
                        if (!json.contains("\"name\": \"mali\"") || !json.contains("\"age\": 26")) {
                            throw new RuntimeException("data not match: " + json);
                        }
                    } else {
                        throw new RuntimeException("no data found");
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(RealMongoTest.class);
    }
}
