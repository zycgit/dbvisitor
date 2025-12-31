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
package net.hasor.realdb.elastic;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticCommandTest {
    @Test
    public void testGetSet() throws Exception {
        try (Connection conn = DsUtils.es7Conn()) {
            try (Statement s = conn.createStatement()) {
                // 1. clean
                try {
                    s.execute("DELETE /test_user_info");
                } catch (Exception e) {
                    // ignore
                }

                // 2. insert
                s.execute("POST /test_user_info/_doc/1 { \"name\": \"mali\", \"age\": 26 }");

                // 3. query
                try (ResultSet rs = s.executeQuery("POST /test_user_info/_search { \"query\": { \"match\": { \"name\": \"mali\" } } }")) {
                    if (rs.next()) {
                        String id = rs.getString("_ID");
                        String doc = rs.getString("_DOC");
                        String name = rs.getString("name");
                        int age = rs.getInt("age");

                        assertEquals("id not match", "1", id);
                        assertTrue("doc not match (name): " + doc, doc.contains("\"name\":\"mali\"") || doc.contains("\"name\": \"mali\""));
                        assertTrue("doc not match (age): " + doc, doc.contains("\"age\":26") || doc.contains("\"age\": 26"));
                        assertEquals("name not match", "mali", name);
                        assertEquals("age not match", 26, age);
                    } else {
                        fail("no data found");
                    }
                }
            }
        }
    }
}
