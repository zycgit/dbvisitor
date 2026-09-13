/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ParameterMarkerTest {
    @Test
    public void jdbcBindsPositionValuesWithoutBindingEscapedQuestionMark() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement("PUT /users/_doc/1?refresh=true {\"id\":?,\"name\":?}")).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        assertEquals(1, new JdbcTemplate(connection).executeUpdate(
                "PUT /users/_doc/1\\?refresh=true {\"id\":?,\"name\":?}", new Object[] { 1, "Alice" }));
        verify(statement).setInt(1, 1);
        verify(statement).setString(2, "Alice");
    }

    @Test
    public void jdbcBindsNamedValuesWithoutBindingEscapedQuestionMark() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement("PUT /users/_doc/1?refresh=true {\"id\":?,\"name\":?}")).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        assertEquals(1, new JdbcTemplate(connection).executeUpdate(
                "PUT /users/_doc/1\\?refresh=true {\"id\":#{id},\"name\":#{name}}", Map.of("id", 1, "name", "Alice")));
        verify(statement).setInt(1, 1);
        verify(statement).setString(2, "Alice");
    }

    @Test
    public void escapedMarkersDoNotConsumePositionArguments() throws Exception {
        SqlBuilder result = DynamicParsed.getParsedSql("PUT /${index}/_doc\\?refresh=true\\&pretty {\"id\":?,\"name\":?}")
                .buildQuery(Map.of("index", "users", "arg0", 7, "arg1", "alice"), new TestQueryContext());
        assertEquals("PUT /users/_doc?refresh=true&pretty {\"id\":?,\"name\":?}", result.getSqlString());
        assertEquals(2, result.getArgs().length);
        assertEquals(7, ((SqlArg) result.getArgs()[0]).getValue());
        assertEquals("alice", ((SqlArg) result.getArgs()[1]).getValue());
    }

    @Test
    public void colonBeforeBracedParameterIsLiteral() throws Exception {
        SqlBuilder result = DynamicParsed.getParsedSql("{\"id\":#{id},\"name\": :name,\"number\":&number}")
                .buildQuery(Map.of("id", 7, "name", "alice", "number", 3), new TestQueryContext());
        assertEquals("{\"id\":?,\"name\": ?,\"number\":?}", result.getSqlString());
        assertEquals(3, result.getArgs().length);
        assertEquals(7, ((SqlArg) result.getArgs()[0]).getValue());
        assertEquals("alice", ((SqlArg) result.getArgs()[1]).getValue());
        assertEquals(3, ((SqlArg) result.getArgs()[2]).getValue());
    }

    @Test
    public void backslashParityPreservesOtherBackslashes() throws Exception {
        for (int count = 1; count <= 4; count++) {
            String prefix = "\\".repeat(count);
            SqlBuilder result = DynamicParsed.getParsedSql(prefix + "?")
                    .buildQuery(Map.of("arg0", 7), new TestQueryContext());
            assertEquals("\\".repeat(count % 2 == 0 ? count : count - 1) + "?", result.getSqlString());
            assertEquals(count % 2 == 0 ? 1 : 0, result.getArgs().length);
        }
    }

    @Test
    public void quotedTextCommentsAndCastsKeepTheirMeaning() throws Exception {
        String sql = "select '\\?', \"\\:\", :id::int /* \\? */ -- \\&name\n, \\:literal, \\&literal, \\path";
        SqlBuilder result = DynamicParsed.getParsedSql(sql).buildQuery(Map.of("id", 7), new TestQueryContext());
        assertEquals("select '\\?', \"\\:\", ?::int /* \\? */ -- \\&name\n, :literal, &literal, \\path", result.getSqlString());
        assertEquals(1, result.getArgs().length);
        assertEquals(7, ((SqlArg) result.getArgs()[0]).getValue());
    }
}
