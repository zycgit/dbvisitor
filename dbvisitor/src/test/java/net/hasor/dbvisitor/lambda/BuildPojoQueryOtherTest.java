/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.lambda;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.lambda.dto.UserInfo;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

/***
 * @version 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuildPojoQueryOtherTest {
    private LambdaTemplate newLambda() throws SQLException {
        Options opt = Options.of();
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new LambdaTemplate((DataSource) null, registry, context);
    }

    @Test
    public void queryBuilder_apply_1() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a")//
                .eq(UserInfo::getLoginName, "b")//
                .apply("seq > ?", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? AND loginName = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");
        assert boundSql1.getArgs()[2].equals(123);

    }

    @Test
    public void queryBuilder_apply_1_2map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .eq("loginName", "a")//
                .eq("loginName", "b")//
                .apply("seq > ?", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? AND loginName = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");
        assert boundSql1.getArgs()[2].equals(123);

    }

    @Test
    public void queryBuilder_eq_sample_1() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .eqBySample(new UserInfo()).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo");
        assert boundSql1.getArgs().length == 0;
    }

    @Test
    public void queryBuilder_eq_sample_1_2map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .eqBySample(new HashMap<>()).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo");
        assert boundSql1.getArgs().length == 0;
    }

    @Test
    public void queryBuilder_eq_sample_2() throws SQLException {
        UserInfo dto = new UserInfo();
        dto.setLoginName("abc");
        dto.setSeq(1);

        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .eqBySample(dto).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo WHERE ( loginName = ? AND seq = ? )");
        assert boundSql1.getArgs()[0].equals("abc");
        assert boundSql1.getArgs()[1].equals(1);
    }

    @Test
    public void queryBuilder_eq_sample_2_2map() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", "abc");
        map.put("seq", 1);

        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .eqBySample(map).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo WHERE ( loginName = ? AND seq = ? )");
        assert boundSql1.getArgs()[0].equals("abc");
        assert boundSql1.getArgs()[1].equals(1);
    }

    @Test
    public void queryBuilder_group_by_1() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .select(UserInfo::getSeq)//
                .eq(UserInfo::getLoginName, "a").eq(UserInfo::getLoginName, "b")//
                .groupBy(UserInfo::getSeq).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT seq FROM UserInfo WHERE loginName = ? AND loginName = ? GROUP BY seq");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");
    }

    @Test
    public void queryBuilder_group_by_1_map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .select("seq")//
                .eq("loginName", "a").eq("loginName", "b")//
                .groupBy("seq").getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT seq FROM UserInfo WHERE loginName = ? AND loginName = ? GROUP BY seq");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");
    }

    @Test
    public void queryBuilder_group_by_2() {
        try {
            newLambda().query(UserInfo.class)//
                    .eq(UserInfo::getLoginName, "a")//
                    .eq(UserInfo::getLoginName, "b")//
                    .apply("limit 1")//
                    .groupBy(UserInfo::getSeq)//
                    .eq(UserInfo::getLoginName, "b"); // after groupBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("must before (group by/order by) invoke it.");
        }

        try {
            newLambda().query(UserInfo.class)//
                    .eq(UserInfo::getLoginName, "a")//
                    .eq(UserInfo::getLoginName, "b")//
                    .apply("limit 1")//
                    .orderBy(UserInfo::getSeq)//
                    .eq(UserInfo::getLoginName, "b"); // << --- after orderBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("must before (group by/order by) invoke it.");
        }

        try {
            newLambda().query(UserInfo.class)//
                    .eq(UserInfo::getLoginName, "a")//
                    .eq(UserInfo::getLoginName, "b")//
                    .apply("limit 1")//
                    .groupBy(UserInfo::getSeq)  //
                    .orderBy(UserInfo::getSeq)  //
                    .groupBy(UserInfo::getSeq); // << --- after orderBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("must before order by invoke it.");
        }
    }

    @Test
    public void queryBuilder_group_by_2_map() {
        try {
            newLambda().query(UserInfo.class).asMap()//
                    .eq("loginName", "a")//
                    .eq("loginName", "b")//
                    .apply("limit 1")//
                    .groupBy("seq")//
                    .eq("loginName", "b"); // after groupBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("must before (group by/order by) invoke it.");
        }

        try {
            newLambda().query(UserInfo.class).asMap()//
                    .eq("loginName", "a")//
                    .eq("loginName", "b")//
                    .apply("limit 1")//
                    .orderBy("seq")//
                    .eq("loginName", "b"); // << --- after orderBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("must before (group by/order by) invoke it.");
        }

        try {
            newLambda().query(UserInfo.class).asMap()//
                    .eq("loginName", "a")//
                    .eq("loginName", "b")//
                    .apply("limit 1")//
                    .groupBy("seq")  //
                    .orderBy("seq")  //
                    .groupBy("seq"); // << --- after orderBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("must before order by invoke it.");
        }
    }

    @Test
    public void queryBuilder_order_by_1() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a")//
                .asc(UserInfo::getLoginName).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY loginName ASC");
        assert boundSql1.getArgs()[0].equals("a");

        BoundSql boundSql2 = newLambda().query(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a")//
                .asc(UserInfo::getLoginName).asc(UserInfo::getSeq)//
                .getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY loginName ASC, seq ASC");
        assert boundSql2.getArgs()[0].equals("a");

        BoundSql boundSql3 = newLambda().query(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a")//
                .desc(UserInfo::getLoginName)//
                .getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY loginName DESC");
        assert boundSql3.getArgs()[0].equals("a");

        BoundSql boundSql4 = newLambda().query(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a")//
                .asc(UserInfo::getSeq).desc(UserInfo::getLoginName)//
                .getBoundSql();
        assert boundSql4.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY seq ASC, loginName DESC");
        assert boundSql4.getArgs()[0].equals("a");

        BoundSql boundSql5 = newLambda().query(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a")//
                .orderBy(UserInfo::getSeq)//
                .getBoundSql();
        assert boundSql5.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY seq");
        assert boundSql5.getArgs()[0].equals("a");
    }

    @Test
    public void queryBuilder_order_by_1_2map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .eq("loginName", "a")//
                .asc("loginName").getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY loginName ASC");
        assert boundSql1.getArgs()[0].equals("a");

        BoundSql boundSql2 = newLambda().query(UserInfo.class).asMap()//
                .eq("loginName", "a")//
                .asc("loginName").asc("seq")//
                .getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY loginName ASC, seq ASC");
        assert boundSql2.getArgs()[0].equals("a");

        BoundSql boundSql3 = newLambda().query(UserInfo.class).asMap()//
                .eq("loginName", "a")//
                .desc("loginName")//
                .getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY loginName DESC");
        assert boundSql3.getArgs()[0].equals("a");

        BoundSql boundSql4 = newLambda().query(UserInfo.class).asMap()//
                .eq("loginName", "a")//
                .asc("seq").desc("loginName")//
                .getBoundSql();
        assert boundSql4.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY seq ASC, loginName DESC");
        assert boundSql4.getArgs()[0].equals("a");

        BoundSql boundSql5 = newLambda().query(UserInfo.class).asMap()//
                .eq("loginName", "a")//
                .orderBy("seq")//
                .getBoundSql();
        assert boundSql5.getSqlString().equals("SELECT * FROM UserInfo WHERE loginName = ? ORDER BY seq");
        assert boundSql5.getArgs()[0].equals("a");
    }

    @Test
    public void queryBuilder_select_1() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).applySelect("a, b, c, d")//
                .eq(UserInfo::getSeq, 1)//
                .or()//
                .rangeBetween(UserInfo::getLoginName, 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT a, b, c, d FROM UserInfo WHERE seq = ? OR loginName BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_1_2map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .applySelect("a, b, c, d")//
                .eq("seq", 1)//
                .or()//
                .rangeBetween("loginName", 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT a, b, c, d FROM UserInfo WHERE seq = ? OR loginName BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_2() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .selectAdd(UserInfo::getLoginName).selectAdd(UserInfo::getSeq)//
                .eq(UserInfo::getSeq, 1)//
                .or()//
                .rangeBetween(UserInfo::getLoginName, 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT loginName, seq FROM UserInfo WHERE seq = ? OR loginName BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_2_2map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .selectAdd("loginName").selectAdd("seq")//
                .eq("seq", 1)//
                .or()//
                .rangeBetween("loginName", 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT loginName, seq FROM UserInfo WHERE seq = ? OR loginName BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_3() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class)//
                .select(UserInfo::getLoginName)//
                .eq(UserInfo::getSeq, 1)//
                .or()//
                .rangeBetween(UserInfo::getLoginName, 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT loginName FROM UserInfo WHERE seq = ? OR loginName BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_3_2map() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo.class).asMap()//
                .select("loginName")//
                .eq("seq", 1)//
                .or()//
                .rangeBetween("loginName", 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT loginName FROM UserInfo WHERE seq = ? OR loginName BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void bad_1() {
        try {
            newLambda().query(UserInfo.class)//
                    .eq(UserInfo::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("Connection unavailable, JdbcTemplate is required.");
        }
    }
}
