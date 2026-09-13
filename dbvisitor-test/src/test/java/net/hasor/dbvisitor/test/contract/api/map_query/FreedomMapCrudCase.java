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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class FreedomMapCrudCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 620000;
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_CRUD_INSERT)
    public void lambdaFreedomInsert_shouldPersistOneUser() throws SQLException {
        int id = baseId() + 11;
        int rows = lambdaTemplate.insertFreedom("user_info")//
                .applyMap(userMap(id, "NXN-Lambda-Freedom-Insert", 51, "nxn-lambda-freedom-insert@test.com"))//
                .executeSumResult();

        assertEquals(1, rows);
        assertEquals("NXN-Lambda-Freedom-Insert", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_CRUD_QUERY)
    public void lambdaFreedomQuery_shouldReadUserById() throws SQLException {
        int id = baseId() + 12;
        insertByJdbc(id, "NXN-Lambda-Freedom-Query", 52, "nxn-lambda-freedom-query@test.com");

        Map<String, Object> loaded = lambdaTemplate.queryFreedom("user_info")//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("NXN-Lambda-Freedom-Query", value(loaded, "name"));
        assertEquals(52, ((Number) value(loaded, "age")).intValue());
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_CRUD_UPDATE)
    public void lambdaFreedomUpdate_shouldChangeUser() throws SQLException {
        int id = baseId() + 13;
        insertByJdbc(id, "NXN-Lambda-Freedom-Update", 53, "nxn-lambda-freedom-update@test.com");

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "nxn-lambda-freedom-updated@test.com");

        int rows = lambdaTemplate.updateFreedom("user_info")//
                .eq("id", id)//
                .updateToSample(updates)//
                .doUpdate();

        assertEquals(1, rows);
        assertEquals("nxn-lambda-freedom-updated@test.com", jdbcTemplate.queryForString("SELECT email FROM user_info WHERE id = ?", new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_FREEDOM_CRUD_DELETE)
    public void lambdaFreedomDelete_shouldRemoveUser() throws SQLException {
        int id = baseId() + 14;
        insertByJdbc(id, "NXN-Lambda-Freedom-Delete", 55, "nxn-lambda-freedom-delete@test.com");

        int rows = lambdaTemplate.deleteFreedom("user_info")//
                .eq("id", id)//
                .doDelete();

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    private void insertByJdbc(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private Map<String, Object> userMap(int id, String name, int age, String email) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("age", age);
        map.put("email", email);
        map.put("create_time", new Date());
        return map;
    }

    private Object value(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        return map.get(key.toLowerCase());
    }
}
