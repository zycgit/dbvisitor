/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.provider.ClickHouseDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.vector.ChVectorTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClickHouseDialectTest {
    private LambdaTemplate template() throws SQLException {
        return new LambdaTemplate((DataSource) null, Options.of().dialect(ClickHouseDialect.DEFAULT));
    }

    @Test
    public void emptyMutationRequiresExplicitPermissionAndServerPredicate() throws SQLException {
        LambdaTemplate lambda = template();
        assertThrows(IllegalStateException.class, () -> lambda.delete(VectorItem.class).getBoundSql());
        assertThrows(IllegalStateException.class, () -> lambda.update(VectorItem.class).updateTo(VectorItem::getName, "all").getBoundSql());
        assertEquals("DELETE FROM vector_item WHERE 1 = 1", lambda.delete(VectorItem.class).allowEmptyWhere().getBoundSql().getSqlString());
        assertEquals("UPDATE vector_item SET name = ? WHERE 1 = 1", lambda.update(VectorItem.class).updateTo(VectorItem::getName, "all").allowEmptyWhere().getBoundSql().getSqlString());
        assertEquals("DELETE FROM vector_item WHERE name = ?", lambda.delete(VectorItem.class).eq(VectorItem::getName, "only").getBoundSql().getSqlString());
    }

    @Test
    public void mutationDeleteKeepsBindingsQuotingAndModeOnNewBuilders() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate((DataSource) null, Options.of().dialect(new ClickHouseDialect(ClickHouseDialect.DeleteMode.MUTATION)).useDelimited(true));
        BoundSql bound = lambda.delete(VectorItem.class).eq(VectorItem::getName, "only").getBoundSql();
        assertEquals("ALTER TABLE `vector_item` DELETE WHERE `name` = ?", bound.getSqlString());
        assertEquals(1, bound.getArgs().length);
        assertEquals("ALTER TABLE `vector_item` DELETE WHERE 1 = 1", lambda.delete(VectorItem.class).allowEmptyWhere().getBoundSql().getSqlString());
    }

    @Test
    public void vectorOrderingAndRangePreserveBoundValuesAndHandler() throws SQLException {
        List<Float> vector = List.of(0.1f, 0.2f);
        String[] functions = { "L2Distance", "cosineDistance", "-dotProduct" };
        MetricType[] metrics = { MetricType.L2, MetricType.COSINE, MetricType.IP };
        for (int i = 0; i < metrics.length; i++) {
            BoundSql sql = template().query(VectorItem.class).orderByMetric(metrics[i], VectorItem::getEmbedding, vector).getBoundSql();
            assertEquals("SELECT * FROM vector_item ORDER BY " + functions[i] + "(embedding, ?)", sql.getSqlString());
            SqlArg arg = (SqlArg) sql.getArgs()[0];
            assertSame(vector, arg.getValue());
            assertTrue(arg.getTypeHandler() instanceof ChVectorTypeHandler);
        }
        BoundSql range = template().query(VectorItem.class).eq(VectorItem::getName, "item").vectorByL2(VectorItem::getEmbedding, vector, 0.5).getBoundSql();
        assertEquals("SELECT * FROM vector_item WHERE name = ? AND L2Distance(embedding, ?) < ?", range.getSqlString());
        assertEquals(3, range.getArgs().length);
        assertThrows(UnsupportedOperationException.class, () -> template().query(VectorItem.class).orderByMetric(MetricType.BM25, VectorItem::getEmbedding, vector).getBoundSql());
    }

    @Table("vector_item")
    public static class VectorItem {
        private String      name;
        @Column(typeHandler = ChVectorTypeHandler.class)
        private List<Float> embedding;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Float> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Float> embedding) {
            this.embedding = embedding;
        }
    }
}
