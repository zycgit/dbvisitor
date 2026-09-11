/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusTruncateSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_TRUNCATE)
    public void truncateShouldPreserveSchemaIndexAndAliasAndPermitNewWrites() throws SQLException {
        prepareCollection();
        String alias = this.collection + "_alias";
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("CREATE ALIAS " + alias + " FOR " + this.collection);
            try {
                String schema = createScript();
                assertEquals(0, statement.executeUpdate("TRUNCATE TABLE " + this.collection));
                assertEquals(schema, createScript());
                assertIndexPresent(statement);
                // Loading is explicit; preserving an index does not promise preserved load state.
                loadCollection();
                assertEquals(0, countRows(""));
                assertEquals(Long.valueOf(0), this.jdbcTemplate.queryForLong("COUNT FROM " + alias));
                statement.executeUpdate("INSERT INTO " + this.collection + " (id,v) VALUES (3,[1,0])");
                try (ResultSet rows = statement.executeQuery("SELECT id FROM " + alias + " ORDER BY v <-> [1,0] LIMIT 1")) {
                    assertTrue(rows.next());
                    assertEquals(3L, rows.getLong("id"));
                    assertFalse(rows.next());
                }
                assertEquals(1, countRows(""));
            } finally {
                statement.executeUpdate("DROP ALIAS " + alias);
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_TRUNCATE_UNAVAILABLE)
    public void baselineMissingApiShouldPreserveCollectionAndStopFollowingMutation() throws SQLException {
        prepareCollection();
        String catalog = this.connection.getCatalog();
        String schema = createScript();
        try (Statement statement = this.connection.createStatement()) {
            SQLException failure = assertThrows(SQLException.class, () -> statement.execute(
                    "TRUNCATE TABLE " + this.collection + " IN DATABASE " + catalog + "; DELETE FROM " + this.collection));
            assertTrue(failure.getMessage(), failure.getMessage().contains("UNIMPLEMENTED"));
            assertEquals(catalog, this.connection.getCatalog());
            assertEquals(schema, createScript());
            assertIndexPresent(statement);
            assertEquals(2, countRows(""));
            assertEquals(1, statement.executeUpdate("DELETE FROM " + this.collection + " WHERE id=1"));
            assertEquals(1, countRows(" WHERE id=2"));
        }
    }

    private String createScript() throws SQLException {
        return this.jdbcTemplate.queryForObject("SHOW CREATE TABLE " + this.collection,
                (rows, rowNum) -> rows.getString("CREATE SCRIPT"));
    }

    private void assertIndexPresent(Statement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery("SHOW INDEXES FROM " + this.collection)) {
            assertTrue(result.next());
            assertEquals("idx_v", result.getString("INDEX"));
            assertFalse(result.next());
        }
    }

    private void prepareCollection() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,v) VALUES (1,[1,0]),(2,[0,1])");
    }
}
