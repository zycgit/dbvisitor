/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.Date;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnum;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RedisNamedFieldTypeContractTest extends RedisNativeTypeSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_DATE)
    public void date_shouldRoundTripByFieldName() throws SQLException {
        assertNamed(Date.valueOf("2024-02-29"), Date.class);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_ENUM)
    public void enum_shouldRoundTripByFieldName() throws SQLException {
        assertNamed(StatusEnum.ACTIVE, StatusEnum.class);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_BOOLEAN)
    public void boolean_shouldRoundTripByFieldName() throws SQLException {
        assertNamed(true, Boolean.class);
        assertNamed(false, Boolean.class);
    }

    private <T> void assertNamed(T expected, Class<T> type) throws SQLException {
        String key = key("named-" + type.getSimpleName() + expected);
        this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, expected });
        Object actual = this.jdbcTemplate.queryForObject("GET ?", new Object[] { key },
                (rs, row) -> TypeHandlerRegistry.DEFAULT.getTypeHandler(type).getResult(rs, "VALUE"));
        assertEquals(expected, actual);
    }
}
