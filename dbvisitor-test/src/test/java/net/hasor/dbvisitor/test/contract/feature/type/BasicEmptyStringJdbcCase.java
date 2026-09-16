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
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class BasicEmptyStringJdbcCase extends BasicTypeJdbcSupport {
    // 能力归属：类型处理器 / 字符与字节数组 / 字符。
    @Test
    @Capability(value = CapabilityId.TYPE_BASIC_CHARACTER_EMPTY, column = "types/basic-types/values")
    public void basicEmptyString_shouldPreserveItsDatabaseMeaning() throws SQLException {
        Object value = roundTripEmptyString();
        if (isOracle()) {
            assertNull(value);
        } else {
            assertEquals("", value);
        }
    }

    protected Object roundTripEmptyString() throws SQLException {
        int emptyId = baseId() + 7;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_explicit_test", "id, char_value, varchar_value, nvarchar_value"), new Object[] { emptyId, null, "", null });
        Map<String, Object> emptyRow = jdbcTemplate.queryForMap(selectCommand("basic_types_explicit_test", "char_value, varchar_value, nvarchar_value"), new Object[] { emptyId });
        return value(emptyRow, "varchar_value");
    }
}
