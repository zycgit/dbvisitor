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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterSupport;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MilvusIdentifierParameterContractTest extends JdbcParameterSupport {
    private final String table = "dbv_params_" + UUID.randomUUID().toString().replace("-", "");
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
            statement.executeUpdate("""
                    CREATE TABLE %s (
                        id INT64 PRIMARY KEY, name VARCHAR(128), age INT32 NULL,
                        email VARCHAR(128) NULL, create_time VARCHAR(128), v FLOAT_VECTOR(2)
                    ) WITH (consistency_level='Strong')
                    """.formatted(this.table));
            statement.executeUpdate("CREATE INDEX params_v ON " + this.table + "(v) USING FLAT WITH (metric_type=L2)");
            statement.executeUpdate("LOAD TABLE " + this.table);
        }
    }

    @After
    public void cleanupCollection() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection; Statement statement = closing.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS " + this.table);
            }
        }
    }

    @Override
    protected String sql(String statement) {
        String sql = statement.replace("user_info", this.table);
        if (sql.startsWith("INSERT INTO ")) {
            // Add the required vector literal without changing any bound value or its position.
            sql = sql.replace("create_time)", "create_time, v)");
            sql = sql.substring(0, sql.length() - 1) + ", [1, 0])";
        }
        return sql;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_IDENTIFIER_REPLACEMENT)
    public void identifierReplacement_shouldWorkWithoutScalarOrderingAndKeepValuesBound() throws SQLException {
        String name = "reader's text";
        insert(baseId() + 15, name, 34, "text@nxn.test");
        Map<String, Object> params = new HashMap<>();
        // Identifiers are trusted fixture values, not untrusted application input.
        params.put("tableName", this.table);
        params.put("column", "name");
        params.put("name", name);
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(
                "SELECT ${column}, age FROM ${tableName} WHERE ${column} = #{name}", params);
        assertEquals(1, rows.size());
        assertEquals(name, rows.get(0).get("name"));
        assertEquals(34, ((Number) rows.get(0).get("age")).intValue());
    }
}
