/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcJoinQueryCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 680000;
    }

    @Test
    @Capability(CapabilityId.JDBC_JOIN_INNER)
    public void jdbcJoin_shouldReturnInnerJoinRowsAsMaps() throws SQLException {
        seedUsersAndOrders();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                "SELECT u.id, u.name, o.id AS order_id, o.amount FROM user_info u INNER JOIN user_order o ON u.id = o.user_id WHERE u.id = ? ORDER BY o.id", //
                new Object[] { baseId() + 1 });

        assertEquals(2, rows.size());
        assertEquals(baseId() + 1, ((Number) value(rows.get(0), "id")).intValue());
        assertEquals("NXN-Join-User-1", value(rows.get(0), "name"));
        assertNotNull(value(rows.get(0), "order_id"));
        assertNotNull(value(rows.get(0), "amount"));
    }

    @Test
    @Capability(CapabilityId.JDBC_JOIN_LEFT_NULL)
    public void jdbcJoin_shouldPreserveLeftJoinNulls() throws SQLException {
        requiresNxnFeature(FeatureId.LEFT_JOIN_NULL_VALUES);

        seedUsersAndOrders();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                "SELECT u.id, u.name, o.id AS order_id FROM user_info u LEFT JOIN user_order o ON u.id = o.user_id WHERE u.id = ? ORDER BY u.id", //
                new Object[] { baseId() + 10 });

        assertEquals(1, rows.size());
        assertEquals(baseId() + 10, ((Number) value(rows.get(0), "id")).intValue());
        assertNull(value(rows.get(0), "order_id"));
    }

    @Test
    @Capability(CapabilityId.JDBC_JOIN_RIGHT)
    public void jdbcJoin_shouldReturnRightJoinRows() throws SQLException {
        seedUsersAndOrders();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                "SELECT u.id, u.name, o.id AS order_id, o.amount FROM user_info u RIGHT JOIN user_order o ON u.id = o.user_id WHERE o.id BETWEEN ? AND ? ORDER BY o.id", //
                new Object[] { baseId() + 101, baseId() + 110 });

        assertEquals(10, rows.size());
        for (Map<String, Object> row : rows) {
            assertNotNull(value(row, "order_id"));
            assertNotNull(value(row, "amount"));
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_JOIN_CONDITION_ORDER)
    public void jdbcJoin_shouldHandleJoinConditionsAndOrdering() throws SQLException {
        seedUsersAndOrders();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                "SELECT u.id, u.name, o.id AS order_id, o.amount FROM user_info u INNER JOIN user_order o ON u.id = o.user_id AND o.amount > ? WHERE u.name LIKE ? ORDER BY o.amount DESC", //
                new Object[] { 500.0, "NXN-Join-User-%" });

        assertEquals(5, rows.size());
        double firstAmount = ((Number) value(rows.get(0), "amount")).doubleValue();
        double lastAmount = ((Number) value(rows.get(rows.size() - 1), "amount")).doubleValue();
        assertTrue(firstAmount >= lastAmount);
        assertTrue(lastAmount > 500.0);
    }

    @Test
    @Capability(CapabilityId.JDBC_JOIN_SELF)
    public void jdbcJoin_shouldSupportSelfJoinWithAliases() throws SQLException {
        seedSelfJoinUsers();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                """
                    SELECT u1.id AS user1_id, u1.name AS user1_name, u2.id AS user2_id, u2.name AS user2_name
                    FROM user_info u1 INNER JOIN user_info u2 ON u1.email = u2.email
                    WHERE u1.id < u2.id AND u1.id = ?
                    """, //
                new Object[] { baseId() + 20 });

        assertEquals(1, rows.size());
        assertEquals(baseId() + 20, ((Number) value(rows.get(0), "user1_id")).intValue());
        assertEquals(baseId() + 21, ((Number) value(rows.get(0), "user2_id")).intValue());
    }

    @Test
    @Capability(CapabilityId.JDBC_JOIN_CROSS)
    public void jdbcJoin_shouldSupportCrossJoin() throws SQLException {
        seedCrossJoinUsers();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                "SELECT u1.id AS id1, u2.id AS id2 FROM user_info u1 CROSS JOIN user_info u2 WHERE u1.id BETWEEN ? AND ? AND u2.id BETWEEN ? AND ? ORDER BY u1.id, u2.id", //
                new Object[] { baseId() + 30, baseId() + 32, baseId() + 30, baseId() + 32 });

        assertEquals(9, rows.size());
        assertEquals(baseId() + 30, ((Number) value(rows.get(0), "id1")).intValue());
        assertEquals(baseId() + 30, ((Number) value(rows.get(0), "id2")).intValue());
    }

    private void seedUsersAndOrders() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + i, "NXN-Join-User-" + i, 30 + i, "nxn-join-user-" + i + "@test.com", new Date() });
        }
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { baseId() + 10, "NXN-Join-NoOrder", 40, "nxn-join-noorder@test.com", new Date() });
        for (int i = 1; i <= 10; i++) {
            int userId = baseId() + 1 + (i - 1) % 5;
            jdbcTemplate.executeUpdate("INSERT INTO user_order (id, user_id, order_no, amount, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + 100 + i, userId, "NXN-ORDER-" + (baseId() + 100 + i), 100.0 * i, new Date() });
        }
    }

    private void seedSelfJoinUsers() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { baseId() + 20, "NXN-Join-Self-1", 41, "nxn-join-same@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { baseId() + 21, "NXN-Join-Self-2", 42, "nxn-join-same@test.com", new Date() });
    }

    private void seedCrossJoinUsers() throws SQLException {
        for (int i = 30; i <= 32; i++) {
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + i, "NXN-Join-Cross-" + i, 50 + i, "nxn-join-cross-" + i + "@test.com", new Date() });
        }
    }

    private Object value(Map<String, Object> row, String key) {
        assertNotNull(row);
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
