/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean.Address;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JsonSerializationJdbcContractTest extends JsonTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_JSON_WRITE_OBJECT)
    public void jsonObject_shouldSerializeToStringColumn() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object id = fixtureKey(baseId() + 1);
        JsonTestBean bean = new JsonTestBean("Alice", 30, true);
        bean.setAddress(new Address("Shenzhen", "Futian Road", "518000"));

        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", id, "bean", bean));

        String json = storedJson(id);

        assertNotNull(json);
        assertTrue(json.contains("Alice"));
        assertTrue(json.contains("30"));
        assertTrue(json.contains("Shenzhen"));
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_WRITE_SPECIAL)
    public void jsonObject_shouldSerializeNullFieldsSpecialCharactersAndEmptyObject() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object specialId = fixtureKey(baseId() + 2);
        Object emptyId = fixtureKey(baseId() + 3);
        JsonTestBean special = new JsonTestBean();
        special.setName("中文名字");
        special.setAge(28);
        special.setActive(true);
        special.setTags(Arrays.asList("emoji😀", "quote\"test\"", "backslash\\path"));
        special.setAddress(new Address("上海", "南京路123号", "200000"));
        JsonTestBean empty = new JsonTestBean();

        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", specialId, "bean", special));
        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", emptyId, "bean", empty));

        String specialJson = storedJson(specialId);
        String emptyJson = storedJson(emptyId);

        assertNotNull(specialJson);
        assertTrue(specialJson.contains("中文名字") || specialJson.contains("\\u4e2d"));
        assertTrue(specialJson.contains("上海") || specialJson.contains("\\u4e0a"));
        assertTrue(specialJson.contains("28"));
        assertNotNull(emptyJson);
        assertTrue(emptyJson.contains("{") && emptyJson.contains("}"));
    }
}
