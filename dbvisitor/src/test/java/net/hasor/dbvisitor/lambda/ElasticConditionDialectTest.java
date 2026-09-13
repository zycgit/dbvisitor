/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;

import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionLogic;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionType;
import net.hasor.dbvisitor.dialect.SqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.provider.AbstractElasticDialect;
import net.hasor.dbvisitor.dialect.provider.Elastic6Dialect;
import net.hasor.dbvisitor.dialect.provider.Elastic7Dialect;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.lambda.core.OrderType;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticConditionDialectTest {
    @Test
    public void emptyMutationsRequireExplicitOptIn() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            assertThrows(IllegalStateException.class, () -> dialect.buildDelete(false, false));
            dialect.addUpdateSet("age", 25, null);
            assertThrows(IllegalStateException.class, () -> dialect.buildUpdate(false, false));
            assertTrue(dialect.buildDelete(false, true).getSqlString().contains("match_all"));
            assertTrue(dialect.buildUpdate(false, true).getSqlString().contains("putAll"));
            dialect.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);
            assertNotNull(dialect.buildDelete(false, false));
            assertNotNull(dialect.buildUpdate(false, false));
        }
    }

    @Test
    public void defaultOrderIsAscending() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            dialect.addOrderBy("id", null, OrderType.DEFAULT, null);
            assertTrue(dialect.buildSelect(false).getSqlString().contains("\"order\": \"asc\""));
        }
    }

    @Test
    public void assignedPrimaryKeyUsesDocumentIdAndExplicitConflictPolicy() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            dialect.addInsert("id", "user/42", null);
            dialect.addInsert("name", "first", null);
            BoundSql into = dialect.buildInsert(false, Collections.singletonList("id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Into, null);
            assertTrue(into.getSqlString().contains("document_id_column=1, duplicate_strategy=Into"));
            assertArrayEquals(new Object[] { "user/42", "first" }, into.getArgs());
            BoundSql ignore = dialect.buildInsert(false, Collections.singletonList("id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Ignore, null);
            assertTrue(ignore.getSqlString().contains("duplicate_strategy=Ignore"));
            BoundSql update = dialect.buildInsert(false, Collections.singletonList("id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Update, null);
            assertTrue(update.getSqlString().contains("duplicate_strategy=Update"));
        }
    }

    @Test
    public void absentPrimaryMappingKeepsServerGeneratedDocumentId() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            dialect.addInsert("name", "generated", null);
            BoundSql insert = dialect.buildInsert(false, Collections.emptyList(), 1, Collections.emptyList(), DuplicateKeyStrategy.Into, null);
            assertFalse(insert.getSqlString().contains("/{?}"));
            assertArrayEquals(new Object[] { "generated" }, insert.getArgs());
        }
    }

    @Test
    public void likeValuesRemainBoundAndDoNotShiftFollowingArguments() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            String hostile = "a\"},\"matchall\":{}\\line\n";
            dialect.addCondition(ConditionLogic.AND, "name", null, ConditionType.LIKE, hostile, null, SqlLike.RIGHT);
            dialect.addCondition(ConditionLogic.AND, "age", null, ConditionType.GE, 18, null, null);
            BoundSql query = dialect.buildSelect(false);
            assertFalse(query.getSqlString().contains(hostile));
            assertTrue(query.getSqlString().contains("\"wildcard\""));
            assertArrayEquals(new Object[] { hostile + "*", 18 }, query.getArgs());
        }
    }

    @Test
    public void lambdaArgumentWrapperKeepsItsValueAndBindingMetadata() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            SqlArg value = SqlArg.valueOf("quoted\"text", java.sql.Types.VARCHAR);
            dialect.addCondition(ConditionLogic.AND, "name", null, ConditionType.LIKE, value, null, SqlLike.DEFAULT);
            SqlArg bound = (SqlArg) dialect.buildSelect(false).getArgs()[0];
            assertEquals("*quoted\"text*", bound.getValue());
            assertEquals(value.getJdbcType(), bound.getJdbcType());
            assertEquals("quoted\"text", value.getValue());
        }
    }

    @Test
    public void nullConditionsHaveNoPhantomParameters() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            dialect.addCondition(ConditionLogic.AND, "email", null, ConditionType.IS_NULL, null, null, null);
            dialect.addCondition(ConditionLogic.AND, "name", null, ConditionType.IS_NOT_NULL, null, null, null);
            dialect.addCondition(ConditionLogic.AND, "age", null, ConditionType.EQ, 20, null, null);
            assertArrayEquals(new Object[] { 20 }, dialect.buildSelect(false).getArgs());
        }
    }

    @Test
    public void groupsAndRawConditionsBindInRenderedOrderOnRepeatedBuilds() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            dialect.addCondition(ConditionLogic.AND, "id", null, ConditionType.GE, 1, null, null);
            dialect.addConditionGroup(ConditionLogic.AND, group -> {
                group.addCondition(ConditionLogic.AND, "age", null, ConditionType.EQ, 20, null, null);
                group.addCondition(ConditionLogic.OR, "age", null, ConditionType.EQ, 30, null, null);
            });
            dialect.addRawCondition(ConditionLogic.AND, new BoundSql.BoundSqlObj("{\"term\":{\"name\":?}}", new Object[] { "raw" }));
            for (int repeat = 0; repeat < 2; repeat++) {
                BoundSql query = dialect.buildSelect(false);
                assertArrayEquals(new Object[] { 1, 20, 30, "raw" }, query.getArgs());
                assertTrue(query.getSqlString().contains("\"should\""));
                assertTrue(query.getSqlString().replace(" ", "").contains("\"minimum_should_match\":1"));
            }
        }
    }

    @Test
    public void negatedGroupsRetainTheirNegation() throws Exception {
        for (AbstractElasticDialect dialect : dialects()) {
            dialect.addConditionGroup(ConditionLogic.AND_NOT, group ->
                    group.addCondition(ConditionLogic.AND, "age", null, ConditionType.EQ, 30, null, null));
            BoundSql query = dialect.buildSelect(false);
            assertTrue(query.getSqlString().contains("\"must_not\""));
            assertArrayEquals(new Object[] { 30 }, query.getArgs());
        }
    }

    private AbstractElasticDialect[] dialects() {
        AbstractElasticDialect[] dialects = { new Elastic6Dialect(), new Elastic7Dialect() };
        for (AbstractElasticDialect dialect : dialects) {
            dialect.setTable(null, null, "users");
        }
        return dialects;
    }
}
