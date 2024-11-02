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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.dto.UserInfo;
import org.junit.Test;

import javax.sql.DataSource;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuildToCamelPojoQueryNestedTest {
    private WrapperAdapter newLambda() {
        MappingOptions opt = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);
        MappingRegistry registry = new MappingRegistry(null, new TypeHandlerRegistry(), opt);
        RegistryManager manager = new RegistryManager(registry, new RuleRegistry(), new MacroRegistry());

        return new WrapperAdapter((DataSource) null, manager);
    }

    @Test
    public void queryBuilder_nested_or_1() {
        BoundSql boundSql1 = newLambda().queryByEntity(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a").or(nestedQuery -> {
                    nestedQuery.ge(UserInfo::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo::getCreateTime, 2); // <= ?
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( create_time >= ? AND create_time <= ? )");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);

        BoundSql boundSql2 = newLambda().queryByEntity(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a").or().nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo::getCreateTime, 2); // <= ?
                }).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( create_time >= ? AND create_time <= ? )");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
    }

    @Test
    public void queryBuilder_nested_or_1_2map() {
        BoundSql boundSql1 = newLambda().queryByEntity(UserInfo.class).asMap()//
                .eq("loginName", "a").or(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( create_time >= ? AND create_time <= ? )");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);

        BoundSql boundSql2 = newLambda().queryByEntity(UserInfo.class).asMap()//
                .eq("loginName", "a").or().nested(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( create_time >= ? AND create_time <= ? )");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
    }

    @Test
    public void queryBuilder_nested_and_1() {
        BoundSql boundSql1 = newLambda().queryByEntity(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a").and(nestedQuery -> {
                    nestedQuery.ge(UserInfo::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo::getCreateTime, 2); // <= ?
                }).eq(UserInfo::getLoginName, 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( create_time >= ? AND create_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = newLambda().queryByEntity(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a").and().nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo::getCreateTime, 2); // <= ?
                }).eq(UserInfo::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( create_time >= ? AND create_time <= ? ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
        assert boundSql2.getArgs()[3].equals(123);
    }

    @Test
    public void queryBuilder_nested_and_1_2map() {
        BoundSql boundSql1 = newLambda().queryByEntity(UserInfo.class).asMap()//
                .eq("loginName", "a").and(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( create_time >= ? AND create_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = newLambda().queryByEntity(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a").and().nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo::getCreateTime, 2); // <= ?
                }).eq(UserInfo::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( create_time >= ? AND create_time <= ? ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
        assert boundSql2.getArgs()[3].equals(123);
    }

    @Test
    public void queryBuilder_nested_1() {
        BoundSql boundSql1 = newLambda().queryByEntity(UserInfo.class)//
                .eq(UserInfo::getLoginName, "a").nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo::getCreateTime, 2); // <= ?
                }).eq(UserInfo::getLoginName, 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( create_time >= ? AND create_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = newLambda().queryByEntity(UserInfo.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(UserInfo::getCreateTime, 1); // >= ?
                        nq1.le(UserInfo::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(UserInfo::getSeq, 1);
                    });
                }).eq(UserInfo::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE ( ( create_time >= ? AND create_time <= ? ) AND ( seq = ? ) ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals(1);
        assert boundSql2.getArgs()[1].equals(2);
        assert boundSql2.getArgs()[2].equals(1);
        assert boundSql2.getArgs()[3].equals(123);

        BoundSql boundSql3 = newLambda().queryByEntity(UserInfo.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(UserInfo::getCreateTime, 1); // >= ?
                        nq1.le(UserInfo::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(UserInfo::getSeq, 1);
                    });
                }).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE ( ( create_time >= ? AND create_time <= ? ) AND ( seq = ? ) )");
        assert boundSql3.getArgs()[0].equals(1);
        assert boundSql3.getArgs()[1].equals(2);
        assert boundSql3.getArgs()[2].equals(1);
    }

    @Test
    public void queryBuilder_nested_1_2map() {
        BoundSql boundSql1 = newLambda().queryByEntity(UserInfo.class).asMap()//
                .eq("loginName", "a").nested(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( create_time >= ? AND create_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = newLambda().queryByEntity(UserInfo.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(UserInfo::getCreateTime, 1); // >= ?
                        nq1.le(UserInfo::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(UserInfo::getSeq, 1);
                    });
                }).eq(UserInfo::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE ( ( create_time >= ? AND create_time <= ? ) AND ( seq = ? ) ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals(1);
        assert boundSql2.getArgs()[1].equals(2);
        assert boundSql2.getArgs()[2].equals(1);
        assert boundSql2.getArgs()[3].equals(123);

        BoundSql boundSql3 = newLambda().queryByEntity(UserInfo.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(UserInfo::getCreateTime, 1); // >= ?
                        nq1.le(UserInfo::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(UserInfo::getSeq, 1);
                    });
                }).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE ( ( create_time >= ? AND create_time <= ? ) AND ( seq = ? ) )");
        assert boundSql3.getArgs()[0].equals(1);
        assert boundSql3.getArgs()[1].equals(2);
        assert boundSql3.getArgs()[2].equals(1);
    }
}
