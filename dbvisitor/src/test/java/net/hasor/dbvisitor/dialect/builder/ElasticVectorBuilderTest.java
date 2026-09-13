/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.builder;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionLogic;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionType;
import net.hasor.dbvisitor.dialect.provider.Elastic7Dialect;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.lambda.core.OrderType;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ElasticVectorBuilderTest {
    @Test
    public void vectorOrdering_shouldWrapScalarQueryInScriptScore() throws SQLException {
        Elastic7Dialect builder = new Elastic7Dialect();
        builder.setTable(null, null, "vectors");
        List<Float> vector = List.of(1.0f, 0.0f);
        builder.addCondition(ConditionLogic.AND, "category", null, ConditionType.EQ, "A", null, null);
        builder.addOrderByVector("embedding", null, vector, null, MetricType.L2);

        BoundSql sql = builder.buildSelect(false);
        assertTrue(sql.getSqlString().contains("\"script_score\""));
        assertTrue(sql.getSqlString().contains("l2norm(params.vector, params.field)"));
        assertTrue(sql.getSqlString().contains("\"_score\": { \"order\": \"desc\" }"));
        assertFalse(sql.getSqlString().contains("\"_script\""));
        assertArrayEquals(new Object[] { "A", "embedding", vector }, sql.getArgs());
    }

    @Test
    public void vectorScore_shouldPreserveScalarSortPosition() throws SQLException {
        Elastic7Dialect builder = new Elastic7Dialect();
        builder.addOrderBy("category", null, OrderType.ASC, null);
        builder.addOrderByVector("embedding", null, List.of(1.0f), null, MetricType.COSINE);
        builder.addOrderBy("id", null, OrderType.ASC, null);

        String sql = builder.buildSelect(false).getSqlString();
        int sort = sql.indexOf("\"sort\"");
        assertTrue(sql.indexOf("\"category\"", sort) < sql.indexOf("\"_score\"", sort));
        assertTrue(sql.indexOf("\"_score\"", sort) < sql.indexOf("\"id\"", sort));
    }

    @Test
    public void range_shouldBindFieldVectorAndThresholdOutsideScript() throws SQLException {
        Elastic7Dialect builder = new Elastic7Dialect();
        List<Float> vector = List.of(1.0f, 2.0f);
        String field = "embedding']'; return true; //";
        builder.addConditionForVectorRange(ConditionLogic.AND, field, null, vector, null, 0.2, null, MetricType.COSINE);

        BoundSql sql = builder.buildSelect(false);
        assertTrue(sql.getSqlString().contains("\"min_score\": 1"));
        assertTrue(sql.getSqlString().contains("< params.threshold"));
        assertFalse(sql.getSqlString().contains(field));
        assertArrayEquals(new Object[] { field, vector, 0.2 }, sql.getArgs());
    }

    @Test
    public void innerProductRange_shouldUseNegativeDotProductDistance() throws SQLException {
        Elastic7Dialect builder = new Elastic7Dialect();
        builder.addConditionForVectorRange(ConditionLogic.AND, "embedding", null, List.of(1.0f), null, -0.8, null, MetricType.IP);
        assertTrue(builder.buildSelect(false).getSqlString().contains("(-dotProduct(params.vector, params.field)) < params.threshold"));
    }

    @Test
    public void vectorBuilder_shouldRenderRepeatedlyAndClearState() throws SQLException {
        Elastic7Dialect builder = new Elastic7Dialect();
        builder.addOrderByVector("embedding", null, List.of(1.0f), null, MetricType.IP);
        BoundSql first = builder.buildSelect(false);
        assertArrayEquals(first.getArgs(), builder.buildSelect(false).getArgs());
        builder.clearAll();
        assertFalse(builder.buildSelect(false).getSqlString().contains("script_score"));
        builder.addOrderByVector("embedding", null, List.of(1.0f), null, MetricType.L2);
        assertTrue(builder.buildSelect(false).getSqlString().contains("l2norm"));
    }

    @Test
    public void vectorOrdering_shouldRejectSecondScoreAndUnsupportedMetrics() {
        Elastic7Dialect builder = new Elastic7Dialect();
        builder.addOrderByVector("first", null, List.of(1.0f), null, MetricType.L2);
        try {
            builder.addOrderByVector("second", null, List.of(1.0f), null, MetricType.L2);
            fail("Expected multiple vector scores to be rejected");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("one vector score"));
        }
        builder.clearAll();
        try {
            builder.addOrderByVector("embedding", null, List.of(1.0f), null, MetricType.HAMMING);
            fail("L1 distance is not Hamming distance");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("HAMMING"));
        }
    }
}
