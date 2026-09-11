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

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class MappedMapCrudContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 621000;
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_MAPPED_CRUD_INSERT)
    public void mappedMapInsert_shouldPersistMapThroughEntityMapping() throws SQLException {
        int id = baseId() + 1;

        int rows = lambdaTemplate.insert(UserInfo.class)//
                .asMap()//
                .applyMap(userMap(id, "NXN-Mapped-Map-Insert", 31, "nxn-mapped-map-insert@test.com"))//
                .executeSumResult();

        assertEquals(1, rows);
        assertEquals("NXN-Mapped-Map-Insert", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_MAPPED_CRUD_QUERY)
    public void mappedMapQuery_shouldReadPropertiesAsMapKeys() throws SQLException {
        int id = baseId() + 2;
        insertByJdbc(id, "NXN-Mapped-Map-Query", 32, "nxn-mapped-map-query@test.com");

        Map<String, Object> loaded = lambdaTemplate.query(UserInfo.class)//
                .asMap()//
                .select("id", "name", "age", "createTime")//
                .eq("id", id)//
                .queryForMap();

        assertNotNull(loaded);
        assertEquals(id, ((Number) value(loaded, "id")).intValue());
        assertEquals("NXN-Mapped-Map-Query", value(loaded, "name"));
        assertEquals(32, ((Number) value(loaded, "age")).intValue());
        assertNotNull(value(loaded, "createTime"));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_MAPPED_CRUD_UPDATE)
    public void mappedMapUpdate_shouldUsePropertyNamesForColumns() throws SQLException {
        int id = baseId() + 3;
        insertByJdbc(id, "NXN-Mapped-Map-Update", 33, "nxn-mapped-map-update@test.com");

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "NXN-Mapped-Map-Updated");
        updates.put("age", 34);

        int rows = lambdaTemplate.update(UserInfo.class)//
                .asMap()//
                .eq("id", id)//
                .updateToSampleMap(updates)//
                .doUpdate();

        assertEquals(1, rows);
        assertEquals("NXN-Mapped-Map-Updated", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { id }));
        assertEquals(Integer.valueOf(34), jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.MAP_QUERY_MAPPED_CRUD_DELETE)
    public void mappedMapDelete_shouldRemoveByMappedPropertyCondition() throws SQLException {
        int id = baseId() + 4;
        insertByJdbc(id, "NXN-Mapped-Map-Delete", 35, "nxn-mapped-map-delete@test.com");

        int rows = lambdaTemplate.delete(UserInfo.class)//
                .asMap()//
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
        map.put("createTime", new Date());
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
