/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnum;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfCode;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfValue;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class RedisEnumTypeJdbcTest extends RedisNativeTypeSupport {

    @Test
    @Capability(CapabilityId.TYPE_ENUM_NAME)
    public void enumName_shouldMapFromStringColumn() throws SQLException {
        String id = key("1");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { id, StatusEnum.ACTIVE.name() });

        StatusEnum loaded = jdbcTemplate.queryForObject("GET ?", new Object[] { id }, StatusEnum.class);

        assertEquals(StatusEnum.ACTIVE, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_CODE)
    public void enumCode_shouldMapFromCustomStringCode() throws SQLException {
        String id = key("2");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { id, "inactive" });

        StatusEnumOfCode loaded = jdbcTemplate.queryForObject("GET ?", new Object[] { id }, StatusEnumOfCode.class);

        assertEquals(StatusEnumOfCode.INACTIVE, loaded);
        assertEquals("inactive", loaded.codeName());
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_VALUE)
    public void enumValue_shouldMapFromIntegerCode() throws SQLException {
        String id = key("3");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { id, -1 });

        StatusEnumOfValue loaded = jdbcTemplate.queryForObject("GET ?", new Object[] { id }, StatusEnumOfValue.class);

        assertEquals(StatusEnumOfValue.DELETED, loaded);
        assertEquals(-1, loaded.codeValue());
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_INVALID)
    public void enumInvalidValues_shouldExposeInvalidMappingBehavior() throws SQLException {
        String invalidNameId = key("5");
        String invalidValueId = key("6");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { invalidNameId, "UNKNOWN" });

        try {
            jdbcTemplate.queryForObject("GET ?", new Object[] { invalidNameId }, StatusEnum.class);
            fail("Should throw exception for invalid enum name.");
        } catch (Exception e) {
            assertExceptionMentions(e, "UNKNOWN");
        }

        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { invalidValueId, 999 });

        try {
            StatusEnumOfValue result = jdbcTemplate.queryForObject("GET ?", new Object[] { invalidValueId }, StatusEnumOfValue.class);
            assertNull(result);
        } catch (Exception e) {
            assertExceptionMentions(e, "999");
        }
    }

    private void assertExceptionMentions(Exception e, String expected) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(expected)) {
                return;
            }
            current = current.getCause();
        }
        fail("Expected exception message to mention: " + expected);
    }
}
