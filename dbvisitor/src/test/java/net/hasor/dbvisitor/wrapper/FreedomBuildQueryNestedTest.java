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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.template.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

import javax.sql.DataSource;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class FreedomBuildQueryNestedTest {
    private WrapperAdapter newLambda() {
        MappingOptions opt = MappingOptions.buildNew();
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new WrapperAdapter((DataSource) null, registry, context);
    }

    @Test
    public void queryBuilder_nested_or_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a").or(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE loginName = ? OR ( createTime >= ? AND createTime <= ? )");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);

        BoundSql boundSql2 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a").or().nested(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE loginName = ? OR ( createTime >= ? AND createTime <= ? )");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
    }

    @Test
    public void queryBuilder_nested_or_2() {
        BoundSql boundSql1 = new WrapperAdapter().freedomQuery("user_info")//
                .nested(qc -> {
                    qc.eq("loginName", "user-1").eq("seq", 1);
                }).or(qc -> {
                    qc.eq("loginName", "user-2").eq("seq", 2);
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( loginName = ? AND seq = ? ) OR ( loginName = ? AND seq = ? )");
        assert boundSql1.getArgs()[0].equals("user-1");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals("user-2");
        assert boundSql1.getArgs()[3].equals(2);
    }

    @Test
    public void queryBuilder_nested_and_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a").and(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE loginName = ? AND ( createTime >= ? AND createTime <= ? ) AND loginName = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a").and().nested(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE loginName = ? AND ( createTime >= ? AND createTime <= ? ) AND loginName = ?");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
        assert boundSql2.getArgs()[3].equals(123);
    }

    @Test
    public void queryBuilder_nested_and_2() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .nested(qc -> {
                    qc.eq("seq", 1).or().eq("seq", 2);
                }).and(qc -> {
                    qc.eq("loginName", "user-1").or().eq("loginName", "user-2");
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( seq = ? OR seq = ? ) AND ( loginName = ? OR loginName = ? )");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals("user-1");
        assert boundSql1.getArgs()[3].equals("user-2");
    }

    @Test
    public void queryBuilder_nested_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .eq("loginName", "a").nested(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE loginName = ? AND ( createTime >= ? AND createTime <= ? ) AND loginName = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = newLambda().freedomQuery("user_info")//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge("createTime", 1); // >= ?
                        nq1.le("createTime", 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq("seq", 1);
                    });
                }).eq("loginName", 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE ( ( createTime >= ? AND createTime <= ? ) AND ( seq = ? ) ) AND loginName = ?");
        assert boundSql2.getArgs()[0].equals(1);
        assert boundSql2.getArgs()[1].equals(2);
        assert boundSql2.getArgs()[2].equals(1);
        assert boundSql2.getArgs()[3].equals(123);

        BoundSql boundSql3 = newLambda().freedomQuery("user_info")//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge("createTime", 1); // >= ?
                        nq1.le("createTime", 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq("seq", 1);
                    });
                }).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE ( ( createTime >= ? AND createTime <= ? ) AND ( seq = ? ) )");
        assert boundSql3.getArgs()[0].equals(1);
        assert boundSql3.getArgs()[1].equals(2);
        assert boundSql3.getArgs()[2].equals(1);
    }

    @Test
    public void queryBuilder_nested_not_1() {
        BoundSql boundSql1 = newLambda().freedomQuery("user_info")//
                .not(qc -> {
                    qc.eq("seq", 1).or().eq("loginName", "a");
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE NOT ( seq = ? OR loginName = ? )");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals("a");
    }
}
