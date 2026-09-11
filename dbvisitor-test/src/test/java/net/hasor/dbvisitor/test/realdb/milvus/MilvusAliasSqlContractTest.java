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
import java.util.HashSet;
import java.util.Set;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

public class MilvusAliasSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ALIAS_LIFECYCLE)
    public void aliasLifecycle_shouldExposeAndQueryTheCurrentTarget() throws SQLException {
        String alias = this.collection + "_alias";
        String target = this.collection + "_next";
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        boolean aliasCreated = false;
        try (Statement statement = this.connection.createStatement()) {
            try {
                statement.executeUpdate("INSERT INTO " + this.collection + " (id, v) VALUES (1, [1, 0])");
                statement.executeUpdate("CREATE TABLE " + target + " (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)) WITH (consistency_level='Strong')");
                statement.executeUpdate("CREATE INDEX idx_v ON " + target + "(v) USING FLAT WITH (metric_type=L2)");
                statement.executeUpdate("LOAD TABLE " + target);
                statement.executeUpdate("INSERT INTO " + target + " (id, v) VALUES (2, [0, 1])");
                assertTrue(aliases(statement, this.collection).isEmpty());

                statement.executeUpdate("CREATE ALIAS " + alias + " FOR " + this.collection);
                aliasCreated = true;
                assertEquals(Set.of(alias), aliases(statement, this.collection));
                assertTarget(statement, alias, this.collection, 1L);

                statement.executeUpdate("ALTER ALIAS " + alias + " FOR " + target);
                assertTrue(aliases(statement, this.collection).isEmpty());
                assertEquals(Set.of(alias), aliases(statement, target));
                assertTarget(statement, alias, target, 2L);

                statement.executeUpdate("DROP ALIAS " + alias);
                aliasCreated = false;
                assertTrue(aliases(statement, target).isEmpty());
                assertThrows(SQLException.class, () -> statement.executeQuery("SHOW ALIAS " + alias));
            } finally {
                try {
                    if (aliasCreated) {
                        statement.executeUpdate("DROP ALIAS " + alias);
                    }
                } finally {
                    statement.executeUpdate("DROP TABLE IF EXISTS " + target);
                }
            }
        }
    }

    private Set<String> aliases(Statement statement, String target) throws SQLException {
        Set<String> aliases = new HashSet<>();
        try (ResultSet result = statement.executeQuery("SHOW ALIASES FROM " + target)) {
            assertEquals(2, result.getMetaData().getColumnCount());
            while (result.next()) {
                assertEquals(target, result.getString("TABLE"));
                assertTrue(aliases.add(result.getString("ALIAS")));
            }
        }
        return aliases;
    }

    private void assertTarget(Statement statement, String alias, String target, long id) throws SQLException {
        try (ResultSet result = statement.executeQuery("SHOW ALIAS " + alias)) {
            assertTrue(result.next());
            assertEquals("default", result.getString("DATABASE"));
            assertEquals(alias, result.getString("ALIAS"));
            assertEquals(target, result.getString("TABLE"));
            assertFalse(result.next());
        }
        try (ResultSet result = statement.executeQuery("SELECT id FROM " + alias)) {
            assertTrue(result.next());
            assertEquals(id, result.getLong("id"));
            assertFalse(result.next());
        }
    }
}
