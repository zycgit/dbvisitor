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

@NxnContract
public abstract class BasicCharacterTypeJdbcCase extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_CHARACTER)
    public void basicCharacterTypes_shouldRoundTripAsciiAndUnicode() throws SQLException {
        int id = baseId() + 6;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_explicit_test", "id, char_value, varchar_value, nvarchar_value"), //
                new Object[] { id, 'A', "Hello World!", "你好世界！🌍" });

        Map<String, Object> row = jdbcTemplate.queryForMap(selectCommand("basic_types_explicit_test", "char_value, varchar_value, nvarchar_value"), new Object[] { id });

        assertEquals('A', ((String) value(row, "char_value")).charAt(0));
        assertEquals("Hello World!", value(row, "varchar_value"));
        assertEquals("你好世界！🌍", value(row, "nvarchar_value"));
    }
}
