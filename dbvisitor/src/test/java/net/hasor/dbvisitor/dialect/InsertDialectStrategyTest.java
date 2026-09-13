/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;

import java.util.List;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static net.hasor.dbvisitor.lambda.GeneratedKeyStrategy.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class InsertDialectStrategyTest {
    private final String dialectName;
    private final Expected expected;
    private final List<String> primaryKey = List.of("id");
    private final List<String> columns = List.of("id", "name", "age");
    private final List<String> returnColumns = List.of("id");

    public InsertDialectStrategyTest(String dialectName, Expected expected) {
        this.dialectName = dialectName;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] dialects() {
        // Expected: no return / Into / Ignore / Update; Ignore / Update / no-PK Ignore / no-PK Update / key-only Update.
        // @formatter:off
        return new Object[][] {
            { JdbcHelper.H2,         new Expected(JdbcBatch, OneByOne,               OneByOne, OneByOne,            true,  true,  false, false, true) },
            { JdbcHelper.MYSQL,      new Expected(JdbcBatch, JdbcBatchGeneratedKeys, OneByOne, OneByOne,            true,  true,  true,  true,  true) },
            { JdbcHelper.POSTGRESQL, new Expected(JdbcBatch, MultiValuesResultSet,   OneByOne, MultiValuesResultSet, true,  true,  true,  false, false) },
            { JdbcHelper.ORACLE,     new Expected(OneByOne,  OneByOne,               OneByOne, OneByOne,            true,  true,  false, false, true) },
            { JdbcHelper.SQL_SERVER, new Expected(JdbcBatch, MultiValuesResultSet,   OneByOne, OneByOne,            true,  true,  false, false, true) },
            { JdbcHelper.DB2,        new Expected(JdbcBatch, OneByOne,               OneByOne, OneByOne,            true,  true,  false, false, true) },
            { JdbcHelper.CLICKHOUSE, new Expected(JdbcBatch, OneByOne,               OneByOne, OneByOne,            false, false, false, false, false) },
            { JdbcHelper.MILVUS,     new Expected(OneByOne,  OneByOne,               OneByOne, OneByOne,            false, true,  false, false, true) }
        };
        // @formatter:on
    }

    @Test
    public void generatedKeyStrategy_withoutReturnColumns_shouldMatchDialect() {
        GeneratedKeyStrategy actual = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, List.of(), DuplicateKeyStrategy.Into);
        assertEquals(this.expected.noReturn(), actual);
    }

    @Test
    public void generatedKeyStrategy_withReturnColumnsAndInto_shouldMatchDialect() {
        GeneratedKeyStrategy actual = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, this.returnColumns, DuplicateKeyStrategy.Into);
        assertEquals(this.expected.into(), actual);
    }

    @Test
    public void generatedKeyStrategy_withReturnColumnsAndIgnore_shouldMatchDialect() {
        GeneratedKeyStrategy actual = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, this.returnColumns, DuplicateKeyStrategy.Ignore);
        assertEquals(this.expected.ignore(), actual);
    }

    @Test
    public void generatedKeyStrategy_withReturnColumnsAndUpdate_shouldMatchDialect() {
        GeneratedKeyStrategy actual = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, this.returnColumns, DuplicateKeyStrategy.Update);
        assertEquals(this.expected.update(), actual);
    }

    @Test
    public void supportDuplicateStrategy_shouldMatchDialect() {
        InsertSqlDialect dialect = insertDialect();
        assertTrue(dialect.supportDuplicateStrategy(this.primaryKey, this.columns, List.of(), DuplicateKeyStrategy.Into));
        assertEquals(this.expected.ignoreSupported(), dialect.supportDuplicateStrategy(this.primaryKey, this.columns, List.of(), DuplicateKeyStrategy.Ignore));
        assertEquals(this.expected.updateSupported(), dialect.supportDuplicateStrategy(this.primaryKey, this.columns, List.of(), DuplicateKeyStrategy.Update));
        assertEquals(this.expected.ignoreWithoutKey(), dialect.supportDuplicateStrategy(List.of(), this.columns, List.of(), DuplicateKeyStrategy.Ignore));
        assertEquals(this.expected.updateWithoutKey(), dialect.supportDuplicateStrategy(List.of(), this.columns, List.of(), DuplicateKeyStrategy.Update));
        assertEquals(this.expected.updateKeyOnly(), dialect.supportDuplicateStrategy(this.primaryKey, this.primaryKey, List.of(), DuplicateKeyStrategy.Update));
    }

    private InsertSqlDialect insertDialect() {
        SqlDialect dialect = SqlDialectRegister.findOrCreate(this.dialectName);
        assertTrue("Dialect '" + this.dialectName + "' must implement InsertSqlDialect.", dialect instanceof InsertSqlDialect);
        return (InsertSqlDialect) dialect;
    }

    public record Expected(GeneratedKeyStrategy noReturn, GeneratedKeyStrategy into, GeneratedKeyStrategy ignore,
            GeneratedKeyStrategy update, boolean ignoreSupported, boolean updateSupported, boolean ignoreWithoutKey,
            boolean updateWithoutKey, boolean updateKeyOnly) {
    }
}
