/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.handler.json.wrap.JsonType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JsonListJdbcCase extends JsonTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_LIST)
    public void jsonArray_shouldReadAsList() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object id = fixtureKey(baseId() + 5);
        List<Map<String, Object>> list = Arrays.asList(//
                CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95), //
                CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88));

        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", id, "list", list));

        List loaded = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { id }, JsonType.jsonList());

        assertTrue(loaded instanceof ArrayList);
        assertEquals(2, loaded.size());
        assertTrue(loaded.get(0) instanceof Map);
        assertEquals("Alice", ((Map) loaded.get(0)).get("name"));
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_BEAN_LIST)
    public void jsonBeanArray_shouldReadAsList() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object beanListId = fixtureKey(baseId() + 7);
        // @formatter:off
        List<JsonTestBean> beanList = Arrays.asList(
            new JsonTestBean("George", 29, true),
            new JsonTestBean("Helen", 31, false),
            new JsonTestBean("Ivan", 27, true)
        );
        // @formatter:on
        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", beanListId, "list", beanList));
        List loadedList = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { beanListId }, JsonType.jsonList());

        assertTrue(loadedList instanceof ArrayList);
        assertEquals(3, loadedList.size());
        assertJsonElementName(loadedList.get(0), "George");
    }
}
