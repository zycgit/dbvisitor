/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertNull;

@NxnContract
public abstract class JsonNullRowJdbcContractTest extends JsonTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_JSON_NULL_ROW)
    public void jsonNullColumns_shouldRemainNullInRowMap() throws SQLException {
        Object id = fixtureKey(baseId() + 13);
        jdbcTemplate.executeUpdate(insertCommand("id, json_varchar, json_mysql, nested_json", "?", "?", "?", "?"), //
                new Object[] { id, null, null, null });

        Map<String, Object> row = jdbcTemplate.queryForMap(selectCommand("json_varchar, json_mysql, nested_json"), new Object[] { id });
        assertNull(value(row, "json_varchar"));
        assertNull(value(row, "json_mysql"));
        assertNull(value(row, "nested_json"));
    }
}
