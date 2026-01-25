package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.MD5Rule;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

public class MD5RuleTest {

    @Test
    public void md5RuleTest_1() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{md5,:data}").buildQuery(ctx1, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("900150983cd24fb0d6963f7d28e17f72");

        // // "id =" will be ignored.
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{md5, id = :data}").buildQuery(ctx2, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().equals("900150983cd24fb0d6963f7d28e17f72");
    }

    @Test
    public void badTest_1() {
        PlanDynamicSql segment = DynamicParsed.getParsedSql("@{md5,:array:array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());

        try {
            segment.buildQuery(ctx, new TestQueryContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role MD5 args error, require 1, but 2");
        }
    }

    @Test
    public void badTest_2() {
        try {
            PlanDynamicSql segment = DynamicParsed.getParsedSql("@{md5,'abc'}");
            Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
            segment.buildQuery(ctx, new TestQueryContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role MD5 args error, require 1, but 0");
        }

        try {
            PlanDynamicSql segment = DynamicParsed.getParsedSql("@{md5}");
            Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
            segment.buildQuery(ctx, new TestQueryContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role MD5 args error, require 1, but 0");
        }
    }

    @Test
    public void toStringTest_1() {
        assert new MD5Rule().toString().startsWith("md5 [");
    }
}
