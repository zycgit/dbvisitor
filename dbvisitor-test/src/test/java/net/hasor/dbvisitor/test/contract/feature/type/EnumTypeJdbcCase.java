/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnum;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfCode;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfValue;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class EnumTypeJdbcCase extends TypeJdbcCommandSupport {
    protected int baseId() {
        return 660000;
    }

    // 能力归属：类型处理器 / 枚举处理器 / 枚举映射。
    @Test
    @Capability(value = CapabilityId.TYPE_ENUM_NAME, column = "types/enum-handlers/enums")
    public void enumName_shouldMapFromStringColumn() throws SQLException {
        int id = baseId() + 1;
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_string"), new Object[] { id, StatusEnum.ACTIVE.name() });

        StatusEnum loaded = jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), new Object[] { id }, StatusEnum.class);

        assertEquals(StatusEnum.ACTIVE, loaded);
    }

    // 能力归属：类型处理器 / 枚举处理器 / 枚举映射。
    @Test
    @Capability(value = CapabilityId.TYPE_ENUM_CODE, column = "types/enum-handlers/enums")
    public void enumCode_shouldMapFromCustomStringCode() throws SQLException {
        int id = baseId() + 2;
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_string"), new Object[] { id, "inactive" });

        StatusEnumOfCode loaded = jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), new Object[] { id }, StatusEnumOfCode.class);

        assertEquals(StatusEnumOfCode.INACTIVE, loaded);
        assertEquals("inactive", loaded.codeName());
    }

    // 能力归属：类型处理器 / 枚举处理器 / 枚举映射。
    @Test
    @Capability(value = CapabilityId.TYPE_ENUM_VALUE, column = "types/enum-handlers/enums")
    public void enumValue_shouldMapFromIntegerCode() throws SQLException {
        int id = baseId() + 3;
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_code"), new Object[] { id, -1 });

        StatusEnumOfValue loaded = jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_code"), new Object[] { id }, StatusEnumOfValue.class);

        assertEquals(StatusEnumOfValue.DELETED, loaded);
        assertEquals(-1, loaded.codeValue());
    }

    // 能力归属：类型处理器 / 枚举类型 / 枚举参数的 name、字符串 code 与数值 code 写入。
    @Test
    @Capability(value = CapabilityId.TYPE_ENUM_PARAMETER_BINDING, column = "types/enum-handlers/enums")
    public void enumParameters_shouldWriteDeclaredNameAndCustomCodes() throws SQLException {
        int nameId = baseId() + 7;
        int codeId = baseId() + 8;
        int valueId = baseId() + 9;
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_string"), new Object[] { nameId, StatusEnum.ACTIVE });
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_string"), new Object[] { codeId, StatusEnumOfCode.INACTIVE });
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_code"), new Object[] { valueId, StatusEnumOfValue.DELETED });

        assertEquals("ACTIVE", jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), selectParameters(nameId), String.class));
        assertEquals("inactive", jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), selectParameters(codeId), String.class));
        assertEquals(Integer.valueOf(-1), jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_code"), selectParameters(valueId), Integer.class));
        assertEquals(StatusEnum.ACTIVE, jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), selectParameters(nameId), StatusEnum.class));
        assertEquals(StatusEnumOfCode.INACTIVE, jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), selectParameters(codeId), StatusEnumOfCode.class));
        assertEquals(StatusEnumOfValue.DELETED, jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_code"), selectParameters(valueId), StatusEnumOfValue.class));
    }

    // 能力归属：类型处理器 / 枚举处理器 / 枚举映射。
    @Test
    @Capability(value = CapabilityId.TYPE_ENUM_NULL, column = "types/enum-handlers/enums")
    public void enumValues_shouldReturnNullForNullColumns() throws SQLException {
        int id = baseId() + 4;
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_string, status_ordinal, status_code"), //
                new Object[] { id, null, null, null });

        assertNull(jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), new Object[] { id }, StatusEnum.class));
        assertNull(jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), new Object[] { id }, StatusEnumOfCode.class));
        assertNull(jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_code"), new Object[] { id }, StatusEnumOfValue.class));
        assertNull(jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_ordinal"), new Object[] { id }, StatusEnumOfValue.class));
    }

    // 能力归属：类型处理器 / 枚举处理器 / 枚举映射。
    @Test
    @Capability(value = CapabilityId.TYPE_ENUM_INVALID, column = "types/enum-handlers/enums")
    public void enumInvalidValues_shouldExposeInvalidMappingBehavior() throws SQLException {
        int invalidNameId = baseId() + 5;
        int invalidValueId = baseId() + 6;
        executeInsert(insertCommand("enum_types_explicit_test", "id, status_string"), new Object[] { invalidNameId, "UNKNOWN" });

        try {
            jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_string"), new Object[] { invalidNameId }, StatusEnum.class);
            fail("Should throw exception for invalid enum name.");
        } catch (Exception e) {
            assertExceptionMentions(e, "UNKNOWN");
        }

        executeInsert(insertCommand("enum_types_explicit_test", "id, status_code"), new Object[] { invalidValueId, 999 });

        try {
            StatusEnumOfValue result = jdbcTemplate.queryForObject(selectCommand("enum_types_explicit_test", "status_code"), new Object[] { invalidValueId }, StatusEnumOfValue.class);
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
