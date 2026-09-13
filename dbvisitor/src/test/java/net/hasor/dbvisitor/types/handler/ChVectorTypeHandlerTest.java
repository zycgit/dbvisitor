/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler;
import java.sql.*;
import java.util.List;
import net.hasor.dbvisitor.types.handler.vector.ChVectorTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChVectorTypeHandlerTest {
    @Test
    public void bindsTypedArrayAndReleasesItOnFailure() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Array array = mock(Array.class);
        when(statement.getConnection()).thenReturn(connection);
        when(connection.createArrayOf(eq("Float32"), any(Object[].class))).thenAnswer(call -> {
            assertArrayEquals(new Float[] { 0.5f, -1.0f }, call.getArgument(1));
            return array;
        });
        doThrow(new SQLException("binding failed")).when(statement).setArray(1, array);
        ChVectorTypeHandler handler = new ChVectorTypeHandler();
        assertThrows(SQLException.class, () -> handler.setParameter(statement, 1, List.of(0.5f, -1.0f), null));
        verify(array).free();
        assertThrows(SQLException.class, () -> handler.setParameter(statement, 1, null, null));
    }

    @Test
    public void readsPrimitiveAndBoxedArraysByIndexAndName() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        Array primitive = mock(Array.class);
        Array boxed = mock(Array.class);
        when(result.getArray(1)).thenReturn(primitive);
        when(result.getArray("embedding")).thenReturn(boxed);
        when(primitive.getArray()).thenReturn(new float[] { 0.5f, -1.0f });
        when(boxed.getArray()).thenReturn(new Float[] { 0.5f, -1.0f });
        ChVectorTypeHandler handler = new ChVectorTypeHandler();
        assertEquals(List.of(0.5f, -1.0f), handler.getResult(result, 1));
        assertEquals(List.of(0.5f, -1.0f), handler.getResult(result, "embedding"));
        verify(primitive).free();
        verify(boxed).free();
    }
}
