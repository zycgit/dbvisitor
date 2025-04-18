package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.UUID36Rule;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class UUID36RuleTest {

    @Test
    public void uuid36RuleTest_1() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder1 = DynamicParsed.getParsedSql("@{uuid36}").buildQuery(ctx1, new TestQueryContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().toString().length() == 36;

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder2 = DynamicParsed.getParsedSql("@{uuid36,:data}").buildQuery(ctx2, new TestQueryContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().toString().length() == 36;

        // // "id =" will be ignored.
        Map<String, Object> ctx3 = CollectionUtils.asMap("data", "abc");
        SqlBuilder sqlBuilder3 = DynamicParsed.getParsedSql("@{uuid36, id = :data}").buildQuery(ctx3, new TestQueryContext());
        assert sqlBuilder3.getSqlString().equals("?");
        assert sqlBuilder3.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().toString().length() == 36;

        //
        String s1 = ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().toString();
        String s2 = ((SqlArg) sqlBuilder2.getArgs()[0]).getValue().toString();
        String s3 = ((SqlArg) sqlBuilder3.getArgs()[0]).getValue().toString();
        assert !s1.equals(s2);
        assert !s2.equals(s3);
        assert !s1.equals(s3);
    }

    @Test
    public void toStringTest_1() {
        assert new UUID36Rule().toString().startsWith("uuid36 [");
    }
}
