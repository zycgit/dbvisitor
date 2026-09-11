/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean.Address;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.handler.json.wrap.JsonType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JsonMapJdbcContractTest extends JsonTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_MAP)
    public void jsonObject_shouldReadAsMap() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object id = fixtureKey(baseId() + 4);
        JsonTestBean bean = new JsonTestBean("Frank", 40, true);
        bean.setAddress(new Address("Shenzhen", "Futian Road", "518000"));

        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", id, "bean", bean));

        Map loaded = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { id }, JsonType.jsonMap());

        assertTrue(loaded instanceof HashMap);
        assertEquals("Frank", loaded.get("name"));
        assertEquals(40, ((Number) loaded.get("age")).intValue());
        assertEquals(Boolean.TRUE, loaded.get("active"));
        assertTrue(loaded.get("address") instanceof Map);
    }
}
