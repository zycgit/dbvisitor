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

import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.mapper.def.*;
import net.hasor.dbvisitor.mapper.dto.*;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class ApiConfigTwoMapperTest {
    @Test
    public void configQuery_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configQuery1");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getConfigId().equals("configQuery1");
        assert def.getResultType() == UserInfo.class;
        assert def.getResultExtractor() == null;
        assert def.getResultRowCallback() == null;
        assert def.getResultRowMapper() instanceof BeanMappingRowMapper;
        assert ((SelectConfig) def.getConfig()).getType() == QueryType.Select;
        assert ((SelectConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((SelectConfig) def.getConfig()).getTimeout() == 123;
        assert ((SelectConfig) def.getConfig()).getFetchSize() == 512;
        assert ((SelectConfig) def.getConfig()).getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert ((SelectConfig) def.getConfig()).getResultMapSpace().equals("net.hasor.dbvisitor.mapper.dto.ApiBasicConfigTwoMapper");
        assert ((SelectConfig) def.getConfig()).getResultMapId().equals("userInfo");
        assert ((SelectConfig) def.getConfig()).getResultType() == null;
        assert ((SelectConfig) def.getConfig()).getResultSetExtractor() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowCallback() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowMapper() == null;
        assert ((SelectConfig) def.getConfig()).getBindOut()[0].equals("out1");
        assert ((SelectConfig) def.getConfig()).getBindOut()[1].equals("out2");
    }

    @Test
    public void configQuery_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configQuery2");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getConfigId().equals("configQuery2");
        assert def.getResultType() == UserInfoExt.class;
        assert def.getResultExtractor() == null;
        assert def.getResultRowCallback() == null;
        assert def.getResultRowMapper() instanceof BeanMappingRowMapper;
        assert ((SelectConfig) def.getConfig()).getType() == QueryType.Select;
        assert ((SelectConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((SelectConfig) def.getConfig()).getTimeout() == 123;
        assert ((SelectConfig) def.getConfig()).getFetchSize() == 512;
        assert ((SelectConfig) def.getConfig()).getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert ((SelectConfig) def.getConfig()).getResultMapSpace().equals("net.hasor.dbvisitor.mapper.dto.ApiBasicConfigTwoMapper");
        assert ((SelectConfig) def.getConfig()).getResultMapId() == null;
        assert ((SelectConfig) def.getConfig()).getResultType().equals("net.hasor.dbvisitor.mapper.dto.UserInfoExt");
        assert ((SelectConfig) def.getConfig()).getResultSetExtractor() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowCallback() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowMapper() == null;
        assert ((SelectConfig) def.getConfig()).getBindOut()[0].equals("out1");
        assert ((SelectConfig) def.getConfig()).getBindOut()[1].equals("out2");
    }

    @Test
    public void configQuery_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configQuery3");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getConfigId().equals("configQuery3");
        assert def.getResultType() == null;
        assert def.getResultExtractor() instanceof UserNameResultSetExtractor;
        assert def.getResultRowCallback() == null;
        assert def.getResultRowMapper() == null;
        assert ((SelectConfig) def.getConfig()).getType() == QueryType.Select;
        assert ((SelectConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((SelectConfig) def.getConfig()).getTimeout() == 123;
        assert ((SelectConfig) def.getConfig()).getFetchSize() == 512;
        assert ((SelectConfig) def.getConfig()).getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert ((SelectConfig) def.getConfig()).getResultMapSpace().equals("net.hasor.dbvisitor.mapper.dto.ApiBasicConfigTwoMapper");
        assert ((SelectConfig) def.getConfig()).getResultMapId() == null;
        assert ((SelectConfig) def.getConfig()).getResultType() == null;
        assert ((SelectConfig) def.getConfig()).getResultSetExtractor().equals("net.hasor.dbvisitor.mapper.dto.UserNameResultSetExtractor");
        assert ((SelectConfig) def.getConfig()).getResultRowCallback() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowMapper() == null;
        assert ((SelectConfig) def.getConfig()).getBindOut()[0].equals("out1");
        assert ((SelectConfig) def.getConfig()).getBindOut()[1].equals("out2");
    }

    @Test
    public void configQuery_4() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configQuery4");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getConfigId().equals("configQuery4");
        assert def.getResultType() == null;
        assert def.getResultExtractor() == null;
        assert def.getResultRowCallback() instanceof UserNameRowCallback;
        assert def.getResultRowMapper() == null;
        assert ((SelectConfig) def.getConfig()).getType() == QueryType.Select;
        assert ((SelectConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((SelectConfig) def.getConfig()).getTimeout() == 123;
        assert ((SelectConfig) def.getConfig()).getFetchSize() == 512;
        assert ((SelectConfig) def.getConfig()).getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert ((SelectConfig) def.getConfig()).getResultMapSpace().equals("net.hasor.dbvisitor.mapper.dto.ApiBasicConfigTwoMapper");
        assert ((SelectConfig) def.getConfig()).getResultMapId() == null;
        assert ((SelectConfig) def.getConfig()).getResultType() == null;
        assert ((SelectConfig) def.getConfig()).getResultSetExtractor() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowCallback().equals("net.hasor.dbvisitor.mapper.dto.UserNameRowCallback");
        assert ((SelectConfig) def.getConfig()).getResultRowMapper() == null;
        assert ((SelectConfig) def.getConfig()).getBindOut()[0].equals("out1");
        assert ((SelectConfig) def.getConfig()).getBindOut()[1].equals("out2");
    }

    @Test
    public void configQuery_5() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configQuery5");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getConfigId().equals("configQuery5");
        assert def.getResultType() == null;
        assert def.getResultExtractor() == null;
        assert def.getResultRowCallback() == null;
        assert def.getResultRowMapper() instanceof UserNameRowMapper;
        assert ((SelectConfig) def.getConfig()).getType() == QueryType.Select;
        assert ((SelectConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((SelectConfig) def.getConfig()).getTimeout() == 123;
        assert ((SelectConfig) def.getConfig()).getFetchSize() == 512;
        assert ((SelectConfig) def.getConfig()).getResultSetType() == ResultSetType.FORWARD_ONLY;
        assert ((SelectConfig) def.getConfig()).getResultMapSpace().equals("net.hasor.dbvisitor.mapper.dto.ApiBasicConfigTwoMapper");
        assert ((SelectConfig) def.getConfig()).getResultMapId() == null;
        assert ((SelectConfig) def.getConfig()).getResultType() == null;
        assert ((SelectConfig) def.getConfig()).getResultSetExtractor() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowCallback() == null;
        assert ((SelectConfig) def.getConfig()).getResultRowMapper().equals("net.hasor.dbvisitor.mapper.dto.UserNameRowMapper");
        assert ((SelectConfig) def.getConfig()).getBindOut()[0].equals("out1");
        assert ((SelectConfig) def.getConfig()).getBindOut()[1].equals("out2");
    }

    @Test
    public void configInsert_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configInsert");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getResultType() == long.class;
        assert ((InsertConfig) def.getConfig()).getType() == QueryType.Insert;
        assert ((InsertConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((InsertConfig) def.getConfig()).getTimeout() == 123;
        assert ((InsertConfig) def.getConfig()).isUseGeneratedKeys();
        assert ((InsertConfig) def.getConfig()).getKeyProperty().equals("numId");
        assert ((InsertConfig) def.getConfig()).getKeyColumn().equals("num_id");
        assert ((InsertConfig) def.getConfig()).getSelectKey() == null;
    }

    @Test
    public void configInsert_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configInsertSelectKey");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getResultType() == long.class;
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
    }

    @Test
    public void configUpdate_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configUpdate");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getResultType() == long.class;
        assert ((UpdateConfig) def.getConfig()).getType() == QueryType.Update;
        assert ((UpdateConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((UpdateConfig) def.getConfig()).getTimeout() == 123;
    }

    @Test
    public void configDelete_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configDelete");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getResultType() == long.class;
        assert ((DeleteConfig) def.getConfig()).getType() == QueryType.Delete;
        assert ((DeleteConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((DeleteConfig) def.getConfig()).getTimeout() == 123;
    }

    @Test
    public void configExecute_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ApiBasicConfigTwoMapper.class);

        StatementDef def = registry.findStatement(ApiBasicConfigTwoMapper.class, "configExecute");
        assert def != null;
        assert def.getConfigNamespace().equals(ApiBasicConfigTwoMapper.class.getName());
        assert def.getResultType() == null;
        assert ((ExecuteConfig) def.getConfig()).getType() == QueryType.Execute;
        assert ((ExecuteConfig) def.getConfig()).getStatementType() == StatementType.Callable;
        assert ((ExecuteConfig) def.getConfig()).getTimeout() == 123;
    }
}
