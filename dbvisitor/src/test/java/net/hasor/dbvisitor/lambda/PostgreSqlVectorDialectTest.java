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
import net.hasor.dbvisitor.dialect.provider.PostgreSqlDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.vector.PgVectorTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

public class PostgreSqlVectorDialectTest {
    @Test
    public void vectorOrderingShouldUseMappedTypeHandler() throws SQLException {
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);
        assertVectorOrder(MetricType.L2, "<->", vector);
        assertVectorOrder(MetricType.COSINE, "<=>", vector);
        assertVectorOrder(MetricType.IP, "<#>", vector);
    }

    private void assertVectorOrder(MetricType metric, String operator, List<Float> vector) throws SQLException {
        Options options = Options.of();
        options.setDialect(PostgreSqlDialect.DEFAULT);
        LambdaTemplate lambda = new LambdaTemplate((DataSource) null, options);

        BoundSql boundSql = lambda.query(VectorItem.class)//
                .orderByMetric(metric, VectorItem::getEmbedding, vector)//
                .getBoundSql();

        assertEquals("SELECT * FROM vector_item ORDER BY embedding " + operator + " ?", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertTrue(boundSql.getArgs()[0] instanceof SqlArg);
        SqlArg vectorArg = (SqlArg) boundSql.getArgs()[0];
        assertSame(vector, vectorArg.getValue());
        assertTrue(vectorArg.getTypeHandler() instanceof PgVectorTypeHandler);
    }

    @Table("vector_item")
    public static class VectorItem {
        @Column(typeHandler = PgVectorTypeHandler.class)
        private List<Float> embedding;

        public List<Float> getEmbedding() {
            return this.embedding;
        }

        public void setEmbedding(List<Float> embedding) {
            this.embedding = embedding;
        }
    }
}
