/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.provider.MilvusDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MilvusVectorDialectTest {
    @Test
    public void similarityRangesShouldUseGreaterThanWithBoundThresholds() throws SQLException {
        LambdaTemplate lambda = newLambda();
        float[] vector = { 1, 0 };
        assertRange(lambda.queryFreedom("items").vectorByCosine("v", vector, 0.8).getBoundSql(), "<=>", ">", vector, 0.8);
        assertRange(lambda.queryFreedom("items").vectorByIP("v", vector, -0.5).getBoundSql(), "<#>", ">", vector, -0.5);
        assertRange(lambda.queryFreedom("items").vectorByBM25("v", "search text", 1.0).getBoundSql(), "<\\?>", ">", "search text", 1.0);
    }

    @Test
    public void distanceRangesShouldKeepLessThanWithBoundThresholds() throws SQLException {
        LambdaTemplate lambda = newLambda();
        float[] vector = { 1, 0 };
        byte[] binary = { 1, 2 };
        assertRange(lambda.queryFreedom("items").vectorByL2("v", vector, 2.0).getBoundSql(), "<->", "<", vector, 2.0);
        assertRange(lambda.queryFreedom("items").vectorByHamming("v", binary, 3).getBoundSql(), "~=", "<", binary, 3);
        assertRange(lambda.queryFreedom("items").vectorByJaccard("v", binary, 0.5).getBoundSql(), "<%>", "<", binary, 0.5);
    }

    @Test
    public void disabledRangeShouldNotLeaveVectorOrThresholdParameters() throws SQLException {
        BoundSql sql = newLambda().queryFreedom("items").eq("id", 7).vectorByCosine(false, "v", new float[] { 1, 0 }, 0.8).getBoundSql();
        assertEquals("SELECT * FROM items WHERE id = ?", sql.getSqlString());
        assertEquals(1, sql.getArgs().length);
        assertEquals(7, ((SqlArg) sql.getArgs()[0]).getValue());
    }

    @Test
    public void bm25OrderingShouldBindOnlyScalarAndSearchTextThroughJdbcTemplate() throws SQLException {
        for (boolean specialized : new boolean[] { true, false }) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            ResultSet rows = mock(ResultSet.class);
            String nativeSql = "SELECT * FROM items WHERE id = ? ORDER BY v <?> ? LIMIT 2";
            when(connection.prepareStatement(nativeSql)).thenReturn(statement);
            when(statement.execute()).thenReturn(true);
            when(statement.getResultSet()).thenReturn(rows);
            when(rows.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
            LambdaTemplate lambda = new LambdaTemplate(connection, Options.of().dialect(MilvusDialect.DEFAULT));
            MapQuery query = lambda.queryFreedom("items").eq("id", 7);
            String text = "search ? <?> text";
            if (specialized) {
                query.orderByBM25("v", text);
            } else {
                query.orderByMetric(MetricType.BM25, "v", text);
            }
            query.initPage(2, 0);
            assertEquals("SELECT * FROM items WHERE id = ? ORDER BY v <\\?> ? LIMIT 2", query.getBoundSql().getSqlString());
            assertTrue(query.queryForMapList().isEmpty());
            verify(connection).prepareStatement(nativeSql);
            verify(statement).setInt(1, 7);
            verify(statement).setString(2, text);
            verify(statement).execute();
            verify(statement).getResultSet();
            verify(statement, atMostOnce()).getWarnings();
            verify(statement).close();
            verifyNoMoreInteractions(statement);
        }
    }

    @Test
    public void bm25RangeShouldBindOnlyScalarSearchTextAndThresholdThroughJdbcTemplate() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);
        String nativeSql = "SELECT * FROM items WHERE id = ? AND v <?> ? > ? LIMIT 2";
        when(connection.prepareStatement(nativeSql)).thenReturn(statement);
        when(statement.execute()).thenReturn(true);
        when(statement.getResultSet()).thenReturn(rows);
        when(rows.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
        LambdaTemplate lambda = new LambdaTemplate(connection, Options.of().dialect(MilvusDialect.DEFAULT));
        assertTrue(lambda.queryFreedom("items").eq("id", 7).vectorByBM25("v", "search text", 0.75).initPage(2, 0).queryForMapList().isEmpty());
        verify(connection).prepareStatement(nativeSql);
        verify(statement).setInt(1, 7);
        verify(statement).setString(2, "search text");
        verify(statement).setDouble(3, 0.75);
        verify(statement).execute();
        verify(statement).getResultSet();
        verify(statement, atMostOnce()).getWarnings();
        verify(statement).close();
        verifyNoMoreInteractions(statement);
    }

    private LambdaTemplate newLambda() throws SQLException {
        Options options = Options.of();
        options.setDialect(MilvusDialect.DEFAULT);
        return new LambdaTemplate((DataSource) null, options);
    }

    private void assertRange(BoundSql sql, String operator, String comparison, Object vector, Number threshold) {
        assertEquals("SELECT * FROM items WHERE v " + operator + " ? " + comparison + " ?", sql.getSqlString());
        Object[] values = java.util.Arrays.stream(sql.getArgs()).map(arg -> ((SqlArg) arg).getValue()).toArray();
        assertArrayEquals(new Object[] { vector, threshold }, values);
    }
}
