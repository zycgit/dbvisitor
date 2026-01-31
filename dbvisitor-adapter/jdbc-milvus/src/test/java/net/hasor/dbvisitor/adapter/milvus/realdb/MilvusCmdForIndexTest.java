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
package net.hasor.dbvisitor.adapter.milvus.realdb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MilvusCmdForIndexTest extends AbstractMilvusCmdForTest {
    private String tableName;

    @Before
    public void setUp() {
        tableName = "tb_index_test_" + System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        if (tableName != null) {
            dropCollection(tableName);
        }
    }

    @Test
    public void testCreateIndex() throws Exception {
        try (Connection c = DriverManager.getConnection(MILVUS_URL); Statement s = c.createStatement()) {
            s.execute("create table " + tableName + " (id int64 primary key, vec float_vector(4))");
            s.execute("create index idx_01 on " + tableName + " (vec) using IVF_FLAT with (nlist = 128, metric_type = 'L2')");

            assert hasIndex(tableName, "idx_01");
        }
    }

    @Test
    public void testDropIndex() throws Exception {
        try (Connection c = DriverManager.getConnection(MILVUS_URL); Statement s = c.createStatement()) {
            // Setup
            s.execute("create table " + tableName + " (id int64 primary key, vec float_vector(4))");
            s.execute("create index idx_01 on " + tableName + " (vec) using IVF_FLAT with (nlist = 128, metric_type = 'L2')");
            assert hasIndex(tableName, "idx_01");

            // Test
            s.execute("drop index idx_01 on " + tableName);
            assert !hasIndex(tableName, "idx_01");
        }
    }

    @Test
    public void testShowIndex() throws Exception {
        try (Connection c = DriverManager.getConnection(MILVUS_URL); Statement s = c.createStatement()) {
            // Setup
            s.execute("create table " + tableName + " (id int64 primary key, vec float_vector(4))");
            s.execute("create index idx_01 on " + tableName + " (vec) using IVF_FLAT with (nlist = 128, metric_type = 'L2')");

            // Test
            try (ResultSet rs = s.executeQuery("show index idx_01 on " + tableName)) {
                assert rs.next();
                assert "idx_01".equalsIgnoreCase(rs.getString("INDEX"));
                assert "vec".equalsIgnoreCase(rs.getString("FIELD"));
            }
        }
    }

    @Test
    public void testShowIndexes() throws Exception {
        try (Connection c = DriverManager.getConnection(MILVUS_URL); Statement s = c.createStatement()) {
            // Setup
            s.execute("create table " + tableName + " (id int64 primary key, vec float_vector(4))");
            s.execute("create index idx_01 on " + tableName + " (vec) using IVF_FLAT with (nlist = 128, metric_type = 'L2')");

            // Test
            try (ResultSet rs = s.executeQuery("show indexes from " + tableName)) {
                boolean found = false;
                while (rs.next()) {
                    if ("idx_01".equalsIgnoreCase(rs.getString("INDEX"))) {
                        found = true;
                        assert "vec".equalsIgnoreCase(rs.getString("FIELD"));
                    }
                }
                assert found;
            }
        }
    }

    @Test
    public void testShowProgressIndex() throws Exception {
        try (Connection c = DriverManager.getConnection(MILVUS_URL); Statement s = c.createStatement()) {
            // Setup
            s.execute("create table " + tableName + " (id int64 primary key, vec float_vector(4))");
            s.execute("create index idx_01 on " + tableName + " (vec) using IVF_FLAT with (nlist = 128, metric_type = 'L2')");

            // Test
            try (ResultSet rs = s.executeQuery("show progress of index idx_01 on " + tableName)) {
                assert rs.next();
                // We just check if columns exist, values might be 0
                rs.getLong("TOTAL");
                rs.getLong("INDEXED");
            }
        }
    }
}
