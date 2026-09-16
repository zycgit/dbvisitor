/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class BasicBooleanTypeJdbcCase extends BasicTypeJdbcSupport {
    // 能力归属：类型处理器 / 数字与布尔 / 布尔值。
    @Test
    @Capability(value = CapabilityId.TYPE_BASIC_BOOLEAN, column = "types/basic-types/values")
    public void basicBooleanTypes_shouldRoundTripTrueAndFalse() throws SQLException {
        assertEquals(Boolean.TRUE, roundTripBooleanValue(true));
        assertEquals(Boolean.FALSE, roundTripBooleanValue(false));
    }

    protected Boolean roundTripBooleanValue(boolean input) throws SQLException {
        int trueId = baseId() + 2;
        int falseId = baseId() + 8;
        String insertSql = insertCommand("basic_types_test", "id, bool_value");
        int id = input ? trueId : falseId;
        assertEquals(1, jdbcTemplate.executeUpdate(insertSql, new Object[] { id, input }));
        return jdbcTemplate.queryForObject(selectCommand("basic_types_test", "bool_value"), new Object[] { id }, Boolean.class);
    }
}
