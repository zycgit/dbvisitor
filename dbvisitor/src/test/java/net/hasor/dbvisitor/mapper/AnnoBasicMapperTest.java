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
import net.hasor.dbvisitor.mapper.def.InsertConfig;
import net.hasor.dbvisitor.mapper.def.QueryType;
import net.hasor.dbvisitor.mapper.dto.AnnoBasicCrudMapper;
import net.hasor.dbvisitor.mapper.dto.UserInfo;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class AnnoBasicMapperTest {
    @Test
    public void annoQuery_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("abc", "this is abc");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoBasicCrudMapper.class);

        StatementDef def = registry.findStatement(AnnoBasicCrudMapper.class, "selectList");
        assert def != null;
        assert def.getConfigNamespace().equals(AnnoBasicCrudMapper.class.getName());
        assert def.getResultType() == UserInfo.class;
        assert def.getConfig().getType() == QueryType.Select;

        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("select * from console_job where aac = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("this is abc");
    }

    @Test
    public void annoQuery_2() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("title", "this is title", "content", "this is content");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoBasicCrudMapper.class);

        StatementDef def = registry.findStatement(AnnoBasicCrudMapper.class, "selectOne");
        assert def != null;
        assert def.getConfigNamespace().equals(AnnoBasicCrudMapper.class.getName());
        assert def.getResultType() == UserInfo.class;
        assert def.getConfig().getType() == QueryType.Select;

        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("select * from t_blog where title = ? and content = ?");
        assert sqlBuilder.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("this is title");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("this is content");
    }

    @Test
    public void annoInsert_1() throws Exception {
        UserInfo info = new UserInfo();
        info.setUserUuid("this is title");
        info.setName("this is name");
        info.setLoginName("this is login");
        Map<String, Object> ctx = CollectionUtils.asMap("info", info);
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoBasicCrudMapper.class);

        StatementDef def = registry.findStatement(AnnoBasicCrudMapper.class, "insertBean");
        assert def != null;
        assert def.getConfigNamespace().equals(AnnoBasicCrudMapper.class.getName());
        assert def.getResultType() == null;
        assert def.getConfig().getType() == QueryType.Insert;

        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("insert into console_job (uid,name,login) values (?, ?, ?)");
        assert sqlBuilder.getArgs().length == 3;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("this is title");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals("this is name");
        assert ((SqlArg) sqlBuilder.getArgs()[2]).getValue().equals("this is login");

        assert ((InsertConfig) def.getConfig()).getSelectKey() != null;
    }

    @Test
    public void annoUpdate_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("id", 11, "uuid", "this is uuid");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoBasicCrudMapper.class);

        StatementDef def = registry.findStatement(AnnoBasicCrudMapper.class, "updateBean");
        assert def != null;
        assert def.getConfigNamespace().equals(AnnoBasicCrudMapper.class.getName());
        assert def.getResultType() == null;
        assert def.getConfig().getType() == QueryType.Update;

        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("update console_job set uid = ? where id = ?");
        assert sqlBuilder.getArgs().length == 2;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("this is uuid");
        assert ((SqlArg) sqlBuilder.getArgs()[1]).getValue().equals(11);
    }

    @Test
    public void annoDelete_1() throws Exception {
        Map<String, Object> ctx = CollectionUtils.asMap("id", 11);
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoBasicCrudMapper.class);

        StatementDef def = registry.findStatement(AnnoBasicCrudMapper.class, "deleteBean");
        assert def != null;
        assert def.getConfigNamespace().equals(AnnoBasicCrudMapper.class.getName());
        assert def.getResultType() == null;
        assert def.getConfig().getType() == QueryType.Delete;

        SqlBuilder sqlBuilder = def.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("delete console_job where id = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals(11);
    }

    @Test
    public void annoExecute_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoBasicCrudMapper.class);

        StatementDef def = registry.findStatement(AnnoBasicCrudMapper.class, "createTable");
        assert def != null;
        assert def.getConfigNamespace().equals(AnnoBasicCrudMapper.class.getName());
        assert def.getResultType() == null;
        assert def.getConfig().getType() == QueryType.Execute;

        SqlBuilder sqlBuilder = def.buildQuery(null, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("create table console_job (uid int,name varchar(200),login varchar(200))");
        assert sqlBuilder.getArgs().length == 0;
    }
}
