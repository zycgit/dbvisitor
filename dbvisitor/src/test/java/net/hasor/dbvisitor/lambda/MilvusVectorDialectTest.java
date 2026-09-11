/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.provider.MilvusDialect;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MilvusVectorDialectTest {
    @Test
    public void similarityRangesShouldUseGreaterThanWithBoundThresholds() throws SQLException {
        LambdaTemplate lambda = newLambda();
        float[] vector = { 1, 0 };
        assertRange(lambda.queryFreedom("items").vectorByCosine("v", vector, 0.8).getBoundSql(), "<=>", ">", vector, 0.8);
        assertRange(lambda.queryFreedom("items").vectorByIP("v", vector, -0.5).getBoundSql(), "<#>", ">", vector, -0.5);
        assertRange(lambda.queryFreedom("items").vectorByBM25("v", "search text", 1.0).getBoundSql(), "<?>", ">", "search text", 1.0);
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
