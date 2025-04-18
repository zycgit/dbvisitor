package net.hasor.dbvisitor.dynamic;
import net.hasor.dbvisitor.dynamic.rule.TextRule;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;

public class TextRuleTest {
    @Test
    public void ruleTest_1() throws SQLException {
        PlanDynamicSql segment1 = DynamicParsed.getParsedSql("@{text,, :array}");
        SqlBuilder sqlBuilder1 = segment1.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals(", :array");

        //
        PlanDynamicSql segment2 = DynamicParsed.getParsedSql("@{text, :array}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals(" :array");

        //
        PlanDynamicSql segment3 = DynamicParsed.getParsedSql("@{text, :array,}");
        SqlBuilder sqlBuilder3 = segment3.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals(" :array,");
    }

    @Test
    public void ruleTest_2() throws SQLException {
        PlanDynamicSql segment = DynamicParsed.getParsedSql("@{text,false, :array}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("false, :array");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ruleTest_3() throws SQLException {
        PlanDynamicSql segment = DynamicParsed.getParsedSql("@{iftext,, :array}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals(" :array");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void ruleTest_4() throws SQLException {
        PlanDynamicSql segment = DynamicParsed.getParsedSql("@{iftext,false, :array}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;
    }

    @Test
    public void toStringTest_1() {
        assert new TextRule(false).toString().startsWith("text [");
        assert new TextRule(true).toString().startsWith("iftext [");
    }
}
