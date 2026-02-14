/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import org.junit.Test;

public class MongoCommandTest {
    @Test
    public void testGetSet() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection("mongo")) {
            try (Statement s = conn.createStatement()) {
                // 1. clean
                try {
                    s.execute("test.user_info.drop()");
                } catch (Exception e) {
                    // ignore
                }

                // 2. insert
                s.execute("test.user_info.insert({name: 'mali', age: 26})");

                // 3. query
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
}
