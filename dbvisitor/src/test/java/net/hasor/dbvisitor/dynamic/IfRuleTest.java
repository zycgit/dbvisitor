package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.IfRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class IfRuleTest {

    @Test
    public void ifRuleTest_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{if,,'abc'}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(Collections.emptyMap(), new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("'abc'");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifRuleTest_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{if,,:data}");
        Map<String, Object> ctx = CollectionUtils.asMap("data", "abc");

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert sqlBuilder1.getArgs()[0].equals("abc");
    }

    @Test
    public void ifRuleTest_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{if,false,:data}");
        Map<String, Object> ctx = CollectionUtils.asMap("data", "abc");

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifRuleTest_4() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{if,false,'abc'}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(Collections.emptyMap(), new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ifRuleTest_5() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{if,test,:data}");
        Map<String, Object> ctx = CollectionUtils.asMap("data", "abc", "test", true);

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert sqlBuilder1.getArgs()[0].equals("abc");
    }

    @Test
    public void ifRuleTest_6() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{if,test,:data}");
        Map<String, Object> ctx = CollectionUtils.asMap("data", "abc", "test", false);

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void toStringTest_1() {
        assert IfRule.INSTANCE_IF.toString().startsWith("if [");
    }
}
