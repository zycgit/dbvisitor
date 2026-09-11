/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcMultipleResultSetContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 690000;
    }

    protected String tableName() {
        return "user_info";
    }

    protected String insertSql() {
        return "INSERT INTO " + tableName() + " (id, name, age, email) VALUES (?, ?, ?, ?)";
    }

    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_RESULT_SETS)
    public void jdbcMultipleExecute_shouldReturnMultipleResultSets() throws SQLException {
        requiresNxnFeature(FeatureId.MULTIPLE_RESULT_SETS);
        seedUsers();

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(//
                """
                SELECT id, name, age FROM %s WHERE id BETWEEN %d AND %d;
                SELECT id, name, age FROM %s WHERE id BETWEEN %d AND %d;
                """.formatted(tableName(), baseId() + 1, baseId() + 1, tableName(), baseId() + 2, baseId() + 3));
        List<Object> resultList = new ArrayList<>(resultMap.values());

        assertEquals(2, resultList.size());
        assertEquals(1, ((List<?>) resultList.get(0)).size());
        assertEquals(2, ((List<?>) resultList.get(1)).size());
    }

    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_POSITIONAL)
    public void jdbcMultipleExecute_shouldBindPositionalParametersAcrossStatements() throws SQLException {
        requiresNxnFeature(FeatureId.MULTIPLE_RESULT_SETS);
        seedUsers();

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(//
                """
                SELECT id, name, age FROM %s WHERE age > ?;
                SELECT id, name, age FROM %s WHERE name = ?;
                """.formatted(tableName(), tableName()), //
                new Object[] { 31, "NXN-Multi-2" });
        List<Object> resultList = new ArrayList<>(resultMap.values());

        assertEquals(2, resultList.size());
        assertEquals(2, ((List<?>) resultList.get(0)).size());
        List<?> namedRows = (List<?>) resultList.get(1);
        assertEquals(1, namedRows.size());
        assertEquals("NXN-Multi-2", value((Map<?, ?>) namedRows.get(0), "name"));
    }

    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_NAMED)
    public void jdbcMultipleExecute_shouldBindNamedParametersAcrossStatements() throws SQLException {
        requiresNxnFeature(FeatureId.MULTIPLE_RESULT_SETS);
        seedUsers();
        Map<String, Object> params = new HashMap<>();
        params.put("ageLimit", 32);

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(//
                """
                SELECT id, name, age FROM %s WHERE age < :ageLimit;
                SELECT id, name, age FROM %s WHERE age >= :ageLimit;
                """.formatted(tableName(), tableName()), //
                params);
        List<Object> resultList = new ArrayList<>(resultMap.values());

        assertEquals(2, resultList.size());
        assertEquals(1, ((List<?>) resultList.get(0)).size());
        assertEquals(2, ((List<?>) resultList.get(1)).size());
    }

    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_RESULTSET_RULE)
    public void jdbcMultipleExecute_shouldNameAndMapResultSetsWithRule() throws SQLException {
        requiresNxnFeature(FeatureId.MULTIPLE_RESULT_SETS);
        seedUsers();

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(//
                """
                SELECT * FROM %s WHERE id = %d; @{resultSet,name=youngUsers,javaType=net.hasor.dbvisitor.test.contract.material.model.UserInfo}
                SELECT * FROM %s WHERE id = %d; @{resultSet,name=seniorUsers,javaType=net.hasor.dbvisitor.test.contract.material.model.UserInfo}
                """.formatted(tableName(), baseId() + 1, tableName(), baseId() + 3));

        assertTrue(resultMap.containsKey("youngUsers"));
        assertTrue(resultMap.containsKey("seniorUsers"));
        List<?> youngUsers = (List<?>) resultMap.get("youngUsers");
        List<?> seniorUsers = (List<?>) resultMap.get("seniorUsers");
        assertEquals(1, youngUsers.size());
        assertEquals(1, seniorUsers.size());
        assertTrue(youngUsers.get(0) instanceof UserInfo);
        assertTrue(seniorUsers.get(0) instanceof UserInfo);
        assertEquals("NXN-Multi-1", ((UserInfo) youngUsers.get(0)).getName());
        assertEquals("NXN-Multi-3", ((UserInfo) seniorUsers.get(0)).getName());
    }

    protected void seedUsers() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(insertSql(), //
                    new Object[] { baseId() + i, "NXN-Multi-" + i, 30 + i, "nxn-multi-" + i + "@test.com" });
        }
    }

    private Object value(Map<?, ?> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
