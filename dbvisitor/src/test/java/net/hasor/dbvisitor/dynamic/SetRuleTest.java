package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.SetRule;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class SetRuleTest {
    @Test
    public void ruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{set,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{set, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{set,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{set, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("set  @{set,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("set  @{set, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("set  @{set,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("set  @{set, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("set 1=1 @{set,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set 1=1 , name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("set 1=1 @{set, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set 1=1 ,  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("set 1=1 @{set,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set 1=1 , name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("set 1=1 @{set, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set 1=1 ,  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        PlanDynamicSql sqlSegment1 = DynamicParsed.getParsedSql("@{set,name = :name} , @{set,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set name = ? , age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.

        //
        PlanDynamicSql sqlSegment2 = DynamicParsed.getParsedSql("@{set,name = :name} @{set,age = ?}");
        SqlBuilder sqlBuilder2 = sqlSegment2.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set name = ? , age = ?");
        assert sqlBuilder2.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder2.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{set}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{set,abc}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set abc");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{set,:name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set ?");
        assert sqlBuilder3.getArgs().length == 1;
    }

    @Test
    public void ifruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifset,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifset,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifset,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_1_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifset,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifset,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifset,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("set  @{ifset,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("set  @{ifset,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("set  @{ifset,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("set  @{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_2_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("set  @{ifset,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set  ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("set  @{ifset,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set  ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("set  @{ifset,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set  ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("set  @{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set  ");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("set 1=1 @{ifset,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set 1=1 , name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("set 1=1 @{ifset,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("set 1=1 ,  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("set 1=1 @{ifset,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("set 1=1 , name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("set 1=1 @{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set 1=1 ,  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("set name = :name ,  @{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder5.getSqlString().equals("set name = ? ,   name = ?");
        assert sqlBuilder5.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder5.getArgs()[1]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_3_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifset,test,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where 1=1 ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifset,test, name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("where 1=1 ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifset,test,name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("where 1=1 ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("where 1=1 ");
        assert sqlBuilder4.getArgs().length == 0;

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifset,test, name = ?}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder5.getSqlString().equals("where name = ? and  ");
        assert sqlBuilder5.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void ifruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        PlanDynamicSql sqlSegment1 = DynamicParsed.getParsedSql("@{ifset,test,name = :name} , @{ifset,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set name = ? , age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ifruleTest_4_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", false);

        PlanDynamicSql sqlSegment1 = DynamicParsed.getParsedSql("@{ifset,test,name = :name} , @{ifset,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals(" , ");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifset}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifset,test}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifset,abc}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifset,test,abc}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder4.getSqlString().equals("set abc");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void toStringTest_1() {
        assert new SetRule(false).toString().startsWith("set [");
        assert new SetRule(true).toString().startsWith("ifset [");
    }

    @Test
    public void badSqlTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where @{set,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("where set name = ?");
        assert sqlBuilder1.getArgs().length == 1;
    }

    @Test
    public void badSqlTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{set,name = :name} and @{set,name = :name}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("set name = ? and , name = ?");
        assert sqlBuilder1.getArgs().length == 2;
    }
}
