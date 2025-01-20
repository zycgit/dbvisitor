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

import net.hasor.dbvisitor.mapper.def.*;
import net.hasor.dbvisitor.mapper.dto.ApiBasicConfigTwoMapper;
import net.hasor.dbvisitor.mapper.dto.UserInfo;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import org.junit.Test;

import java.io.IOException;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class ApiConfigTwoMapperTest {
    @Test
    public void configQuery_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configQuery");
        assert def != null;
        assert def.getNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getMappingType() == UserInfo.class;
        assert ((SelectConfig) def.getConfig()).getType() == QueryType.Select;
        assert ((SelectConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((SelectConfig) def.getConfig()).getTimeout() == 123;
        assert ((SelectConfig) def.getConfig()).getFetchSize() == 512;
        assert ((SelectConfig) def.getConfig()).getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert ((SelectConfig) def.getConfig()).getBindOut()[0].equals("out1");
        assert ((SelectConfig) def.getConfig()).getBindOut()[1].equals("out2");
    }

    @Test
    public void configInsert_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configInsert");
        assert def != null;
        assert def.getNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getMappingType() == long.class;
        assert ((InsertConfig) def.getConfig()).getType() == QueryType.Insert;
        assert ((InsertConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((InsertConfig) def.getConfig()).getTimeout() == 123;
        assert ((InsertConfig) def.getConfig()).isUseGeneratedKeys();
        assert ((InsertConfig) def.getConfig()).getKeyProperty().equals("numId");
        assert ((InsertConfig) def.getConfig()).getKeyColumn().equals("num_id");
        assert ((InsertConfig) def.getConfig()).getSelectKey() == null;
    }

    @Test
    public void configInsert_2() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configInsertSelectKey");
        assert def != null;
        assert def.getNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getMappingType() == long.class;
        assert ((InsertConfig) def.getConfig()).getType() == QueryType.Insert;
        assert ((InsertConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((InsertConfig) def.getConfig()).getTimeout() == 123;
        assert ((InsertConfig) def.getConfig()).isUseGeneratedKeys();
        assert ((InsertConfig) def.getConfig()).getKeyProperty().equals("numId");
        assert ((InsertConfig) def.getConfig()).getKeyColumn().equals("num_id");
        assert ((InsertConfig) def.getConfig()).getSelectKey() != null;

        SelectKeyConfig keyConfig = ((InsertConfig) def.getConfig()).getSelectKey();
        assert keyConfig.getType() == QueryType.Select;
        assert keyConfig.getStatementType() == StatementType.Callable;
        assert keyConfig.getTimeout() == 123;
        assert keyConfig.getFetchSize() == 512;
        assert keyConfig.getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert keyConfig.getKeyProperty().equals("userUuid");
        assert keyConfig.getKeyColumn().equals("uid");
        assert keyConfig.getOrder().equals("AFTER");
        assert keyConfig.getResultType().equals("java.lang.String");
        assert keyConfig.getResultHandler().equals(StringTypeHandler.class.getName());
    }

    @Test
    public void configUpdate_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configUpdate");
        assert def != null;
        assert def.getNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getMappingType() == long.class;
        assert ((UpdateConfig) def.getConfig()).getType() == QueryType.Update;
        assert ((UpdateConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((UpdateConfig) def.getConfig()).getTimeout() == 123;
        assert ((UpdateConfig) def.getConfig()).getSelectKey() == null;
    }

    @Test
    public void configDelete_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configDelete");
        assert def != null;
        assert def.getNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getMappingType() == long.class;
        assert ((DeleteConfig) def.getConfig()).getType() == QueryType.Delete;
        assert ((DeleteConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((DeleteConfig) def.getConfig()).getTimeout() == 123;
        assert ((DeleteConfig) def.getConfig()).getSelectKey() == null;
    }

    @Test
    public void configExecute_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configExecute");
        assert def != null;
        assert def.getNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getMappingType() == null;
        assert ((ExecuteConfig) def.getConfig()).getType() == QueryType.Execute;
        assert ((ExecuteConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((ExecuteConfig) def.getConfig()).getTimeout() == 123;
    }
}
