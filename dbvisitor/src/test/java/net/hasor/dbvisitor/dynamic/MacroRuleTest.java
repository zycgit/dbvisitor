package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.MacroRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class MacroRuleTest {

    @Test
    public void macroRuleTest_1() throws SQLException {
        RegistryManager dynamicContext = new RegistryManager();
        dynamicContext.addMacro("one arg", "name = ?");

        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{macro, one arg}");
        SqlBuilder sqlBuilder1 = segment.buildQuery(Collections.emptyMap(), dynamicContext);
        assert sqlBuilder1.getSqlString().equals("name = ?");
        assert sqlBuilder1.getArgs().length == 1;
    }

    @Test
    public void macroRuleTest_2() throws SQLException {
        RegistryManager dynamicContext = new RegistryManager();
        dynamicContext.addMacro("one arg", "name = ?");
        Map<String, Object> ctx = CollectionUtils.asMap("test", false);

        DefaultSqlSegment segment1 = DynamicParsed.getParsedSql("@{ifmacro, test}");
        SqlBuilder sqlBuilder1 = segment1.buildQuery(ctx, dynamicContext);
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        DefaultSqlSegment segment2 = DynamicParsed.getParsedSql("@{ifmacro,test,one arg}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx, dynamicContext);
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void macroRuleTest_3() throws SQLException {
        RegistryManager dynamicContext = new RegistryManager();
        dynamicContext.addMacro("one arg", "name = ?");
        Map<String, Object> ctx = CollectionUtils.asMap("test", true);

        try {
            DefaultSqlSegment segment1 = DynamicParsed.getParsedSql("@{ifmacro, test}");
            segment1.buildQuery(ctx, dynamicContext);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("ifmacro 'null' not found.");
        }

        DefaultSqlSegment segment2 = DynamicParsed.getParsedSql("@{ifmacro,test,one arg}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx, dynamicContext);
        assert sqlBuilder2.getSqlString().equals("name = ?");
        assert sqlBuilder2.getArgs().length == 1;
    }

    @Test
    public void toStringTest_1() {
        assert new MacroRule(false).toString().startsWith("macro [");
        assert new MacroRule(true).toString().startsWith("ifmacro [");
    }
}
