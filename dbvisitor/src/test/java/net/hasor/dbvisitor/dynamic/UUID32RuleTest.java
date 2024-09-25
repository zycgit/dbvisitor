package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.UUID32Rule;
import net.hasor.dbvisitor.types.MappedArg;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class UUID32RuleTest {

    @Test
    public void uuid32RuleTest_1() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{uuid32}").buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder1.getArgs()[0]).getValue().toString().length() == 32;

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{uuid32,:data}").buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder2.getArgs()[0]).getValue().toString().length() == 32;

        // // "id =" will be ignored.
        Map<String, Object> ctx3 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{uuid32, id = :data}").buildQuery(ctx3, new DynamicContext());
        assert sqlBuilder3.getSqlString().equals("?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder3.getArgs()[0]).getValue().toString().length() == 32;

        //
        String s1 = ((MappedArg) sqlBuilder1.getArgs()[0]).getValue().toString();
        String s2 = ((MappedArg) sqlBuilder2.getArgs()[0]).getValue().toString();
        String s3 = ((MappedArg) sqlBuilder3.getArgs()[0]).getValue().toString();
        assert !s1.equals(s2);
        assert !s2.equals(s3);
        assert !s1.equals(s3);
    }

    @Test
    public void ifuuid32RuleTest_1() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc", "test", true);
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifuuid32}").buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder1.getArgs()[0]).getValue().toString().length() == 32;

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc", "test", true);
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifuuid32,test,:data}").buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder2.getArgs()[0]).getValue().toString().length() == 32;

        // // "id =" will be ignored.
        Map<String, Object> ctx3 = CollectionUtils.asMap("data", "abc", "test", true);
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifuuid32,test, id = :data}").buildQuery(ctx3, new DynamicContext());
        assert sqlBuilder3.getSqlString().equals("?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((MappedArg) sqlBuilder3.getArgs()[0]).getValue().toString().length() == 32;

        //
        String s1 = ((MappedArg) sqlBuilder1.getArgs()[0]).getValue().toString();
        String s2 = ((MappedArg) sqlBuilder2.getArgs()[0]).getValue().toString();
        String s3 = ((MappedArg) sqlBuilder3.getArgs()[0]).getValue().toString();
        assert !s1.equals(s2);
        assert !s2.equals(s3);
        assert !s1.equals(s3);
    }

    @Test
    public void ifuuid32RuleTest_2() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc", "test", false);
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{ifuuid32,test}").buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc", "test", false);
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{ifuuid32,test,:data}").buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;

        // // "id = :data" will be ignored.
        Map<String, Object> ctx3 = CollectionUtils.asMap("data", "abc", "test", false);
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{ifuuid32,test, id = :data}").buildQuery(ctx3, new DynamicContext());
        assert sqlBuilder3.getSqlString().equals("");
        assert sqlBuilder3.getArgs().length == 0;
    }

    @Test
    public void toStringTest_1() {
        assert new UUID32Rule(false).toString().startsWith("uuid32 [");
        assert new UUID32Rule(true).toString().startsWith("ifuuid32 [");
    }
}
