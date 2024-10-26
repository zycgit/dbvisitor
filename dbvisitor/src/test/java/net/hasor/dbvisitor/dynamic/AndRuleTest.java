package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.AndRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class AndRuleTest {
    @Test
    public void ruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{and,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{and, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{and,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{and, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{and,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{and, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{and,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{and, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{and,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{and, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{and,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{and, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        DefaultSqlSegment sqlSegment1 = DynamicParsed.getParsedSql("@{and,name = :name} and @{and,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ? and age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{and}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{and,abc}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{and,:name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where ?");
        assert sqlBuilder3.getArgs().length == 1;
    }

    @Test
    public void ifruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifand,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifand,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_1_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifand,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifand,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{ifand,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{ifand,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{ifand,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_2_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{ifand,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where  ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{ifand,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where  ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{ifand,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where  ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where  ");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where 1=1 and name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where 1=1 and  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder5.getSqlString().equals("where name = ? and   name = ?");
        assert sqlBuilder5.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder5.getArgs()[1]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_3_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where 1=1 ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where 1=1 ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where 1=1 ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where 1=1 ");
        assert sqlBuilder4.getArgs().length == 0;

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifand,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder5.getSqlString().equals("where name = ? and  ");
        assert sqlBuilder5.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void ifruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        DefaultSqlSegment sqlSegment1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name} and @{ifand,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ? and age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ifruleTest_4_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", false);

        DefaultSqlSegment sqlSegment1 = DynamicParsed.getParsedSql("@{ifand,test,name = :name} and @{ifand,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals(" and ");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifand}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifand,test}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifand,abc}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifand,test,abc}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where abc");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void toStringTest_1() {
        assert new AndRule(false).toString().startsWith("and [");
        assert new AndRule(true).toString().startsWith("ifand [");
    }
}
