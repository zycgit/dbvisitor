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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class BasicEmptyStringJdbcContractTest extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_CHARACTER_EMPTY)
    public void basicEmptyString_shouldPreserveItsDatabaseMeaning() throws SQLException {
        int emptyId = baseId() + 7;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_explicit_test", "id, char_value, varchar_value, nvarchar_value"),
                new Object[] { emptyId, null, "", null });
        Map<String, Object> emptyRow = jdbcTemplate.queryForMap(selectCommand("basic_types_explicit_test", "char_value, varchar_value, nvarchar_value"), new Object[] { emptyId });
        if (isOracle()) {
            assertNull(value(emptyRow, "varchar_value"));
        } else {
            assertEquals("", value(emptyRow, "varchar_value"));
        }
    }
}
