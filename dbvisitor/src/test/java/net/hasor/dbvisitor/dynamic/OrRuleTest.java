package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.OrRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class OrRuleTest {
    @Test
    public void ruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{or,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{or, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{or,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{or, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{or,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{or, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{or,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{or, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123");

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{or,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where 1=1 or name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{or, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where 1=1 or  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{or,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where 1=1 or name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{or, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where 1=1 or  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        DefaultSqlSegment sqlSegment1 = DynamicParsed.getParsedSql("@{or,name = :name} and @{or,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ? and age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123");

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{or}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{or,abc}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{or,:name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where ?");
        assert sqlBuilder3.getArgs().length == 1;
    }

    @Test
    public void ifruleTest_1() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifor,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifor,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifor,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_1_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifor,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifor,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifor,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_2() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{ifor,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where  name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{ifor,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where   name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{ifor,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where  name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where   name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_2_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where  @{ifor,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where  ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where  @{ifor,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where  ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where  @{ifor,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where  ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where  @{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where  ");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_3() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", true);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where 1=1 or name = ?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where 1=1 or  name = ?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("abc");

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where 1=1 or name = ?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().equals("123");

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where 1=1 or  name = ?");
        assert sqlBuilder4.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder4.getArgs()[0]).getValue().equals("123");

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder5.getSqlString().equals("where name = ? and   name = ?");
        assert sqlBuilder5.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder5.getArgs()[1]).getValue().equals("123");
    }

    @Test
    public void ifruleTest_3_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg0", "123", "test", false);

        //
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test,name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where 1=1 ");
        assert sqlBuilder1.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test, name = :name}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("where 1=1 ");
        assert sqlBuilder2.getArgs().length == 0;

        //
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test,name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("where 1=1 ");
        assert sqlBuilder3.getArgs().length == 0;

        // more space char
        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("where 1=1 @{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where 1=1 ");
        assert sqlBuilder4.getArgs().length == 0;

        SqlBuilder sqlBuilder5 = DynamicParsed.getParsedSql("where name = :name and  @{ifor,test, name = ?}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder5.getSqlString().equals("where name = ? and  ");
        assert sqlBuilder5.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder5.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void ifruleTest_4() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        DefaultSqlSegment sqlSegment1 = DynamicParsed.getParsedSql("@{ifor,test,name = :name} and @{ifor,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("where name = ? and age = ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue() == null; // TODO The use of both rule and location parameters is not supported.
    }

    @Test
    public void ifruleTest_4_no() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", false);

        DefaultSqlSegment sqlSegment1 = DynamicParsed.getParsedSql("@{ifor,test,name = :name} and @{ifor,test,age = ?}");
        SqlBuilder sqlBuilder1 = sqlSegment1.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals(" and ");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifruleTest_5() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc", "arg1", "123", "test", true);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifor}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifor,test}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifor,abc}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;

        SqlBuilder sqlBuilder4 = DynamicParsed.getParsedSql("@{ifor,test,abc}").buildQuery(ctx, new RegistryManager());
        assert sqlBuilder4.getSqlString().equals("where abc");
        assert sqlBuilder4.getArgs().length == 0;
    }

    @Test
    public void toStringTest_1() {
        assert new OrRule(false).toString().startsWith("or [");
        assert new OrRule(true).toString().startsWith("ifor [");
    }
}
