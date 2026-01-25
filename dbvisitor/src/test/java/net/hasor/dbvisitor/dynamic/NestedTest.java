package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.string.NStringTypeHandler;
import org.junit.Assert;
import org.junit.Test;

public class NestedTest {

    @Test
    public void nestedRuleTest() throws SQLException {
        String sql = "@{and, password = @{md5, :pwd}}";
        Map<String, Object> params = new HashMap<>();
        params.put("pwd", "123456");

        // md5('123456') = e10adc3949ba59abbe56e057f20f883e
        // dbVisitor's @{and} adds "where" if at the beginning
        String expectedSql = "where  password = ?";

        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        SqlBuilder sqlBuilder = segment.buildQuery(params, new TestQueryContext());

        String generatedSql = sqlBuilder.getSqlString();
        if (!StringUtils.equals(generatedSql, "where  password = ?")) {
            Assert.assertEquals(String.format("Original: [%s]", generatedSql), expectedSql, generatedSql);
        }

        Assert.assertEquals("Arg count mismatch", 1, sqlBuilder.getArgs().length);

        Object outputArg = ((SqlArg) sqlBuilder.getArgs()[0]).getValue();
        Assert.assertEquals("MD5 mismatch", "e10adc3949ba59abbe56e057f20f883e", outputArg);
    }

    @Test
    public void nestedInjectTest() throws SQLException {
        // Case from ReproductionTest: Injection ${...} inside @{and} (Should KEEP even if args are empty)
        String sql3 = "SELECT * FROM tb_user WHERE 1=1 @{and, ${inj}}";
        Map<String, Object> params = new HashMap<>();
        params.put("inj", "deleted=0"); // injection value

        PlanDynamicSql plan = DynamicParsed.getParsedSql(sql3);
        SqlBuilder builder = plan.buildQuery(params, new TestQueryContext());

        // AndRule sees "deleted=0". Args empty. allowNull=false. testNullValue=true.
        // But haveInjection list is NOT empty. So it should KEEP.
        String expected = "SELECT * FROM tb_user WHERE 1=1 and  deleted=0";
        String actual = builder.getSqlString();

        Assert.assertEquals(expected, actual.trim());
    }

    @Test
    public void nestedRoleAndArgsTest() throws SQLException {
        String sql = "@{and, password = @{md5, #{pwd}}}";
        Map<String, Object> params = new HashMap<>();
        params.put("pwd", "123456");

        // md5('123456') = e10adc3949ba59abbe56e057f20f883e
        // dbVisitor's @{and} adds "where" if at the beginning
        String expectedSql = "where  password = ?";

        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        SqlBuilder sqlBuilder = segment.buildQuery(params, new TestQueryContext());

        String generatedSql = sqlBuilder.getSqlString();
        if (!StringUtils.equals(generatedSql, "where  password = ?")) {
            Assert.assertEquals(String.format("Original: [%s]", generatedSql), expectedSql, generatedSql);
        }
        Assert.assertEquals("Arg count mismatch", 1, sqlBuilder.getArgs().length);

        Object outputArg = ((SqlArg) sqlBuilder.getArgs()[0]).getValue();
        Assert.assertEquals("MD5 mismatch", "e10adc3949ba59abbe56e057f20f883e", outputArg);
    }

    @Test
    public void nestedComplexArgsTest() throws SQLException {
        // Test strict parsing of attributes in #{...} inside a rule
        String sql = "@{and, col = #{val, jdbcType=111, mode=IN, javaType=java.lang.String, typeHandler=net.hasor.dbvisitor.types.handler.string.NStringTypeHandler}}";

        Map<String, Object> params = new HashMap<>();
        params.put("val", "123");

        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        SqlBuilder builder = segment.buildQuery(params, new TestQueryContext());

        String generatedSql = builder.getSqlString();
        assert StringUtils.equals(generatedSql, "where  col = ?");

        Object[] args = builder.getArgs();
        Assert.assertEquals(1, args.length);
        SqlArg arg = (SqlArg) args[0];

        Assert.assertEquals("123", arg.getValue());
        Assert.assertEquals(111, (int) arg.getJdbcType());
        Assert.assertEquals(SqlMode.In, arg.getSqlMode());
        Assert.assertEquals(String.class, arg.getJavaType());
        assert arg.getTypeHandler() instanceof NStringTypeHandler;
    }

    @Test
    public void nestedCaseMd5AndTest() throws SQLException {
        String ruleSql = ""                                 //
                + "@{and, password = @{case, encryptMode,"  //
                + "           @{when, true,  @{md5, :pwd}},"//
                + "           @{else,        :pwd}"         //
                + "       }"                                //
                + "}";

        // Case 1: encryptMode = true -> use md5
        Map<String, Object> paramsTrue = new HashMap<>();
        paramsTrue.put("encryptMode", true);
        paramsTrue.put("pwd", "123456");

        PlanDynamicSql segmentTrue = DynamicParsed.getParsedSql(ruleSql);
        SqlBuilder builderTrue = segmentTrue.buildQuery(paramsTrue, new TestQueryContext());

        String generatedSqlTrue = builderTrue.getSqlString();
        assert StringUtils.equals(generatedSqlTrue, "where  password =   ?");

        Object valTrue = ((SqlArg) builderTrue.getArgs()[0]).getValue();
        Assert.assertEquals("True Case MD5 mismatch", "e10adc3949ba59abbe56e057f20f883e", valTrue);

        // Case 2: encryptMode = false -> original value
        Map<String, Object> paramsFalse = new HashMap<>();
        paramsFalse.put("encryptMode", false);
        paramsFalse.put("pwd", "123456");

        PlanDynamicSql segmentFalse = DynamicParsed.getParsedSql(ruleSql);
        SqlBuilder builderFalse = segmentFalse.buildQuery(paramsFalse, new TestQueryContext());

        String generatedSqlFalse = builderFalse.getSqlString();
        assert StringUtils.equals(generatedSqlTrue, "where  password =   ?");

        Object valFalse = ((SqlArg) builderFalse.getArgs()[0]).getValue();
        Assert.assertEquals("False Case mismatch", "123456", valFalse);
    }

    @Test
    public void nestedErrorQuoteTest() throws SQLException {
        // 错误写法
        String sql = "pwd = '@{md5, :val}'";
        Map<String, Object> params = new HashMap<>();
        params.put("val", "123");

        String expectedSql = "pwd = '@{md5, :val}'";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        SqlBuilder sqlBuilder = segment.buildQuery(params, new TestQueryContext());

        String generatedSql = sqlBuilder.getSqlString();
        assert StringUtils.equals(generatedSql, expectedSql);
        Assert.assertEquals("Arg count mismatch", 0, sqlBuilder.getArgs().length);
    }
}
