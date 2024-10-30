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
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class FreedomToCamelBuildQueryOtherTest {
    private LambdaTemplate newLambda() {
        MappingOptions opt = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);
        MappingRegistry registry = new MappingRegistry(null, new TypeHandlerRegistry(), opt);
        RegistryManager manager = new RegistryManager(registry, new RuleRegistry(), new MacroRegistry());

        return new LambdaTemplate((DataSource) null, manager);
    }

    @Test
    public void queryBuilder_apply_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .eq("loginName", "b")//
                .apply("limit ?", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND login_name = ? limit ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");
        assert boundSql1.getArgs()[2].equals(123);

    }

    @Test
    public void queryBuilder_eq_sample_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eqBySample(new HashMap<>()).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info");
        assert boundSql1.getArgs().length == 0;
    }

    @Test
    public void queryBuilder_eq_sample_2() {
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", "abc");
        map.put("seq", 1);

        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eqBySample(map).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( login_name = ? AND seq = ? )");
        assert boundSql1.getArgs()[0].equals("abc");
        assert boundSql1.getArgs()[1].equals(1);
    }

    @Test
    public void queryBuilder_group_by_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a").eq("loginName", "b")//
                .groupBy("seq").getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT seq FROM user_info WHERE login_name = ? AND login_name = ? GROUP BY seq");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");

        BoundSql boundSql2 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .eq("loginName", "b")//
                .apply("limit 1")//
                .groupBy("seq")//
                .apply("limit 1").getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT seq FROM user_info WHERE login_name = ? AND login_name = ? limit 1 GROUP BY seq limit 1");
    }

    @Test
    public void queryBuilder_group_by_2() {
        try {
            newLambda().freedomQuery("user_info")//
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
            newLambda().freedomQuery("user_info")//
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
            newLambda().freedomQuery("user_info")//
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
    public void queryBuilder_order_by_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .asc("loginName").getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY login_name ASC");
        assert boundSql1.getArgs()[0].equals("a");

        BoundSql boundSql2 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .asc("loginName", "seq")//
                .getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY login_name ASC , seq ASC");
        assert boundSql2.getArgs()[0].equals("a");

        BoundSql boundSql3 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .desc("loginName")//
                .getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY login_name DESC");
        assert boundSql3.getArgs()[0].equals("a");

        BoundSql boundSql4 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .asc("seq").desc("loginName")//
                .getBoundSql();
        assert boundSql4.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY seq ASC , login_name DESC");
        assert boundSql4.getArgs()[0].equals("a");

        BoundSql boundSql5 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a")//
                .orderBy("seq")//
                .getBoundSql();
        assert boundSql5.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY seq");
        assert boundSql5.getArgs()[0].equals("a");
    }

    @Test
    public void queryBuilder_select_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info").applySelect("a, b, c, d")//
                .eq("seq", 1)//
                .or()//
                .between("loginName", 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT a, b, c, d FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_2() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .select("loginName", "seq")//
                .eq("seq", 1)//
                .or()//
                .between("loginName", 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT login_name , seq FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder_select_3() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .select("loginName")//
                .eq("seq", 1)//
                .or()//
                .between("loginName", 2, 3)//
                .getBoundSql();

        assert boundSql1.getSqlString().equals("SELECT login_name FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void bad_1() {
        try {
            newLambda().freedomQuery("user_info")//
                    .eq("loginName", "muhammad").apply("limit 1")//
                    .queryForMap();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("Connection unavailable, JdbcTemplate is required.");
        }
    }
}
