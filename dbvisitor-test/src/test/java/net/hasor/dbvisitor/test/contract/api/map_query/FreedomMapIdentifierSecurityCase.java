/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.map_query;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class FreedomMapIdentifierSecurityCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 790000;
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_QUERY)
    public void freedomIdentifier_shouldNotExpandQueryWhenColumnNameContainsPayload() throws SQLException {
        insertUser(baseId() + 1, "FreedomIdVictim1", 25);
        insertUser(baseId() + 2, "FreedomIdVictim2", 30);

        String maliciousEqColumn = closeIdentifier("id") + " = " + (baseId() + 1) + " OR 1=1 --";
        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom("user_info")//
                    .eq(maliciousEqColumn, "irrelevant")//
                    .queryForList();
            assertTrue("Column identifier payload must not expand query results: " + result, result.size() <= 1);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }

        String maliciousLikeColumn = closeIdentifier("name") + " = 'FreedomIdVictim1' OR " + openIdentifier("name");
        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom("user_info")//
                    .like(maliciousLikeColumn, "FreedomId%")//
                    .queryForList();
            assertTrue("LIKE column identifier payload must not bypass predicates: " + result, result.size() <= 1);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }

        assertEquals("FreedomIdVictim1", loadName(baseId() + 1));
        assertEquals("FreedomIdVictim2", loadName(baseId() + 2));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_SUBQUERY)
    public void freedomIdentifier_shouldRejectSubqueryAsColumnIdentifier() throws SQLException {
        insertUser(baseId() + 3, "FreedomSubquerySafe", 25);

        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom("user_info")//
                    .eq("(SELECT name FROM user_info LIMIT 1)", "FreedomSubquerySafe")//
                    .queryForList();
            fail("Subquery column identifier should be rejected, returned " + result.size() + " rows");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }

        assertEquals("FreedomSubquerySafe", loadName(baseId() + 3));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_ORDER_GROUP)
    public void freedomIdentifier_shouldKeepOrderAndGroupPayloadsNonDestructive() throws SQLException {
        insertUser(baseId() + 11, "FreedomSort1", 25);
        insertUser(baseId() + 12, "FreedomSort2", 30);

        String maliciousOrder = closeIdentifier("id") + "; DROP TABLE user_info; --";
        try {
            lambdaTemplate.queryFreedom("user_info")//
                    .like("name", "FreedomSort%")//
                    .asc(maliciousOrder)//
                    .queryForList();
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertTableSurvived(baseId() + 11);

        String maliciousGroup = closeIdentifier("age") + "; DROP TABLE user_info; --";
        try {
            lambdaTemplate.queryFreedom("user_info")//
                    .like("name", "FreedomSort%")//
                    .groupBy(maliciousGroup)//
                    .queryForList();
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertTableSurvived(baseId() + 12);
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_SAMPLE_MAP)
    public void freedomIdentifier_shouldNotTrustEqBySampleMapKeys() throws SQLException {
        insertUser(baseId() + 21, "FreedomMapVictim1", 25);
        insertUser(baseId() + 22, "FreedomMapVictim2", 30);

        Map<String, Object> expandedSample = new LinkedHashMap<>();
        expandedSample.put(closeIdentifier("id") + " = " + (baseId() + 21) + " OR " + openIdentifier("id"), baseId() + 21);
        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom("user_info")//
                    .eqBySampleMap(expandedSample)//
                    .queryForList();
            assertTrue("eqBySampleMap key payload must not expand query results: " + result, result.size() <= 1);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }

        Map<String, Object> alwaysTrueSample = new LinkedHashMap<>();
        alwaysTrueSample.put(closeIdentifier("1") + " = 1 OR " + openIdentifier("1"), 1);
        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom("user_info")//
                    .eqBySampleMap(alwaysTrueSample)//
                    .queryForList();
            assertTrue("eqBySampleMap always-true key must not return rows: " + result, result.isEmpty());
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }

        assertEquals("FreedomMapVictim1", loadName(baseId() + 21));
        assertEquals("FreedomMapVictim2", loadName(baseId() + 22));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_UPDATE)
    public void freedomIdentifier_shouldNotAllowUpdateColumnPayloadsToChangeExtraData() throws SQLException {
        insertUser(baseId() + 31, "FreedomUpdate", 25);

        String maliciousColumn = closeIdentifier("name") + " = 'HACKED', " + openIdentifier("age") + " = 999 --";
        try {
            lambdaTemplate.updateFreedom("user_info")//
                    .eq("id", baseId() + 31)//
                    .updateTo(maliciousColumn, "irrelevant")//
                    .doUpdate();
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertUserUnchanged(baseId() + 31, "FreedomUpdate", 25);

        Map<String, Object> maliciousMap = new LinkedHashMap<>();
        maliciousMap.put(closeIdentifier("name") + " = 'HACKED' WHERE 1=1 --", "irrelevant");
        try {
            lambdaTemplate.updateFreedom("user_info")//
                    .eq("id", baseId() + 31)//
                    .updateToSampleMap(maliciousMap)//
                    .doUpdate();
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertUserUnchanged(baseId() + 31, "FreedomUpdate", 25);
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_INSERT)
    public void freedomIdentifier_shouldNotAllowInsertMapKeysToInjectColumnsOrValues() throws SQLException {
        Map<String, Object> maliciousMap = new LinkedHashMap<>();
        maliciousMap.put("id", baseId() + 41);
        maliciousMap.put(closeIdentifier("name") + ", age) VALUES (" + (baseId() + 41) + ", 'HACKED', 999); --", "irrelevant");

        try {
            lambdaTemplate.insertFreedom("user_info")//
                    .applyMap(maliciousMap)//
                    .executeSumResult();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }

        UserInfo loaded = loadUserOrNull(baseId() + 41);
        if (loaded != null) {
            assertFalse("INSERT key payload must not inject name", "HACKED".equals(loaded.getName()));
            assertFalse("INSERT key payload must not inject age", Integer.valueOf(999).equals(loaded.getAge()));
        }
        assertTableReadable();
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_TABLE)
    public void freedomIdentifier_shouldNotAllowTableNamePayloadsToUnionRows() throws SQLException {
        insertUser(baseId() + 51, "FreedomTableSafe", 25);

        String maliciousTable = closeIdentifier("user_info")//
                + " UNION SELECT id, name, age, email, create_time FROM " + openIdentifier("user_info");
        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom(maliciousTable)//
                    .queryForList();
            assertTrue("Table identifier payload must not return rows: " + result, result.isEmpty());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }

        assertEquals("FreedomTableSafe", loadName(baseId() + 51));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_IDENTIFIER_SELECT)
    public void freedomIdentifier_shouldNotAllowSelectColumnPayloadsToLeakCalculatedAliases() throws SQLException {
        insertUser(baseId() + 61, "FreedomSelectSafe", 25);

        String maliciousColumn = closeIdentifier("id") + ", (SELECT count(*) FROM user_info) as leaked_count --";
        try {
            List<Map<String, Object>> result = lambdaTemplate.queryFreedom("user_info")//
                    .select(maliciousColumn)//
                    .eq("id", baseId() + 61)//
                    .queryForList();
            if (!result.isEmpty()) {
                assertFalse("SELECT identifier payload must not expose injected alias: " + result, result.get(0).containsKey("leaked_count"));
            }
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }

        assertEquals("FreedomSelectSafe", loadName(baseId() + 61));
    }

    private void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, id + "@freedom-id.test", new Date() });
    }

    private UserInfo loadUserOrNull(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject();
    }

    private String loadName(int id) throws SQLException {
        UserInfo user = loadUserOrNull(id);
        assertNotNull("Expected user_info row id=" + id, user);
        return user.getName();
    }

    private void assertUserUnchanged(int id, String name, Integer age) throws SQLException {
        UserInfo user = loadUserOrNull(id);
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(age, user.getAge());
    }

    private void assertTableSurvived(int id) throws SQLException {
        try {
            assertTrue("user_info should remain readable after identifier payload", lambdaTemplate.query(UserInfo.class)//
                    .eq(UserInfo::getId, id)//
                    .queryForCount() >= 1);
        } catch (SQLException e) {
            fail("user_info should survive identifier payload: " + e.getMessage());
        }
    }

    private void assertTableReadable() throws SQLException {
        try {
            lambdaTemplate.query(UserInfo.class)//
                    .eq(UserInfo::getId, baseId() + 41)//
                    .queryForCount();
        } catch (SQLException e) {
            fail("user_info should remain readable after INSERT identifier payload: " + e.getMessage());
        }
    }

    private String closeIdentifier(String name) {
        return name + qualifier();
    }

    private String openIdentifier(String name) {
        return qualifier() + name;
    }

    private String qualifier() {
        String qualifier = profile().rightQualifier();
        return qualifier == null || qualifier.isEmpty() ? "\"" : qualifier;
    }
}
