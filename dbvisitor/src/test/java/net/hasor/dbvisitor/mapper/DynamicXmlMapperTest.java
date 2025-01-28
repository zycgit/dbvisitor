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
package net.hasor.dbvisitor.mapper;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.mapper.def.InsertConfig;
import net.hasor.dbvisitor.mapper.def.SelectKeyConfig;
import net.hasor.dbvisitor.mapper.dto.DynamicXmlMapper;
import net.hasor.dbvisitor.mapper.dto.UserInfo;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class DynamicXmlMapperTest {
    @Test
    public void bind_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("sellerId", "123");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "bind_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().trim().equals("select * from console_job where aac = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("123abc");
    }

    @Test
    public void choose_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("title", "123", "content", "aaa");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "choose_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().trim().equals("select * from t_blog\n        where  title = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void choose_2() throws Exception {
        Map<String, Object> ctx = Collections.emptyMap();
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "choose_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().trim().equals("select * from t_blog\n        where  owner = \"owner1\"");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void foreach_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("resTypes", Arrays.asList("a", "b", "c", "d", "e"));
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "foreach_03");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("SELECT\n            \n        *\n    \n        FROM\n"//
                + "            alert_detail\n        WHERE\n            alert_detail.event_type IN\n" //
                + "            (\n                ?\n            ,\n                ?\n            ,\n                ?\n            ,\n                ?\n            ,\n                ?\n            )");
        assert sqlBuilder.getArgs().length == 5;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("a");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("b");
        assert ((SqlArg) sqlBuilder.getArgs()[2]).getValue().equals("c");
        assert ((SqlArg) sqlBuilder.getArgs()[3]).getValue().equals("d");
        assert ((SqlArg) sqlBuilder.getArgs()[4]).getValue().equals("e");
    }

    @Test
    public void if_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("ownerID", "123", "ownerType", "SYSTEM");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "if_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("select\n        \n        *\n    \n        from\n" //
                + "            PROJECT_INFO\n        where 1=1\n            and status = 2\n            \n"//
                + "                and owner_id = ?\n                and owner_type = ?\n            \n        order by\n            name asc");
        assert sqlBuilder.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("123");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("SYSTEM");
    }

    @Test
    public void if_2() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("ownerID", "123", "ownerType", null);
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "if_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("select\n        \n        *\n    \n        from\n            PROJECT_INFO\n        where 1=1\n            and status = 2\n            \n        order by\n            name asc");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void include_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("eventType", "123");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "include_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("SELECT\n            \n        *\n    \n        FROM\n            alert_detail\n        WHERE\n            event_type = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void selectKey_1() throws Exception {
        UserInfo user = new UserInfo();
        user.setName("name");
        user.setLoginName("loginName");
        Map<String, Object> ctx = CollectionUtils.asMap("user", user);
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "selectKey_01");
        SqlBuilder sqlBuilder1 = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder1.getSqlString().trim().equals("insert into test_user (\n            \n        name,login_name\n    \n        ) values (\n            ? , ?\n        );");
        assert sqlBuilder1.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder1.getArgs()[1]).getValue().equals("loginName");

        SelectKeyConfig keyConfig = ((InsertConfig) def.getConfig()).getSelectKey();
        SqlBuilder sqlBuilder2 = keyConfig.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder2.getSqlString().trim().equals("SELECT LAST_INSERT_ID()");
        assert sqlBuilder2.getArgs().length == 0;
    }

    @Test
    public void set_1() throws Exception {
        UserInfo user = new UserInfo();
        user.setName("name");
        user.setLoginName("loginName");
        user.setUserUuid("abc");
        Map<String, Object> ctx = CollectionUtils.asMap("user", user);
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "set_04");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("UPDATE\n            alert_users\n        set name = ?,\n            \n            \n                loginName = ? \n        WHERE uid = ?");
        assert sqlBuilder.getArgs().length == 3;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("loginName");
        assert ((SqlArg) sqlBuilder.getArgs()[2]).getValue().equals("abc");
    }

    @Test
    public void set_2() throws Exception {
        UserInfo user = new UserInfo();
        user.setUserUuid("abc");
        Map<String, Object> ctx = CollectionUtils.asMap("user", user);
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "set_04");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("UPDATE\n            alert_users\n        \n        WHERE uid = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("abc");
    }

    @Test
    public void where_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "name", "loginName", "loginName");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "where_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("SELECT * FROM BLOG\n        where  name = ?\n            \n            \n                and login_name like ?");
        assert sqlBuilder.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("name");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("loginName");
    }

    @Test
    public void where_2() throws Exception {
        Map<String, Object> ctx = Collections.emptyMap();
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(DynamicXmlMapper.class);

        StatementDef def = registry.findStatement(DynamicXmlMapper.class, "where_01");
        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager(registry.typeRegistry, new RuleRegistry(), registry.macroRegistry));
        assert sqlBuilder.getSqlString().trim().equals("SELECT * FROM BLOG");
        assert sqlBuilder.getArgs().length == 0;
    }
}
