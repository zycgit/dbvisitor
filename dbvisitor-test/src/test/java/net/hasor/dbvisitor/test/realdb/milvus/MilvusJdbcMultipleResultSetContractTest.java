/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Binds the unchanged public multi-result assertions to a Milvus collection fixture. */
public class MilvusJdbcMultipleResultSetContractTest extends JdbcMultipleResultSetContractTest {
    private final String table = "dbv_multi_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        String env = OneApiDataSourceManager.getDbDialect();
        Assume.assumeTrue("Milvus contract is not selected for " + env, profile().env().equals(env));
        this.connection = OneApiDataSourceManager.getConnection(profile().env());
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        try (Statement statement = this.connection.createStatement()) {
            statement.execute("CREATE TABLE " + this.table
                    + " (id INT64 PRIMARY KEY, name VARCHAR(128), age INT32, email VARCHAR(128), v FLOAT_VECTOR(2))"
                    + " WITH (consistency_level='Strong')");
            statement.execute("CREATE INDEX multi_v ON " + this.table + "(v) USING FLAT WITH (metric_type=L2)");
            statement.execute("LOAD TABLE " + this.table);
        }
    }

    @After
    public void cleanupCollection() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection; Statement statement = closing.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS " + this.table);
            }
        }
    }

    @Override
    protected String tableName() {
        return this.table;
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO " + this.table + " (id, name, age, email, v) VALUES (?, ?, ?, ?, [1, 0])";
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_MULTIPLE_POSITIONAL)
    public void nativeComparisons_shouldBindAcrossStatementsAndKeepInclusiveRanges() throws SQLException {
        seedUsers();
        String sql = """
                SELECT id, name, age FROM %s WHERE id BETWEEN ? AND ?;
                SELECT id, name, age FROM %s WHERE name = ?;
                """.formatted(this.table, this.table);
        Map<String, Object> results = this.jdbcTemplate.multipleExecute(sql,
                new Object[] { baseId() + 2, baseId() + 3, "NXN-Multi-2" });
        List<Object> sets = new ArrayList<>(results.values());
        assertEquals(2, sets.size());
        assertEquals(2, ((List<?>) sets.get(0)).size());
        List<?> selected = (List<?>) sets.get(1);
        assertEquals(1, selected.size());
        assertEquals("NXN-Multi-2", ((Map<?, ?>) selected.get(0)).get("name"));
        List<Integer> ids = this.jdbcTemplate.queryForList("SELECT id FROM " + this.table + " WHERE id NOT BETWEEN ? AND ?",
                new Object[] { baseId() + 2, baseId() + 3 }, Integer.class);
        assertEquals(List.of(baseId() + 1), ids);
        assertTrue(this.jdbcTemplate.queryForList("SELECT id FROM " + this.table + " WHERE id BETWEEN ? AND ?",
                new Object[] { baseId() + 3, baseId() + 1 }).isEmpty());
    }
}
