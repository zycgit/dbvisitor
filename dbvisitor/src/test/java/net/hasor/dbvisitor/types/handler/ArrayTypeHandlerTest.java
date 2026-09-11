/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler;
import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class ArrayTypeHandlerTest {
    @Test
    public void primitiveArraysAreBoxedForJdbcWithoutChangingElements() throws SQLException {
        Object[] arrays = { new boolean[] { true, false }, new byte[] { -1, 2 }, new short[] { -3, 4 }, new int[] { -5, 6 }, new long[] { Long.MIN_VALUE, Long.MAX_VALUE }, new float[] { 0.5f, -1.25f }, new double[] { Math.PI, -2.5 }, new char[] { 'a', '中' }, new float[0] };
        String[] sqlTypes = { "BOOLEAN", "TINYINT", "SMALLINT", "INTEGER", "BIGINT", "FLOAT", "DOUBLE", "CHAR", "FLOAT" };
        for (int index = 0; index < arrays.length; index++) {
            Object input = arrays[index];
            Object[] expected = new Object[java.lang.reflect.Array.getLength(input)];
            for (int element = 0; element < expected.length; element++) {
                expected[element] = java.lang.reflect.Array.get(input, element);
            }
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            Array array = mock(Array.class);
            when(statement.getConnection()).thenReturn(connection);
            when(connection.createArrayOf(eq(sqlTypes[index]), any(Object[].class))).thenAnswer(invocation -> {
                assertArrayEquals(expected, invocation.getArgument(1));
                return array;
            });
            new ArrayTypeHandler().setParameter(statement, 1, input, JDBCType.ARRAY.getVendorTypeNumber());
            verify(statement).setArray(1, array);
            verify(array).free();
        }
    }

    @Test
    public void failedBindingFreesOwnedArrayButDoesNotFreeCallerArray() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Array array = mock(Array.class);
        when(statement.getConnection()).thenReturn(connection);
        when(connection.createArrayOf(eq("INTEGER"), any(Object[].class))).thenReturn(array);
        doThrow(new SQLException("bind failed")).when(statement).setArray(1, array);
        assertThrows(SQLException.class, () -> new ArrayTypeHandler().setParameter(statement, 1, new int[] { 1, 2 }, java.sql.Types.ARRAY));
        verify(array).free();

        Array callerArray = mock(Array.class);
        new ArrayTypeHandler().setParameter(statement, 2, callerArray, java.sql.Types.ARRAY);
        verify(statement).setArray(2, callerArray);
        verify(callerArray, never()).free();
    }

    @Test
    public void testArrayTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            jdbcTemplate.executeUpdate("insert into tb_h2_types (a_char) values (?);", ps -> {
                new ArrayTypeHandler().setParameter(ps, 1, testSet.toArray(), JDBCType.ARRAY.getVendorTypeNumber());
            });
            List<Object> dat = jdbcTemplate.queryForList("select a_char from tb_h2_types where a_char is not null limit 1;", (rs, rowNum) -> {
                return new ArrayTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Object[];
            assert ((Object[]) dat.get(0)).length == 3;
            assert ((Object[]) dat.get(0))[0].equals("a");
            assert ((Object[]) dat.get(0))[1].equals("b");
            assert ((Object[]) dat.get(0))[2].equals("c");
        }
    }

    @Test
    public void testArrayTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            jdbcTemplate.executeUpdate("insert into tb_h2_types (a_char) values (?);", ps -> {
                new ArrayTypeHandler().setParameter(ps, 1, testSet.toArray(), JDBCType.ARRAY.getVendorTypeNumber());
            });
            List<Object> dat = jdbcTemplate.queryForList("select a_char from tb_h2_types where a_char is not null limit 1;", (rs, rowNum) -> {
                return new ArrayTypeHandler().getResult(rs, "a_char");
            });
            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Object[];
            assert ((Object[]) dat.get(0)).length == 3;
            assert ((Object[]) dat.get(0))[0].equals("a");
            assert ((Object[]) dat.get(0))[1].equals("b");
            assert ((Object[]) dat.get(0))[2].equals("c");
        }
    }
}
