/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.dto.LicenseOfValueEnum;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.dynamic.dto.ResourceType;
import net.hasor.dbvisitor.dynamic.dto.UserFutures;
import net.hasor.dbvisitor.dynamic.rule.ArgRule;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.handler.number.ShortTypeHandler;
import net.hasor.dbvisitor.types.handler.string.EnumTypeHandler;
import net.hasor.dbvisitor.types.handler.string.SqlXmlTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArgRuleTest {
    @Test
    public void configurationKeysAreRecognizedIgnoringCase() {
        String[] keys = { ArgRule.CFG_KEY_MODE, ArgRule.CFG_KEY_JDBC_TYPE, ArgRule.CFG_KEY_JAVA_TYPE,
                ArgRule.CFG_KEY_TYPE_HANDLER, ArgRule.CFG_KEY_NAME, ArgRule.CFG_KEY_TYPE_NAME, ArgRule.CFG_KEY_SCALE,
                ArgRule.CFG_KEY_EXTRACTOR, ArgRule.CFG_KEY_ROW_HANDLER, ArgRule.CFG_KEY_ROW_MAPPER };
        for (String key : keys) {
            assertTrue(key, ArgRule.isConfigEntry(key + "=value"));
            assertTrue(key, ArgRule.isConfigEntry(" " + key.toUpperCase(Locale.ROOT) + " = value"));
            assertTrue(key, ArgRule.isConfigEntry(key.toLowerCase(Locale.ROOT) + "=value"));
        }
    }

    @Test
    public void ruleTest_3() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("name", "abc");
        PlanDynamicSql segment1 = DynamicParsed.getParsedSql("#{name}");
        SqlBuilder sqlBuilder1 = segment1.buildQuery(ctx1, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getSqlMode() == null;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJdbcType() == null;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJavaType() == null;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getTypeHandler() == null;

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("name", "abc");
        PlanDynamicSql segment2 = DynamicParsed.getParsedSql("#{name,mode=out,jdbcType=123,javaType=java.lang.Integer,typeHandler=net.hasor.dbvisitor.types.handler.number.ShortTypeHandler}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx2, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getSqlMode() == SqlMode.Out;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJdbcType() == 123;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJavaType() == Integer.class;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() instanceof ShortTypeHandler;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() != TypeHandlerRegistry.DEFAULT.getHandlerByHandlerType(ShortTypeHandler.class);
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() != TypeHandlerRegistry.DEFAULT.getTypeHandler(Short.class);
        assert TypeHandlerRegistry.DEFAULT.getHandlerByHandlerType(ShortTypeHandler.class) == TypeHandlerRegistry.DEFAULT.getTypeHandler(Short.class);
    }

    @Test
    public void ruleTest_4() throws SQLException {
        Map<String, Object> ctx2 = CollectionUtils.asMap("name", ResourceType.WORKER);
        PlanDynamicSql segment2 = DynamicParsed.getParsedSql("#{name,javaType=net.hasor.dbvisitor.dynamic.dto.ResourceType}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx2, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getSqlMode() == null;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue() == ResourceType.WORKER;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJdbcType() == null;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJavaType() == ResourceType.class;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() == null;

        assert TypeHandlerRegistry.DEFAULT.getHandlerByHandlerType(EnumTypeHandler.class) == null;
    }

    @Test
    public void ruleTest_5() {
        TypeHandler<?> handler1 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        TypeHandler<?> handler2 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        assert handler1 == handler2;

        TypeHandler<?> handler3 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        TypeHandler<?> handler4 = TypeHandlerRegistry.DEFAULT.getTypeHandler(LicenseOfValueEnum.class);
        assert handler3 != handler4;
    }

    @Test
    public void ruleTest_6() throws SQLException {
        Map<String, Object> ctx2 = CollectionUtils.asMap("name", new UserFutures());
        PlanDynamicSql segment2 = DynamicParsed.getParsedSql("#{name}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx2, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJavaType() == null;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() == null;
    }

    @Test
    public void ruleTest_7() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc");
        PlanDynamicSql segment = DynamicParsed.getParsedSql("#{name,mode=out,jdbcType=123,javaType=java.lang.Integer,typeHandler=net.hasor.dbvisitor.types.handler.string.SqlXmlTypeHandler}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getSqlMode() == SqlMode.Out;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJdbcType() == 123;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJavaType() == Integer.class;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getTypeHandler() instanceof SqlXmlTypeHandler;
    }

    @Test
    public void ruleTest_8() throws SQLException {
        try {
            DynamicParsed.getParsedSql("#{name,,,,}");
        } catch (Exception e) {
            assert e.getMessage().contains("analysisSQL failed, config must be 'key = value'");
        }
    }

    @Test
    public void toStringTest_1() {
        assert ArgRule.INSTANCE.toString().startsWith("arg [");
    }

    private void assertExpression(String expression, Map<String, Object> values, Object expected) throws SQLException {
        SqlBuilder parsed = DynamicParsed.getParsedSql("#{" + expression + "}").buildQuery(values, new TestQueryContext());
        SqlBuilder rule = new SqlBuilder();
        ArgRule.INSTANCE.executeRule(new MapSqlArgSource(values), new TestQueryContext(), rule, "", expression);
        for (SqlBuilder result : new SqlBuilder[] { parsed, rule }) {
            assertEquals("?", result.getSqlString());
            assertEquals(1, result.getArgs().length);
            assertEquals(expected, ((SqlArg) result.getArgs()[0]).getValue());
        }
    }

    @Test
    public void comparisonExpressionsAreNotConfiguration() throws SQLException {
        assertExpression("minAge == null ? 0 : minAge", Collections.singletonMap("minAge", null), 0);
        assertExpression("minAge == null ? 0 : minAge", Map.of("minAge", 18), 18);
        assertExpression("age != 18", Map.of("age", 20), true);
        assertExpression("age >= 18", Map.of("age", 18), true);
        assertExpression("age <= 18", Map.of("age", 20), false);
        assertExpression("mode == null ? 1 : mode", Collections.singletonMap("mode", null), 1);
    }

    @Test
    public void quotedEqualsSignsRemainExpressionData() throws SQLException {
        assertExpression("'a=b'", Collections.emptyMap(), "a=b");
        assertExpression("\"name=alice\"", Collections.emptyMap(), "name=alice");
        assertExpression("text == 'a=b' ? text : 'other'", Map.of("text", "a=b"), "a=b");
    }

    @Test
    public void expressionCanBeFollowedByConfiguration() throws SQLException {
        SqlBuilder result = DynamicParsed.getParsedSql("#{age >= 18 ? age : 18,jdbcType=INTEGER,javaType=java.lang.Integer}")
                .buildQuery(Map.of("age", 20), new TestQueryContext());
        SqlArg arg = (SqlArg) result.getArgs()[0];
        assertEquals(20, arg.getValue());
        assertEquals(Integer.valueOf(Types.INTEGER), arg.getJdbcType());
        assertEquals(Integer.class, arg.getJavaType());
    }

    @Test
    public void outputConfigurationStillAllowsOmittedExpression() throws SQLException {
        String config = "mode=OUT,jdbcType=DECIMAL,name=total,typeName=DECIMAL,scale=2";
        SqlBuilder parsed = DynamicParsed.getParsedSql("#{" + config + "}").buildQuery(Collections.emptyMap(), new TestQueryContext());
        SqlBuilder rule = new SqlBuilder();
        ArgRule.INSTANCE.executeRule(new MapSqlArgSource(Collections.emptyMap()), new TestQueryContext(), rule, "", config);
        for (SqlBuilder result : new SqlBuilder[] { parsed, rule }) {
            SqlArg arg = (SqlArg) result.getArgs()[0];
            assertEquals(SqlMode.Out, arg.getSqlMode());
            assertEquals(Integer.valueOf(Types.DECIMAL), arg.getJdbcType());
            assertEquals("total", arg.getAsName());
            assertEquals("DECIMAL", arg.getJdbcTypeName());
            assertEquals(Integer.valueOf(2), arg.getScale());
            assertNull(arg.getValue());
        }
    }

    @Test
    public void unsupportedAssignmentsDoNotBecomeExecutableExpressions() {
        assertThrows(IllegalArgumentException.class, () -> DynamicParsed.getParsedSql("#{age = 18}"));
        assertThrows(IllegalArgumentException.class, () -> DynamicParsed.getParsedSql("#{user.age = 18}"));
        assertThrows(IllegalArgumentException.class, () -> DynamicParsed.getParsedSql("#{unknownConfig = value}"));
    }
}
