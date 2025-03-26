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
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.dto.UserInfo;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-3-22
 */
public class BuildToCamelPojoUpdateTest {
    private WrapperAdapter newLambda() throws SQLException {
        Options opt = Options.of().mapUnderscoreToCamelCase(true);
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new WrapperAdapter((DataSource) null, registry, context);
    }

    @Test
    public void updateBuilder_bad_1() {
        try {
            EntityUpdateWrapper<UserInfo> lambdaUpdate = newLambda().update(UserInfo.class);
            assert lambdaUpdate.getBoundSql() == null;
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("there nothing to update.");
        }

        try {
            newLambda().update(UserInfo.class).updateToSample(null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("newValue is null.");
        }

        try {
            EntityUpdateWrapper<UserInfo> lambdaUpdate = newLambda().update(UserInfo.class).updateRow(new UserInfo());
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous UPDATE operation,");
        }
    }

    @Test
    public void updateBuilder_bad_1_2map() {
        try {
            MapUpdateWrapper lambdaUpdate = newLambda().update(UserInfo.class).asMap();
            assert lambdaUpdate.getBoundSql() == null;
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("there nothing to update.");
        }

        try {
            newLambda().update(UserInfo.class).updateToSample(null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("newValue is null.");
        }

        try {
            MapUpdateWrapper lambdaUpdate = newLambda().update(UserInfo.class).asMap()//
                    .updateRow(new HashMap<>());
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous UPDATE operation,");
        }
    }

    @Test
    public void updateBuilder_bad_2() throws SQLException {
        EntityUpdateWrapper<UserInfo> lambdaUpdate = newLambda().update(UserInfo.class);
        lambdaUpdate.allowEmptyWhere();

        try {
            lambdaUpdate.getBoundSql();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("there nothing to update.");
        }
    }

    @Test
    public void updateBuilder_bad_2_2map() throws SQLException {
        MapUpdateWrapper lambdaUpdate = newLambda().update(UserInfo.class).asMap();
        lambdaUpdate.allowEmptyWhere();

        try {
            lambdaUpdate.getBoundSql();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("there nothing to update.");
        }
    }

    @Test
    public void updateBuilder_1_1() throws SQLException {
        UserInfo data = new UserInfo();
        data.setLoginName("acc");
        data.setPassword("pwd");
        EntityUpdateWrapper<UserInfo> lambdaUpdate = newLambda().update(UserInfo.class);
        lambdaUpdate.and(qb -> qb.eq(UserInfo::getSeq, 123)).updateToSample(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_1_1_2map() throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("loginName", "acc");
        map.put("password", "pwd");
        map.put("abc", "pwd");

        MapUpdateWrapper lambdaUpdate = newLambda().update(UserInfo.class).asMap();
        lambdaUpdate.and(qb -> qb.eq("seq", 123)).updateToSample(map);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_1_2() throws SQLException {
        UserInfo data = new UserInfo();
        data.setLoginName("acc");
        data.setPassword("pwd");
        EntityUpdateWrapper<UserInfo> lambdaUpdate = newLambda().update(UserInfo.class);
        lambdaUpdate.and(qb -> qb.eq(UserInfo::getSeq, 123)).updateToSample(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_1_2_2map() throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("loginName", "acc");
        map.put("password", "pwd");
        map.put("abc", "pwd");

        MapUpdateWrapper lambdaUpdate = newLambda().update(UserInfo.class).asMap();
        lambdaUpdate.and(qb -> qb.eq("seq", 123)).updateToSample(map);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_2_1() throws SQLException {
        UserInfo data = new UserInfo();
        data.setLoginName("acc");
        data.setPassword("pwd");

        EntityUpdateWrapper<UserInfo> lambdaUpdate = newLambda().update(UserInfo.class);
        lambdaUpdate.eq(UserInfo::getLoginName, "admin").and().eq(UserInfo::getPassword, "pass").updateRow(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET uid = ? , name = ? , login_name = ? , password = ? , email = ? , seq = ? , create_time = ? WHERE login_name = ? AND password = ?");
    }

    @Test
    public void updateBuilder_2_1_map() throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("loginName", "acc");
        map.put("password", "pwd");
        map.put("abc", "pwd");

        MapUpdateWrapper lambdaUpdate = newLambda().update(UserInfo.class).asMap();
        lambdaUpdate.eq("loginName", "admin").and().eq("password", "pass").updateRow(map);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET uid = ? , name = ? , login_name = ? , password = ? , email = ? , seq = ? , create_time = ? WHERE login_name = ? AND password = ?");
    }

    @Test
    public void updateBuilder_by_sample_1() throws SQLException {
        WrapperAdapter lambdaTemplate = newLambda();

        Map<String, Object> whereValue = new HashMap<>();
        whereValue.put("id", 1);
        whereValue.put("user_name", "mali1");
        whereValue.put("name", "123");
        whereValue.put("abc", "abc");

        Map<String, Object> setValue = new HashMap<>();
        setValue.put("user_name", "mali2");
        setValue.put("name", "321");
        setValue.put("abc", "abc");
        setValue.put("create_time", new Date());

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.update(UserInfo.class).eqBySampleMap(whereValue).updateToSampleMap(setValue).getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET name = ? WHERE ( name = ? )");
        assert ((SqlArg) boundSql1.getArgs()[0]).getValue().equals("321");
        assert ((SqlArg) boundSql1.getArgs()[1]).getValue().equals("123");
    }

    @Test
    public void updateBuilder_by_sample_1_2map() throws SQLException {
        WrapperAdapter lambdaTemplate = newLambda();

        Map<String, Object> whereValue = new HashMap<>();
        whereValue.put("id", 1);
        whereValue.put("user_name", "mali1");
        whereValue.put("name", "123");
        whereValue.put("abc", "abc");

        Map<String, Object> setValue = new HashMap<>();
        setValue.put("user_name", "mali2");
        setValue.put("name", "321");
        setValue.put("abc", "abc");
        setValue.put("create_time", new Date());

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.update(UserInfo.class).asMap().eqBySampleMap(whereValue).updateToSampleMap(setValue).getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET name = ? WHERE ( name = ? )");
        assert ((SqlArg) boundSql1.getArgs()[0]).getValue().equals("321");
        assert ((SqlArg) boundSql1.getArgs()[1]).getValue().equals("123");
    }
}
