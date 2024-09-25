package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.MD5Rule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.types.MappedArg;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class MD5RuleTest {

    @Test
    public void md5RuleTest_1() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{md5,:data}").buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder1.getArgs()[0]).getValue().equals("6cd3f2864a1e06fa55cf1d7657196a89");

        // // "id =" will be ignored.
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{md5, id = :data}").buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder2.getArgs()[0]).getValue().equals("6cd3f2864a1e06fa55cf1d7657196a89");
    }

    @Test
    public void ifmd5RuleTest_1() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc", "test", true);
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifmd5,test,:data}").buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder1.getArgs()[0]).getValue().equals("6cd3f2864a1e06fa55cf1d7657196a89");

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc", "test", false);
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifmd5,test,:data}").buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void badTest_1() {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{md5,:array:array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());

        try {
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role MD5 args error, require 1, but 2");
        }
    }

    @Test
    public void badTest_2() {
        try {
            DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{md5,'abc'}");
            Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role MD5 args error, require 1, but 0");
        }

        try {
            DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{md5}");
            Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role MD5 args error, require 1, but 0");
        }
    }

    @Test
    public void badTest_3() {
        try {
            DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifmd5,true,'abc'}");
            Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role IFMD5 args error, require 1, but 0");
        }

        try {
            DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifmd5,true}");
            Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role IFMD5 args error, require 1, but 0");
        }
    }

    @Test
    public void toStringTest_1() {
        assert new MD5Rule(false).toString().startsWith("md5 [");
        assert new MD5Rule(true).toString().startsWith("ifmd5 [");
    }
}
