package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.dto.LicenseOfValueEnum;
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

public class ArgRuleTest {
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
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc");
        PlanDynamicSql segment = DynamicParsed.getParsedSql("#{name,,,,}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getSqlMode() == null;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJavaType() == null;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJdbcType() == null;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getTypeHandler() == null;
    }

    @Test
    public void toStringTest_1() {
        assert ArgRule.INSTANCE.toString().startsWith("arg [");
    }
}
