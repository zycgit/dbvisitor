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
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.template.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-3-22
 */
public class BuildEntUpdateTest {
    private WrapperAdapter newLambda() throws SQLException {
        MappingOptions opt = MappingOptions.buildNew().defaultDialect(new MySqlDialect());
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
            EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = newLambda().updateByEntity(AnnoUserInfoDTO.class);
            assert lambdaUpdate.getBoundSql() == null;
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("there nothing to update.");
        }

        try {
            new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class).updateToSample(null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("newValue is null.");
        }

        try {
            EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = new WrapperAdapter()//
                    .updateByEntity(AnnoUserInfoDTO.class)//
                    .updateRow(new AnnoUserInfoDTO());
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }
    }

    @Test
    public void updateBuilder_bad_1_2map() {
        try {
            MapUpdateWrapper lambdaUpdate = newLambda().updateByEntity(AnnoUserInfoDTO.class).asMap();
            assert lambdaUpdate.getBoundSql() == null;
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("there nothing to update.");
        }

        try {
            new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class).updateToSample(null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("newValue is null.");
        }

        try {
            MapUpdateWrapper lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .updateRow(new HashMap<>());
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous UPDATE operation,");
        }
    }

    @Test
    public void updateBuilder_bad_2() throws SQLException {
        EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = newLambda().updateByEntity(AnnoUserInfoDTO.class);
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
        MapUpdateWrapper lambdaUpdate = newLambda().updateByEntity(AnnoUserInfoDTO.class).asMap();
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
        AnnoUserInfoDTO data = new AnnoUserInfoDTO();
        data.setLoginName("acc");
        data.setPassword("pwd");
        EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class);
        lambdaUpdate.and(qb -> qb.eq(AnnoUserInfoDTO::getSeq, 123)).updateToSample(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , login_password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_1_1_2map() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", "acc");
        map.put("password", "pwd");
        map.put("abc", "pwd");

        MapUpdateWrapper lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class).asMap();
        lambdaUpdate.and(qb -> qb.eq("seq", 123)).updateToSample(map);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , login_password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_1_2() throws SQLException {
        AnnoUserInfoDTO data = new AnnoUserInfoDTO();
        data.setLoginName("acc");
        data.setPassword("pwd");
        EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class);
        lambdaUpdate.and(qb -> qb.eq(AnnoUserInfoDTO::getSeq, 123)).updateToSample(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , login_password = ? WHERE ( seq = ? )");
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

        MapUpdateWrapper lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class).asMap();
        lambdaUpdate.and(qb -> qb.eq("seq", 123)).updateToSample(map);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , login_password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_2_1() throws SQLException {
        AnnoUserInfoDTO data = new AnnoUserInfoDTO();
        data.setLoginName("acc");
        data.setPassword("pwd");

        EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class);
        lambdaUpdate.eq(AnnoUserInfoDTO::getLoginName, "admin").and().eq(AnnoUserInfoDTO::getPassword, "pass").updateRow(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET user_name = ? , login_name = ? , login_password = ? , email = ? , seq = ? , register_time = ? WHERE login_name = ? AND login_password = ?");
    }

    @Test
    public void updateBuilder_2_1_map() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", "acc");
        map.put("password", "pwd");
        map.put("abc", "pwd");

        MapUpdateWrapper lambdaUpdate = new WrapperAdapter().updateByEntity(AnnoUserInfoDTO.class).asMap();
        lambdaUpdate.eq("loginName", "admin").and().eq("password", "pass").updateRow(map);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET user_name = ? , login_name = ? , login_password = ? , email = ? , seq = ? , register_time = ? WHERE login_name = ? AND login_password = ?");
    }

    @Test
    public void updateBuilder_by_sample_1() throws SQLException {
        WrapperAdapter lambdaTemplate = new WrapperAdapter();

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
        BoundSql boundSql1 = lambdaTemplate.updateByEntity(AnnoUserInfoDTO.class)//
                .eqBySampleMap(whereValue)//
                .updateToSampleMap(setValue)//
                .getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET user_name = ? WHERE ( user_name = ? )");
        assert ((SqlArg) boundSql1.getArgs()[0]).getValue().equals("321");
        assert ((SqlArg) boundSql1.getArgs()[1]).getValue().equals("123");
    }

    @Test
    public void updateBuilder_by_sample_1_2map() throws SQLException {
        WrapperAdapter lambdaTemplate = new WrapperAdapter();

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
        BoundSql boundSql1 = lambdaTemplate.updateByEntity(AnnoUserInfoDTO.class).asMap().eqBySampleMap(whereValue).updateToSampleMap(setValue).getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET user_name = ? WHERE ( user_name = ? )");
        assert ((SqlArg) boundSql1.getArgs()[0]).getValue().equals("321");
        assert ((SqlArg) boundSql1.getArgs()[1]).getValue().equals("123");
    }
}
