/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.custom.LicenseOfCodeEnum;
import net.hasor.dbvisitor.types.custom.LicenseOfValueEnum;
import net.hasor.dbvisitor.types.handler.string.EnumTypeHandler;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class EnumParameterBindingTest {
    @Test
    public void bareEnums_shouldBindNamesStringCodesAndIntegerCodes() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        PreparedStatement statement = mock(PreparedStatement.class);

        registry.setParameterValue(statement, 1, PlainEnum.ACTIVE);
        registry.setParameterValue(statement, 2, LicenseOfCodeEnum.Apache2);
        registry.setParameterValue(statement, 3, LicenseOfValueEnum.Apache2);
        registry.setParameterValue(statement, 4, PlainEnum.OVERRIDDEN);

        verify(statement).setString(1, "ACTIVE");
        verify(statement).setString(2, "Apache 2.0");
        verify(statement).setInt(3, 4);
        verify(statement).setString(4, "OVERRIDDEN");
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void untypedSqlArgsAndCallableInputs_shouldUseEnumHandlerDefaults() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        PreparedStatement statement = mock(PreparedStatement.class);
        registry.setParameterValue(statement, 1, SqlArg.valueOf(PlainEnum.ACTIVE));
        registry.setParameterValue(statement, 2, SqlArg.valueOf(LicenseOfCodeEnum.Apache2));
        registry.setParameterValue(statement, 3, SqlArg.valueOf(LicenseOfValueEnum.Apache2));
        registry.setParameterValue(statement, 4, SqlArg.valueOf(PlainEnum.ACTIVE, new EnumTypeHandler<>(PlainEnum.class)));

        verify(statement).setString(1, "ACTIVE");
        verify(statement).setString(2, "Apache 2.0");
        verify(statement).setInt(3, 4);
        verify(statement).setString(4, "ACTIVE");
        verifyNoMoreInteractions(statement);

        CallableStatement callable = mock(CallableStatement.class);
        registry.setParameterValue(callable, 1, PlainEnum.ACTIVE);
        registry.setParameterValue(callable, 2, SqlArg.valueOf(LicenseOfCodeEnum.Apache2));
        registry.setParameterValue(callable, 3, SqlArg.valueOf(LicenseOfValueEnum.Apache2));
        verify(callable).setString(1, "ACTIVE");
        verify(callable).setString(2, "Apache 2.0");
        verify(callable).setInt(3, 4);
        verifyNoMoreInteractions(callable);
    }

    @Test
    public void explicitJdbcTypes_shouldRemainUnchangedIncludingOther() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        PreparedStatement statement = mock(PreparedStatement.class);
        registry.setParameterValue(statement, 1, SqlArg.valueOf(PlainEnum.ACTIVE, Types.OTHER));
        registry.setParameterValue(statement, 2, SqlArg.valueOf(PlainEnum.ACTIVE, Types.VARCHAR));
        registry.setParameterValue(statement, 3, new SqlArg(PlainEnum.ACTIVE, Types.OTHER, new EnumTypeHandler<>(PlainEnum.class)));
        registry.setParameterValue(statement, 4, new SqlArg(null, Types.OTHER, new EnumTypeHandler<>(PlainEnum.class)));

        verify(statement).setObject(1, "ACTIVE", Types.OTHER);
        verify(statement).setObject(2, "ACTIVE", Types.VARCHAR);
        verify(statement).setObject(3, "ACTIVE", Types.OTHER);
        verify(statement).setNull(4, Types.OTHER);
        verifyNoMoreInteractions(statement);

        CallableStatement callable = mock(CallableStatement.class);
        registry.setParameterValue(callable, 1, SqlArg.valueOf(PlainEnum.ACTIVE, Types.OTHER));
        verify(callable).setObject(1, "ACTIVE", Types.OTHER);
        verifyNoMoreInteractions(callable);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void customHandlers_shouldKeepSelectionAndExistingTypeInference() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        TypeHandler<PlainEnum> registered = mock(TypeHandler.class);
        TypeHandler<PlainEnum> explicit = mock(TypeHandler.class);
        registry.register(PlainEnum.class, registered);
        PreparedStatement statement = mock(PreparedStatement.class);

        registry.setParameterValue(statement, 1, PlainEnum.ACTIVE);
        registry.setParameterValue(statement, 2, SqlArg.valueOf(PlainEnum.ACTIVE));
        registry.setParameterValue(statement, 3, new SqlArg(PlainEnum.ACTIVE, Types.INTEGER, explicit));
        registry.setParameterValue(statement, 4, SqlArg.valueOf(PlainEnum.ACTIVE, explicit));

        verify(registered).setParameter(statement, 1, PlainEnum.ACTIVE, Types.OTHER);
        verify(registered).setParameter(statement, 2, PlainEnum.ACTIVE, Types.OTHER);
        verify(explicit).setParameter(statement, 3, PlainEnum.ACTIVE, Types.INTEGER);
        verify(explicit).setParameter(statement, 4, PlainEnum.ACTIVE, Types.OTHER);
        verifyNoMoreInteractions(registered, explicit, statement);

        registry.setParameterValue(statement, 5, BoundEnum.ACTIVE);
        registry.setParameterValue(statement, 6, SqlArg.valueOf(BoundEnum.ACTIVE, Types.VARCHAR));
        verify(statement).setString(5, "bound:ACTIVE:" + Types.OTHER);
        verify(statement).setString(6, "bound:ACTIVE:" + Types.VARCHAR);
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void nonEnumValuesAndUntypedNulls_shouldRetainExistingBinding() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        PreparedStatement statement = mock(PreparedStatement.class);
        registry.setParameterValue(statement, 1, null);
        registry.setParameterValue(statement, 2, "text");
        registry.setParameterValue(statement, 3, 42);
        registry.setParameterValue(statement, 4, SqlArg.valueOf(null));

        verify(statement).setObject(1, null);
        verify(statement).setString(2, "text");
        verify(statement).setInt(3, 42);
        verify(statement).setObject(4, null);
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void jdbcTemplate_shouldWriteEnumValuesThroughPositionalAndNamedParameters() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:enum_parameters_" + UUID.randomUUID())) {
            JdbcTemplate jdbc = new JdbcTemplate(connection);
            jdbc.executeUpdate("create table enum_parameters (id integer, enum_name varchar(30), enum_code varchar(30), enum_value integer)");
            assertEquals(1, jdbc.executeUpdate("insert into enum_parameters values (?, ?, ?, ?)",
                    new Object[] { 1, PlainEnum.ACTIVE, LicenseOfCodeEnum.Apache2, LicenseOfValueEnum.Apache2 }));

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", 2);
            parameters.put("enumName", PlainEnum.OVERRIDDEN);
            parameters.put("enumCode", LicenseOfCodeEnum.Apache2);
            parameters.put("enumValue", LicenseOfValueEnum.Apache2);
            assertEquals(1, jdbc.executeUpdate("insert into enum_parameters values (:id, :enumName, :enumCode, :enumValue)", parameters));

            assertEquals(Arrays.asList("ACTIVE", "OVERRIDDEN"), jdbc.queryForList("select enum_name from enum_parameters order by id", String.class));
            assertEquals(Arrays.asList("Apache 2.0", "Apache 2.0"), jdbc.queryForList("select enum_code from enum_parameters order by id", String.class));
            assertEquals(Arrays.asList(4, 4), jdbc.queryForList("select enum_value from enum_parameters order by id", Integer.class));
        }
    }

    public enum PlainEnum {
        ACTIVE,
        OVERRIDDEN {
            @Override
            public String toString() {
                return "display text";
            }
        }
    }

    @BindTypeHandler(BoundEnumHandler.class)
    public enum BoundEnum {
        ACTIVE
    }

    public static class BoundEnumHandler extends EnumTypeHandler<BoundEnum> {
        public BoundEnumHandler() {
            super(BoundEnum.class);
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, BoundEnum parameter, Integer jdbcType) throws SQLException {
            ps.setString(i, "bound:" + parameter.name() + ":" + jdbcType);
        }
    }
}
