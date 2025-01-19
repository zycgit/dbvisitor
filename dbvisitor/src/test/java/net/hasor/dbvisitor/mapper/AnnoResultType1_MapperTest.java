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

import net.hasor.dbvisitor.mapper.def.QueryType;
import net.hasor.dbvisitor.mapper.dto.AnnoResultType1_Mapper;
import org.junit.Test;

import java.io.IOException;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class AnnoResultType1_MapperTest {
    @Test
    public void selectByte_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectByte_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == byte.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectByte_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectByte_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Byte.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectShort_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectShort_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == short.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectShort_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectShort_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Short.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectInt_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectInt_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == int.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectInt_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectInt_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Integer.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLong_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectLong_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == long.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLong_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectLong_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Long.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectFloat_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectFloat_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == float.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectFloat_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectFloat_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Float.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDouble_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectDouble_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == double.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDouble_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectDouble_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Double.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectChar_1() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectChar_1");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == char.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectChar_2() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectChar_2");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == Character.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectVoid() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectVoid");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == null;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectString() throws ReflectiveOperationException, IOException {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoResultType1_Mapper.class);

        StatementDef def = registry.findStatement(AnnoResultType1_Mapper.class, "selectString");
        assert def != null;
        assert def.getNamespace().equals(AnnoResultType1_Mapper.class.getName());
        assert def.getMappingType() == String.class;
        assert def.getConfig().getType() == QueryType.Select;
    }
}
