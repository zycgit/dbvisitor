/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class BasicBooleanNullJdbcCase extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_BOOLEAN_NULL)
    public void basicBooleanNull_shouldRemainNull() throws SQLException {
        int nullId = baseId() + 3;
        assertEquals(1, jdbcTemplate.executeUpdate(insertCommand("basic_types_test", "id, bool_value"), new Object[] { nullId, null }));
        Boolean nullValue = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "bool_value"), new Object[] { nullId }, Boolean.class);
        assertNull(nullValue);
    }
}
