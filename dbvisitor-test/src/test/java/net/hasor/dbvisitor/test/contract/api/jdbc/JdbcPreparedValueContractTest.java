/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** JDBC 值绑定契约；表定义和必填字段由具体数据源提供。 */
@NxnContract
public abstract class JdbcPreparedValueContractTest extends AdapterContractTest {
    protected final String table = "dbv_bound_" + UUID.randomUUID().toString().replace("-", "");
    protected Connection connection;
    private static final String HOSTILE = "引号 \" OR id > 0 OR name = \"x'; -- \\ 换行\n下一行";

    protected abstract void createFixture() throws SQLException;

    protected abstract String insertSql();

    @Before
    public void prepareValues() throws SQLException {
        this.connection = newAdapterConnection();
        createFixture();
        try (PreparedStatement insert = this.connection.prepareStatement(insertSql())) {
            String[] names = { HOSTILE, "ordinary", "", null };
            for (int i = 0; i < names.length; i++) {
                insert.setLong(1, i + 1);
                insert.setString(2, names[i]);
                assertEquals(1, insert.executeUpdate());
            }
        }
    }

    @After
    public void cleanupValues() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection; Statement statement = closing.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS " + this.table);
            }
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_BOUND_STRING_LITERAL)
    public void setString_shouldTreatExpressionCharactersAsData() throws SQLException {
        try (PreparedStatement query = selectByName()) {
            query.setString(1, HOSTILE);
            assertOnlyRow(query, 1, HOSTILE);
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_BOUND_OBJECT_LITERAL)
    public void setObject_shouldTreatExpressionCharactersAsData() throws SQLException {
        try (PreparedStatement query = selectByName()) {
            query.setObject(1, HOSTILE);
            assertOnlyRow(query, 1, HOSTILE);
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_BOUND_TYPED_LITERAL)
    public void setObjectWithJdbcType_shouldTreatExpressionCharactersAsData() throws SQLException {
        try (PreparedStatement query = selectByName()) {
            query.setObject(1, HOSTILE, Types.VARCHAR);
            assertOnlyRow(query, 1, HOSTILE);
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_BOUND_REUSE)
    public void reusedStatement_shouldReplaceValuesWithoutLeakingOldFilter() throws SQLException {
        try (PreparedStatement query = selectByName()) {
            query.setString(1, HOSTILE);
            assertOnlyRow(query, 1, HOSTILE);
            query.setString(1, "ordinary");
            assertOnlyRow(query, 2, "ordinary");
            query.clearParameters();
            query.setString(1, "absent");
            try (ResultSet result = query.executeQuery()) {
                assertFalse(result.next());
            }
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_BOUND_NULL_EMPTY)
    public void nullAndEmptyString_shouldRemainDistinct() throws SQLException {
        try (PreparedStatement query = selectByName()) {
            query.setString(1, "");
            assertOnlyRow(query, 3, "");
        }
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id, name FROM " + this.table + " WHERE name IS NULL")) {
            assertTrue(result.next());
            assertEquals(4, result.getLong("id"));
            assertNull(result.getString("name"));
            assertTrue(result.wasNull());
            assertFalse(result.next());
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_BOUND_MUTATION_LITERAL)
    public void updateAndDelete_shouldNotBroadenBoundFilter() throws SQLException {
        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + this.table + " SET name = ? WHERE name = ?")) {
            update.setString(1, "changed");
            update.setString(2, HOSTILE);
            assertEquals(1, update.executeUpdate());
        }
        try (PreparedStatement delete = this.connection.prepareStatement("DELETE FROM " + this.table + " WHERE name = ?")) {
            delete.setString(1, HOSTILE);
            assertMutationRows(0, delete.executeUpdate());
            delete.setObject(1, "changed", Types.VARCHAR);
            assertEquals(1, delete.executeUpdate());
        }
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id FROM " + this.table)) {
            Set<Long> ids = new HashSet<>();
            while (result.next()) {
                assertTrue(ids.add(result.getLong(1)));
            }
            assertEquals(Set.of(2L, 3L, 4L), ids);
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_FETCH_SIZE_LIMIT)
    public void fetchSize_shouldNotLimitTotalRowsAndMaxRowsShould() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.setFetchSize(2);
            try (ResultSet result = statement.executeQuery("SELECT id FROM " + this.table)) {
                Set<Long> ids = new HashSet<>();
                while (result.next()) {
                    assertTrue(ids.add(result.getLong(1)));
                }
                assertEquals(Set.of(1L, 2L, 3L, 4L), ids);
            }
            statement.setMaxRows(3);
            try (ResultSet result = statement.executeQuery("SELECT id FROM " + this.table)) {
                Set<Long> ids = new HashSet<>();
                while (result.next()) {
                    assertTrue(ids.add(result.getLong(1)));
                }
                assertEquals(3, ids.size());
            }
        }
    }

    private PreparedStatement selectByName() throws SQLException {
        return this.connection.prepareStatement("SELECT id, name FROM " + this.table + " WHERE name = ?");
    }

    private void assertOnlyRow(PreparedStatement query, long id, String name) throws SQLException {
        try (ResultSet result = query.executeQuery()) {
            assertTrue(result.next());
            assertEquals(id, result.getLong("id"));
            assertEquals(name, result.getString("name"));
            assertFalse(result.next());
        }
    }
}
