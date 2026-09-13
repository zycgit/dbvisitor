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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.handler.json.wrap.JsonType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JsonSetJdbcCase extends JsonTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_SET)
    public void jsonArray_shouldReadAsSet() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object setId = fixtureKey(baseId() + 6);
        // @formatter:off
        List<Map<String, Object>> setSource = Arrays.asList(
            CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95),
            CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88),
            CollectionUtils.asMap("id", 3, "name", "Charlie", "score", 92)
        );
        // @formatter:on

        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}"), //
                CollectionUtils.asMap("id", setId, "list", setSource));

        Set loadedSet = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { setId }, JsonType.jsonSet());

        assertNotNull(loadedSet);
        assertEquals(3, loadedSet.size());
        assertJsonSetContainsName(loadedSet, "Alice");
        assertJsonSetContainsName(loadedSet, "Bob");
    }
}
