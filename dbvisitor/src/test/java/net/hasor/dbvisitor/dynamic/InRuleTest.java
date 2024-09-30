package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.InRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.test.types.MyTypeHandler;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class InRuleTest {
    @Test
    public void ruleTest_char_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new char[] { 'a', 'b', 'c' });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals('a');
        assert sqlBuilder1.getArgs()[1].equals('b');
        assert sqlBuilder1.getArgs()[2].equals('c');

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Character[] { 'a', 'b', 'c' });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals('a');
        assert sqlBuilder2.getArgs()[1].equals('b');
        assert sqlBuilder2.getArgs()[2].equals('c');
    }

    @Test
    public void ruleTest_char_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ ifin,test,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new char[] { 'a', 'b', 'c' }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals('a');
        assert sqlBuilder1.getArgs()[1].equals('b');
        assert sqlBuilder1.getArgs()[2].equals('c');

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Character[] { 'a', 'b', 'c' }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals('a');
        assert sqlBuilder2.getArgs()[1].equals('b');
        assert sqlBuilder2.getArgs()[2].equals('c');
    }

    @Test
    public void ruleTest_char_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin ,test,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new char[] { 'a', 'b', 'c' }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Character[] { 'a', 'b', 'c' }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_short_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in , :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new short[] { (short) 1, (short) 2, (short) 3 });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals((short) 1);
        assert sqlBuilder1.getArgs()[1].equals((short) 2);
        assert sqlBuilder1.getArgs()[2].equals((short) 3);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Short[] { (short) 1, (short) 2, (short) 3 });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals((short) 1);
        assert sqlBuilder2.getArgs()[1].equals((short) 2);
        assert sqlBuilder2.getArgs()[2].equals((short) 3);
    }

    @Test
    public void ruleTest_short_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test ,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new short[] { (short) 1, (short) 2, (short) 3 }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals((short) 1);
        assert sqlBuilder1.getArgs()[1].equals((short) 2);
        assert sqlBuilder1.getArgs()[2].equals((short) 3);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Short[] { (short) 1, (short) 2, (short) 3 }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals((short) 1);
        assert sqlBuilder2.getArgs()[1].equals((short) 2);
        assert sqlBuilder2.getArgs()[2].equals((short) 3);
    }

    @Test
    public void ruleTest_short_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test,:array }");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new short[] { (short) 1, (short) 2, (short) 3 }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Short[] { (short) 1, (short) 2, (short) 3 }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_int_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ in , :array }");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new int[] { 1, 2, 3 });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?) ");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1);
        assert sqlBuilder1.getArgs()[1].equals(2);
        assert sqlBuilder1.getArgs()[2].equals(3);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Integer[] { 1, 2, 3 });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?) ");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1);
        assert sqlBuilder2.getArgs()[1].equals(2);
        assert sqlBuilder2.getArgs()[2].equals(3);
    }

    @Test
    public void ruleTest_int_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ ifin , test , :array }");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new int[] { 1, 2, 3 }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?) ");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1);
        assert sqlBuilder1.getArgs()[1].equals(2);
        assert sqlBuilder1.getArgs()[2].equals(3);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Integer[] { 1, 2, 3 }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?) ");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1);
        assert sqlBuilder2.getArgs()[1].equals(2);
        assert sqlBuilder2.getArgs()[2].equals(3);
    }

    @Test
    public void ruleTest_int_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin, test, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new int[] { 1, 2, 3 }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Integer[] { 1, 2, 3 }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_long_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new long[] { 1, 2, 3 });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1L);
        assert sqlBuilder1.getArgs()[1].equals(2L);
        assert sqlBuilder1.getArgs()[2].equals(3L);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Long[] { 1L, 2L, 3L });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1L);
        assert sqlBuilder2.getArgs()[1].equals(2L);
        assert sqlBuilder2.getArgs()[2].equals(3L);
    }

    @Test
    public void ruleTest_long_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin, test, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new long[] { 1, 2, 3 }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1L);
        assert sqlBuilder1.getArgs()[1].equals(2L);
        assert sqlBuilder1.getArgs()[2].equals(3L);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Long[] { 1L, 2L, 3L }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1L);
        assert sqlBuilder2.getArgs()[1].equals(2L);
        assert sqlBuilder2.getArgs()[2].equals(3L);
    }

    @Test
    public void ruleTest_long_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new long[] { 1, 2, 3 }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Long[] { 1L, 2L, 3L }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_float_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new float[] { 1f, 2f, 3f });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1f);
        assert sqlBuilder1.getArgs()[1].equals(2f);
        assert sqlBuilder1.getArgs()[2].equals(3f);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Float[] { 1f, 2f, 3f });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1f);
        assert sqlBuilder2.getArgs()[1].equals(2f);
        assert sqlBuilder2.getArgs()[2].equals(3f);
    }

    @Test
    public void ruleTest_float_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new float[] { 1f, 2f, 3f }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1f);
        assert sqlBuilder1.getArgs()[1].equals(2f);
        assert sqlBuilder1.getArgs()[2].equals(3f);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Float[] { 1f, 2f, 3f }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1f);
        assert sqlBuilder2.getArgs()[1].equals(2f);
        assert sqlBuilder2.getArgs()[2].equals(3f);
    }

    @Test
    public void ruleTest_float_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin, test, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new float[] { 1f, 2f, 3f }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Float[] { 1f, 2f, 3f }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_double_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new double[] { 1d, 2d, 3d });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1d);
        assert sqlBuilder1.getArgs()[1].equals(2d);
        assert sqlBuilder1.getArgs()[2].equals(3d);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Double[] { 1d, 2d, 3d });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1d);
        assert sqlBuilder2.getArgs()[1].equals(2d);
        assert sqlBuilder2.getArgs()[2].equals(3d);
    }

    @Test
    public void ruleTest_double_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test,:array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new double[] { 1d, 2d, 3d }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(1d);
        assert sqlBuilder1.getArgs()[1].equals(2d);
        assert sqlBuilder1.getArgs()[2].equals(3d);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Double[] { 1d, 2d, 3d }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("(?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(1d);
        assert sqlBuilder2.getArgs()[1].equals(2d);
        assert sqlBuilder2.getArgs()[2].equals(3d);
    }

    @Test
    public void ruleTest_double_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin , test , :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new double[] { 1d, 2d, 3d }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Double[] { 1d, 2d, 3d }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_boolean_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new boolean[] { true, false, true });
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(true);
        assert sqlBuilder1.getArgs()[1].equals(false);
        assert sqlBuilder1.getArgs()[2].equals(true);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Boolean[] { true, false, true });
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(true);
        assert sqlBuilder2.getArgs()[1].equals(false);
        assert sqlBuilder2.getArgs()[2].equals(true);
    }

    @Test
    public void ruleTest_boolean_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new boolean[] { true, false, true }, "test", true);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder1.getArgs().length == 3;
        assert sqlBuilder1.getArgs()[0].equals(true);
        assert sqlBuilder1.getArgs()[1].equals(false);
        assert sqlBuilder1.getArgs()[2].equals(true);

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Boolean[] { true, false, true }, "test", true);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder2.getArgs().length == 3;
        assert sqlBuilder2.getArgs()[0].equals(true);
        assert sqlBuilder2.getArgs()[1].equals(false);
        assert sqlBuilder2.getArgs()[2].equals(true);
    }

    @Test
    public void ruleTest_boolean_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin, test, :array}");

        Map<String, Object> ctx1 = CollectionUtils.asMap("array", new boolean[] { true, false, true }, "test", false);
        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("");
        assert sqlBuilder1.getArgs().length == 0;

        Map<String, Object> ctx2 = CollectionUtils.asMap("array", new Boolean[] { true, false, true }, "test", false);
        SqlBuilder sqlBuilder2 = segment.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void ruleTest_string_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", new Object[] { "1", "2", "3" });

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder.getArgs().length == 3;
        assert sqlBuilder.getArgs()[0].equals("1");
        assert sqlBuilder.getArgs()[1].equals("2");
        assert sqlBuilder.getArgs()[2].equals("3");
    }

    @Test
    public void ruleTest_string_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test, :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", new Object[] { "1", "2", "3" }, "test", true);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder.getArgs().length == 3;
        assert sqlBuilder.getArgs()[0].equals("1");
        assert sqlBuilder.getArgs()[1].equals("2");
        assert sqlBuilder.getArgs()[2].equals("3");
    }

    @Test
    public void ruleTest_string_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,test, :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", new Object[] { "1", "2", "3" }, "test", false);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void ruleTest_list_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Arrays.asList("1", "2", "3"));

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals(" (?, ?, ?)");
        assert sqlBuilder.getArgs().length == 3;
        assert sqlBuilder.getArgs()[0].equals("1");
        assert sqlBuilder.getArgs()[1].equals("2");
        assert sqlBuilder.getArgs()[2].equals("3");
    }

    @Test
    public void ruleTest_1() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");
        Map<String, Object> ctx = Collections.emptyMap();

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void ruleTest_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void ruleTest_3() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in,id in :array}");
        SqlArg arg = new SqlArg(Arrays.asList("1", "2"), 123, new MyTypeHandler());
        Map<String, Object> ctx = CollectionUtils.asMap("array", arg);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("id in (?, ?)");
        assert sqlBuilder.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("1");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getJdbcType() == 123;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getTypeHandler() == arg.getTypeHandler();
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("2");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getJdbcType() == 123;
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getTypeHandler() == arg.getTypeHandler();

        assert ((SqlArg) sqlBuilder.getArgs()[0]).getTypeHandler() == ((SqlArg) sqlBuilder.getArgs()[1]).getTypeHandler();
    }

    @Test
    public void badTest_1() {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{in, :array , :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());

        try {
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role IN args error, require 1, but 2");
        }
    }

    @Test
    public void badTest_2() throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin, false, :array , :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());
        segment.buildQuery(ctx, new DynamicContext());
    }

    @Test
    public void badTest_3() {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("@{ifin,true, :array , :array}");
        Map<String, Object> ctx = CollectionUtils.asMap("array", Collections.emptyList());

        try {
            segment.buildQuery(ctx, new DynamicContext());
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("role IFIN args error, require 1, but 2");
        }
    }

    @Test
    public void toStringTest_1() {
        assert new InRule(false).toString().startsWith("in [");
        assert new InRule(true).toString().startsWith("ifin [");
    }
}
