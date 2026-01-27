package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.AndRule;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

public class AndRuleTest {
    @Test
    public void ruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{and,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{and, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{and,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{and, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{and,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{and, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{and,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{and, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{and,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{and, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{and,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{and, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        PlanDynamicSql sqlSegment1 = DynamicParsed.getParsedSql("@{and,name = :name} and @{and,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where name = ? and age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{and}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{and,abc}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{and,:name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where ?");
        assert sqlBuilder3.getArgs().length == 1;
    }

    @Test
    public void ruleTest_6() throws SQLException {
        // Case 3: idList has values
        Map<String, Object> ctxValues = new HashMap<>();
        ctxValues.put("idList", Arrays.asList(1, 2, 3));

        String sqlDebug = "SELECT * FROM tb_user @{and, id IN @{in, :idList}}";
        PlanDynamicSql segDebug = DynamicParsed.getParsedSql(sqlDebug);
        SqlBuilder sbDebug = segDebug.buildQuery(ctxValues, new TestQueryContext());

        assert sbDebug.getSqlString().trim().equals("SELECT * FROM tb_user where  id IN  (?, ?, ?)");
        assert sbDebug.getArgs().length == 3;
    }

    @Test
    public void ifruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifand,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifand,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_1_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifand,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifand,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{ifand,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{ifand,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{ifand,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_2_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{ifand,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where  ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{ifand,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where  ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{ifand,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where  ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where  ");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder5.getSqlString().equals("where name = ? and   name = ?");
        assert sqlBuilder5.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder5.getArgs()[1]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_3_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where 1=1 ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where 1=1 ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where 1=1 ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where 1=1 ");
        assert sqlBuilder4.getArgs().length == 0;

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifand,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder5.getSqlString().equals("where name = ? and  ");
        assert sqlBuilder5.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void ifruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        PlanDynamicSql sqlSegment1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name} and @{ifand,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where name = ? and age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ifruleTest_4_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", false);

        PlanDynamicSql sqlSegment1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name} and @{ifand,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals(" and ");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifand}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifand,test}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifand,abc}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifand,test,abc}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where abc");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void testIfAndWithNullList() throws SQLException {
        String sqlTemplate = "SELECT * FROM tb_user @{ifand, idList, AND id IN @{in, :idList}}";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sqlTemplate);

        // Case 1: idList is null
        Map<String, Object> ctxNull = new HashMap<>();
        ctxNull.put("idList", null);
        SqlBuilder sbNull = segment.buildQuery(ctxNull, new TestQueryContext());
        System.out.println("Null idList SQL: " + sbNull.getSqlString());
        assert sbNull.getSqlString().trim().equals("SELECT * FROM tb_user");
    }

    @Test
    public void testIfAndWithEmptyList() throws SQLException {
        String sqlTemplate = "SELECT * FROM tb_user @{ifand, idList, AND id IN @{in, :idList}}";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sqlTemplate);

        // Case 2: idList is empty list
        Map<String, Object> ctxEmpty = new HashMap<>();
        ctxEmpty.put("idList", new ArrayList<>());
        SqlBuilder sbEmpty = segment.buildQuery(ctxEmpty, new TestQueryContext());
        System.out.println("Empty idList SQL: " + sbEmpty.getSqlString());
        assert sbEmpty.getSqlString().trim().equals("SELECT * FROM tb_user");
    }

    @Test
    public void testIfAndWithValues_1() throws SQLException {
        // Case 3: idList has values
        Map<String, Object> ctxValues = new HashMap<>();
        ctxValues.put("idList", Arrays.asList(1, 2, 3));

        String sqlDebug = "SELECT * FROM tb_user @{ifand, true, id IN @{in, :idList}}";
        PlanDynamicSql segDebug = DynamicParsed.getParsedSql(sqlDebug);
        SqlBuilder sbDebug = segDebug.buildQuery(ctxValues, new TestQueryContext());

        assert sbDebug.getSqlString().trim().equals("SELECT * FROM tb_user where  id IN  (?, ?, ?)");
        assert sbDebug.getArgs().length == 3;
    }

    @Test
    public void testIfAndWithSizeCheck() throws SQLException {
        Map<String, Object> ctxValues = new HashMap<>();
        ctxValues.put("idList", Arrays.asList(1, 2, 3));

        // Debug 2: Try with OGNL size check
        String sqlSize = "SELECT * FROM tb_user @{ifand, idList.size() > 0, id IN @{in, :idList}}";
        PlanDynamicSql segSize = DynamicParsed.getParsedSql(sqlSize);
        SqlBuilder sbSize = segSize.buildQuery(ctxValues, new TestQueryContext());
        System.out.println("Debug (size > 0) SQL: " + sbSize.getSqlString());
        assert "SELECT * FROM tb_user where  id IN  (?, ?, ?)".equals(sbSize.getSqlString());
        assert sbSize.getArgs().length == 3;
    }

    @Test
    public void testIfAndWithNotEmptyCheck() throws SQLException {
        Map<String, Object> ctxValues = new HashMap<>();
        ctxValues.put("idList", Arrays.asList(1, 2, 3));

        // Debug 3: Try with isEmpty check and NO manual AND in body
        // OGNL: !idList.isEmpty()
        String sqlEmptyCheck = "SELECT * FROM tb_user @{ifand, !idList.isEmpty(), id IN @{in, :idList}}";
        PlanDynamicSql segEmptyCheck = DynamicParsed.getParsedSql(sqlEmptyCheck);
        SqlBuilder sbEmptyCheck = segEmptyCheck.buildQuery(ctxValues, new TestQueryContext());
        System.out.println("Debug (!isEmpty) SQL: " + sbEmptyCheck.getSqlString());

        // verify parameter expansion using the working SQL (Debug 3)
        assert "SELECT * FROM tb_user where  id IN  (?, ?, ?)".equals(sbEmptyCheck.getSqlString());
        assert sbEmptyCheck.getArgs().length == 3;
    }

    @Test
    public void testIfAndSmartPrefix() throws SQLException {
        // 1. Start of WHERE Clause -> @{ifand} should detect missing WHERE and inject it
        String sql1 = "SELECT * FROM tb_user @{ifand, idList != null, id IN @{in, :idList}}";
        PlanDynamicSql seg1 = DynamicParsed.getParsedSql(sql1);
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("idList", Arrays.asList(1));

        SqlBuilder sb1 = seg1.buildQuery(ctx, new TestQueryContext());
        // Expect: ... where id IN (?)
        assert "SELECT * FROM tb_user where  id IN  (?)".equals(sb1.getSqlString().trim());
        assert sb1.getArgs().length == 1;

        // 2. Existing WHERE Clause -> @{ifand} should detect existing WHERE and inject AND
        String sql2 = "SELECT * FROM tb_user WHERE name = 'abc' @{ifand, idList != null, id IN @{in, :idList}}";
        PlanDynamicSql seg2 = DynamicParsed.getParsedSql(sql2);

        SqlBuilder sb2 = seg2.buildQuery(ctx, new TestQueryContext());
        // Expect: ... WHERE name = 'abc' and id IN (?)
        assert "SELECT * FROM tb_user WHERE name = 'abc' and  id IN  (?)".equals(sb2.getSqlString().trim());
        assert sb2.getArgs().length == 1;
    }

    @Test
    public void testAndRuleWithEmptyList() throws SQLException {
        String sql = "SELECT * FROM tb_user @{and, id IN @{in, :idList}}";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        Map<String, Object> ctx = new HashMap<>();

        // 1. empty list -> @{in} produces nothing -> @{and} produces nothing
        ctx.put("idList", new ArrayList<>());
        SqlBuilder sbEmpty = segment.buildQuery(ctx, new TestQueryContext());
        assert "SELECT * FROM tb_user".equals(sbEmpty.getSqlString().trim());
        assert sbEmpty.getArgs().length == 0;

        // 2. null list -> @{in} produces nothing -> @{and} produces nothing
        ctx.put("idList", null);
        SqlBuilder sbNull = segment.buildQuery(ctx, new TestQueryContext());
        assert "SELECT * FROM tb_user".equals(sbNull.getSqlString().trim());
        assert sbNull.getArgs().length == 0;

        // 3. valid list -> @{in} produces (...) -> @{and} prepends "where" (Auto-prefix)
        ctx.put("idList", Arrays.asList(1, 2));
        SqlBuilder sbValid = segment.buildQuery(ctx, new TestQueryContext());
        assert "SELECT * FROM tb_user where  id IN  (?, ?)".equals(sbValid.getSqlString().trim());
        assert sbValid.getArgs().length == 2;
    }

    @Test
    public void toStringTest_1() {
        assert new AndRule(false).toString().startsWith("and [");
        assert new AndRule(true).toString().startsWith("ifand [");
    }
}
