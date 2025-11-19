package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.PairsRule;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

public class PairsRuleTest {
    @Test
    public void pairsTest_1() throws SQLException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "abc");
        data.put("arg0", "123");
        Map<String, Object> ctx = CollectionUtils.asMap("data", data);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("mset @{pairs,:data,:k :v}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("mset ? ? ? ?");
        assert sqlBuilder1.getArgs().length == 4;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[2]).getValue().equals("arg0");
        assert ((SqlArg) sqlBuilder1.getArgs()[3]).getValue().equals("123");
    }

    @Test
    public void pairsTest_2() throws SQLException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "abc");
        data.put("arg0", "123");
        Map<String, Object> ctx = CollectionUtils.asMap("data", data);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("mset @{pairs,:data,:k,:v}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("mset ?,? ?,?");
        assert sqlBuilder1.getArgs().length == 4;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[2]).getValue().equals("arg0");
        assert ((SqlArg) sqlBuilder1.getArgs()[3]).getValue().equals("123");
    }

    @Test
    public void pairsTest_3() throws SQLException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "abc");
        data.put("arg0", "123");
        Map<String, Object> ctx = CollectionUtils.asMap("data", data);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("mset @{pairs,:data,:k}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("mset ? ?");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue().equals("arg0");
    }

    @Test
    public void pairsTest_4() throws SQLException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "abc");
        data.put("arg0", "123");
        Map<String, Object> ctx = CollectionUtils.asMap("data", data);

        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("mset @{pairs,:data,:k :v}").buildQuery(ctx, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("mset ? ? ? ?");
        assert sqlBuilder1.getArgs().length == 4;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue().equals("abc");
        assert ((SqlArg) sqlBuilder1.getArgs()[2]).getValue().equals("arg0");
        assert ((SqlArg) sqlBuilder1.getArgs()[3]).getValue().equals("123");
    }

    @Test
    public void toStringTest_1() {
        assert new PairsRule().toString().startsWith("pairs [");
    }
}
