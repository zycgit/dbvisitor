package net.hasor.dbvisitor.dynamic;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Assert;
import org.junit.Test;

public class SafeTest {

    @Test
    public void testCommaInQuotes() throws SQLException {
        // Case 1: Comma inside quotes (String literal)
        // Expected: should be parsed as one expression "map['k1,k2']"
        // Current Split(",") behavior: splits into "map['k1" and "k2']" -> Error or malformed expr
        String sql = "select * from user where val = #{ 'a,b' }";

        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        try {
            SqlBuilder builder = segment.buildQuery(new HashMap<>(), new TestQueryContext());
            Object val = ((SqlArg) builder.getArgs()[0]).getValue();
            Assert.assertEquals("a,b", val);
        } catch (Exception e) {
            Assert.fail("Failed to parse comma in quotes: " + e.getMessage());
        }
    }

    @Test
    public void testCommaInFunction() throws SQLException {
        // Case 2: Comma inside function call arguments
        // Expected: should be parsed as one expression "testFunc(1, 2)"
        // Current Split(",") behavior: splits into "testFunc(1" and " 2)" -> Error
        String sql = "select * from user where val = #{ @net.hasor.dbvisitor.dynamic.CommaSensitivityTest@testFunc(1, 2) }";

        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        try {
            SqlBuilder builder = segment.buildQuery(new HashMap<>(), new TestQueryContext());
            Object val = ((SqlArg) builder.getArgs()[0]).getValue();
            Assert.assertEquals("3", val.toString());
        } catch (Exception e) {
            Assert.fail("Failed to parse comma in function args: " + e.getMessage());
        }
    }

    @Test
    public void testCurlyBraceInString() throws SQLException {
        // Fix verification: parser should ignore '}' inside quotes
        String sql = "select * from user where name = #{ '}' }";

        Map<String, Object> params = new HashMap<>(); // No params needed as value is literal

        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        SqlBuilder builder = segment.buildQuery(params, new TestQueryContext());

        String generatedSql = builder.getSqlString();
        // Should parse as one parameter
        // generatedSql should be "select * from user where name = ?"
        Assert.assertEquals("select * from user where name = ?", generatedSql.trim());

        Object[] args = builder.getArgs();
        Assert.assertEquals(1, args.length);
        Object arg = ((SqlArg) args[0]).getValue();
        Assert.assertEquals("}", arg.toString());
    }
}
