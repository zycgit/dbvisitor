package net.hasor.dbvisitor.dynamic;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.dynamic.segment.SqlModifier;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.BigDecimalTypeHandler;
import org.junit.Test;

public class DynamicTest {
    private static void assertArgCnt(int argCnt, String sql) throws SQLException {
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);

        SqlBuilder sqlBuilder = segment.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals(sql);
        assert sqlBuilder.getArgs().length == argCnt;

        for (int i = 0; i < sqlBuilder.getArgs().length; i++) {
            assert ((SqlArg) sqlBuilder.getArgs()[i]).getName().equals("arg" + i);
        }
    }

    @Test
    public void skip_Test_1() throws SQLException {
        assertArgCnt(0, " -- this is comment a = ? ");
        assertArgCnt(0, " /* this is comment a = ? ");
        assertArgCnt(0, " /* this is comment a = ?*/ ");

        assertArgCnt(0, " '' ");
        assertArgCnt(0, " 'abc' ");
        assertArgCnt(0, " 'aa''''''bb' ");

        assertArgCnt(0, " \"\" ");
        assertArgCnt(0, " \"abc\" ");
        assertArgCnt(0, " \"aa\"\"\"\"\"\"bb\" ");

        assertArgCnt(0, " \"aa'bb\" ");
        assertArgCnt(0, " 'aa\"bb' ");

        assertArgCnt(0, " 'aa''' '''bb' ");
    }

    @Test
    public void arg_Test_1() throws SQLException {
        // arg0
        assertArgCnt(0, "-- this is comment a = ?");
        assertArgCnt(0, "-- this is comment a = ? and b = ?");
        assertArgCnt(0, "/*-- this is comment a = ? and b = ?*/");
        assertArgCnt(0, "/* this is comment a = ? and b = ?"); // bad sql
        assertArgCnt(0, "/*-- this is comment a = ? and b = ?"); // bad sql

        //arg1
        assertArgCnt(1, "a = ? -- this is comment\n");
        assertArgCnt(1, "a = ? -- this is comment");
        assertArgCnt(1, "a = ? -- this is comment and b = ?");
        assertArgCnt(1, "-- this is comment \n b = ?");
        assertArgCnt(1, "'aa' = ?");
        assertArgCnt(1, "\"bb\" = ?");

        assertArgCnt(1, "a = ? /*this is comment\n*/");
        assertArgCnt(1, "a = ? /*this is comment and b = ?*/");
        assertArgCnt(1, "/*-- this is comment \n*/ b = ?");
        assertArgCnt(1, "/*-- this is comment */ b = ?");
        assertArgCnt(1, "-- a = ? /*this is comment \n b = ?*/"); // bad sql

        //arg2
        assertArgCnt(2, "a = ? -- this is comment\n and b = ?");
        assertArgCnt(2, "a = ? /* this is comment*/ and b = ?");
    }

    private static void assertArgNamedCnt(List<String> argNamed, String sql, String testSql) throws SQLException {
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);

        SqlBuilder sqlBuilder = segment.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals(testSql);
        assert sqlBuilder.getArgs().length == argNamed.size();

        for (int i = 0; i < sqlBuilder.getArgs().length; i++) {
            assert ((SqlArg) sqlBuilder.getArgs()[i]).getName().equals(argNamed.get(i));
        }
    }

    @Test
    public void named_Test_1() throws SQLException {
        // arg0
        assertArgNamedCnt(Collections.emptyList(),  //
                "-- this is comment a = :a",        //
                "-- this is comment a = :a");
        assertArgNamedCnt(Collections.emptyList(),      //
                "-- this is comment a = :a and b = :a", //
                "-- this is comment a = :a and b = :a");
        assertArgNamedCnt(Collections.emptyList(),         //
                "/*-- this is comment a = :a and b = :b*/",//
                "/*-- this is comment a = :a and b = :b*/");
        assertArgNamedCnt(Collections.emptyList(),     // bad sql
                "/* this is comment a = :a and b = :b",//
                "/* this is comment a = :a and b = :b");
        assertArgNamedCnt(Collections.emptyList(),       // bad sql
                "/*-- this is comment a = :a and b = :b",//
                "/*-- this is comment a = :a and b = :b");

        //arg1
        assertArgNamedCnt(Collections.singletonList("a"),//
                "a = :a -- this is comment\n",           //
                "a = ? -- this is comment\n");
        assertArgNamedCnt(Collections.singletonList("a"),//
                "a = :a -- this is comment",             //
                "a = ? -- this is comment");
        assertArgNamedCnt(Collections.singletonList("a"),//
                "a = :a -- this is comment and b = :b",  //
                "a = ? -- this is comment and b = :b");
        assertArgNamedCnt(Collections.singletonList("b"),//
                "-- this is comment \n b = :b",          //
                "-- this is comment \n b = ?");
        assertArgNamedCnt(Collections.singletonList("aa"),//
                "'aa' = :aa",                             //
                "'aa' = ?");
        assertArgNamedCnt(Collections.singletonList("bb"),//
                "\"bb\" = :bb",                           //
                "\"bb\" = ?");

        //arg2
        assertArgNamedCnt(Arrays.asList("a", "b"),       //
                "a = :a -- this is comment\n and b = :b",//
                "a = ? -- this is comment\n and b = ?");
        assertArgNamedCnt(Arrays.asList("a", "b"),       //
                "a = :a /* this is comment*/ and b = :b",//
                "a = ? /* this is comment*/ and b = ?");

        //expr
        String sql = "a = :id.ccc['aaa'][0]";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        Map<String, Object> ctx = CollectionUtils.asMap("id", CollectionUtils.asMap("ccc", CollectionUtils.asMap("aaa", Arrays.asList("abc"))));
        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new TestQueryContext());

        assert segment.getNamedList().get(0).getExpr().equals("id.ccc['aaa'][0]");
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals("a = ?");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getName().equals("id.ccc['aaa'][0]");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void named_Test_2() throws SQLException {
        // arg0
        assertArgNamedCnt(Collections.emptyList(),  //
                "-- this is comment a = &a",        //
                "-- this is comment a = &a");
        assertArgNamedCnt(Collections.emptyList(),      //
                "-- this is comment a = &a and b = &a", //
                "-- this is comment a = &a and b = &a");
        assertArgNamedCnt(Collections.emptyList(),         //
                "/*-- this is comment a = &a and b = &b*/",//
                "/*-- this is comment a = &a and b = &b*/");
        assertArgNamedCnt(Collections.emptyList(),     // bad sql
                "/* this is comment a = &a and b = &b",//
                "/* this is comment a = &a and b = &b");
        assertArgNamedCnt(Collections.emptyList(),       // bad sql
                "/*-- this is comment a = &a and b = &b",//
                "/*-- this is comment a = &a and b = &b");

        //arg1
        assertArgNamedCnt(Collections.singletonList("a"),//
                "a = &a -- this is comment\n",           //
                "a = ? -- this is comment\n");
        assertArgNamedCnt(Collections.singletonList("a"),//
                "a = &a -- this is comment",             //
                "a = ? -- this is comment");
        assertArgNamedCnt(Collections.singletonList("a"),//
                "a = &a -- this is comment and b = &b",  //
                "a = ? -- this is comment and b = &b");
        assertArgNamedCnt(Collections.singletonList("b"),//
                "-- this is comment \n b = &b",          //
                "-- this is comment \n b = ?");
        assertArgNamedCnt(Collections.singletonList("aa"),//
                "'aa' = &aa",                             //
                "'aa' = ?");
        assertArgNamedCnt(Collections.singletonList("bb"),//
                "\"bb\" = &bb",                           //
                "\"bb\" = ?");

        //arg2
        assertArgNamedCnt(Arrays.asList("a", "b"),       //
                "a = &a -- this is comment\n and b = &b",//
                "a = ? -- this is comment\n and b = ?");
        assertArgNamedCnt(Arrays.asList("a", "b"),       //
                "a = &a /* this is comment*/ and b = &b",//
                "a = ? /* this is comment*/ and b = ?");

        //expr
        String sql = "a = &id.ccc['aaa'][0]";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        Map<String, Object> ctx = CollectionUtils.asMap("id", CollectionUtils.asMap("ccc", CollectionUtils.asMap("aaa", Arrays.asList("abc"))));
        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals("a = ?");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getName().equals("id.ccc['aaa'][0]");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void named_Test_3() throws SQLException {
        // arg0
        assertRule(Collections.emptyList(),//
                "-- this is comment a = #{name} and b = #{name}",//
                "-- this is comment a = #{name} and b = #{name}");

        // arg1
        assertRule(Arrays.asList("name"),//
                "abc = #{name}",         //
                "abc = ?");
        assertRule(Arrays.asList("eventType"),//
                "abc = #{eventType,javaType=java.lang.Integer}",//
                "abc = ?");

        //arg2
        assertRule(Arrays.asList("aa", "bb"),  //
                "abc = #{aa} and cba = #{bb}",//
                "abc = ? and cba = ?");

        //expr
        String sql = "abc = #{eventType,mode=INOUT,jdbcType=INT,javaType=java.lang.Integer,typeHandler=net.hasor.dbvisitor.types.handler.number.BigDecimalTypeHandler}";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        Map<String, Object> ctx = CollectionUtils.asMap("eventType", "12345");
        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals("abc = ?");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getName().equals("eventType");
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getSqlMode() == SqlMode.InOut;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getJdbcType().equals(JDBCType.INTEGER.getVendorTypeNumber());
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getJavaType() == Integer.class;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getTypeHandler().getClass() == BigDecimalTypeHandler.class;
    }

    @Test
    public void named_Test_4() throws SQLException {
        assertArgNamedCnt(Arrays.asList(),              //
                "test.user_info.find({name: 'mali'})",  //
                "test.user_info.find({name: 'mali'})");

        assertArgNamedCnt(Arrays.asList("name", "nameValue"),//
                "test.user_info.find({:name: :nameValue})",  //
                "test.user_info.find({?: ?})");
    }

    private static void assertRule(List<String> ruleExpr, String sql, String testSql) throws SQLException {
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        SqlBuilder sqlBuilder = segment.buildQuery(Collections.emptyMap(), new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals(testSql);
        assert sqlBuilder.getArgs().length == ruleExpr.size();

        for (int i = 0; i < sqlBuilder.getArgs().length; i++) {
            assert ((SqlArg) sqlBuilder.getArgs()[i]).getName().equals(ruleExpr.get(i));
        }

        segment.clone();
    }

    @Test
    public void rule_Test_1() throws SQLException {
        // arg0
        assertRule(Collections.emptyList(),//
                "abc = @{if}",            //
                "abc = ");
        assertRule(Collections.emptyList(),//
                "abc = @{text,'name'}",   //
                "abc = 'name'");
        assertRule(Collections.emptyList(), //
                "abc = @{if,false,'name'}",//
                "abc = ");
        assertRule(Collections.emptyList(),//
                "abc = @{if,true,'name'}",//
                "abc = 'name'");
        assertRule(Collections.emptyList(),//
                "abc = @{if,true,-- :name}",//
                "abc = -- :name");
        assertRule(Collections.emptyList(),//
                "-- this is comment a = @{if,true,:a} and b = @{if,true,:a}",//
                "-- this is comment a = @{if,true,:a} and b = @{if,true,:a}");

        // arg1
        assertRule(Arrays.asList("aa"),//
                "abc = @{if,true,:aa}",//
                "abc = ?");

        //arg2
        assertRule(Arrays.asList("aa", "bb"),  //
                "abc = @{if,true,:aa and :bb}",//
                "abc = ? and ?");
    }

    @Test
    public void rule_Test_2() throws SQLException {
        //expr
        String sql = "name = #{eventType} and age = :age and type = ?";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        Map<String, Object> ctx = CollectionUtils.asMap("eventType", "a", "age", "b", "arg2", "c");
        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals("name = ? and age = ? and type = ?");
        assert sqlBuilder.getArgs().length == 3;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("a");
        assert sqlBuilder.getArgs()[1].equals("b");
        assert sqlBuilder.getArgs()[2].equals("c");
    }

    @Test
    public void inject_Test_1() throws SQLException {
        //expr
        String sql = "abc = ${eventType}";
        PlanDynamicSql segment = DynamicParsed.getParsedSql(sql);
        Map<String, Object> ctx = CollectionUtils.asMap("eventType", "12345");
        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new TestQueryContext());
        assert segment.getOriSqlString().equals(sql);
        assert sqlBuilder.getSqlString().equals("abc = 12345");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void tokenTest_01() {
        PlanDynamicSql arg1 = DynamicParsed.getParsedSql("select from user where id = ?");
        PlanDynamicSql arg2 = DynamicParsed.getParsedSql("select from user where id = :id");
        PlanDynamicSql arg3 = DynamicParsed.getParsedSql("select from user where id = :id.ccc['aaa'][0].name()");
        PlanDynamicSql arg4 = DynamicParsed.getParsedSql("select from user where id = &id");
        PlanDynamicSql arg5 = DynamicParsed.getParsedSql("select from user where id = &id.ccc['aaa'][0].name()");
        PlanDynamicSql arg6 = DynamicParsed.getParsedSql("select from user where id = @{abc}");
        PlanDynamicSql arg7 = DynamicParsed.getParsedSql("select from user where id = #{abc}");
        PlanDynamicSql arg8 = DynamicParsed.getParsedSql("select from user where id = ${abc}");
        PlanDynamicSql arg9 = DynamicParsed.getParsedSql("select from user where id = @{abc,true, :name}");

        assert SqlModifier.POSITION == arg1.getSqlModifier();
        assert SqlModifier.NAMED == arg2.getSqlModifier();
        assert SqlModifier.NAMED == arg3.getSqlModifier();
        assert SqlModifier.NAMED == arg4.getSqlModifier();
        assert SqlModifier.NAMED == arg5.getSqlModifier();
        assert SqlModifier.RULE == arg6.getSqlModifier();
        assert SqlModifier.NAMED == arg7.getSqlModifier();
        assert SqlModifier.INJECTION == arg8.getSqlModifier();
        assert SqlModifier.RULE == arg9.getSqlModifier();
    }

    @Test
    public void tokenTest_02() {
        PlanDynamicSql arg1 = DynamicParsed.getParsedSql("select @{text,*} from user where id = ? and name :name order by ${order}");
        int sqlModifier = arg1.getSqlModifier();

        assert SqlModifier.POSITION != sqlModifier;
        assert SqlModifier.NAMED != sqlModifier;
        assert SqlModifier.RULE != sqlModifier;
        assert SqlModifier.INJECTION != sqlModifier;
        assert SqlModifier.hasPosition(sqlModifier);
        assert SqlModifier.hasNamed(sqlModifier);
        assert SqlModifier.hasRule(sqlModifier);
        assert SqlModifier.hasInjection(sqlModifier);
    }
}
