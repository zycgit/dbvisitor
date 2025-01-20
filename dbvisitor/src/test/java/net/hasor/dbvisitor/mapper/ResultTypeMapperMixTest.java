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

import net.hasor.dbvisitor.mapper.dto.ResultTypeMixMapper;
import org.junit.Test;

import java.io.IOException;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class ResultTypeMapperMixTest {
    @Test
    public void selectBool_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectBool_1");
        assert def.getMappingType() == boolean.class;
    }

    @Test
    public void selectBool_2() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectBool_2");
        assert def.getMappingType() == boolean.class;
    }

    @Test
    public void selectBool_3() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectBool_3");
        assert def.getMappingType() == boolean.class;
    }

    @Test
    public void selectShort_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectShort_1");
        assert def.getMappingType() == Short.class;
    }

    @Test
    public void selectShort_2() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectShort_2");
        assert def.getMappingType() == Short.class;
    }

    @Test
    public void selectDate_1() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectDate_1");
        assert def.getMappingType() == java.util.Date.class;
    }

    @Test
    public void selectDate_2() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectDate_2");
        assert def.getMappingType() == java.sql.Date.class;
    }

    @Test
    public void selectDate_3() throws IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectDate_3");
        assert def.getMappingType() == java.sql.Time.class;
    }
}
