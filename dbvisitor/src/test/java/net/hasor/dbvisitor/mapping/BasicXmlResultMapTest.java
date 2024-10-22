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
package net.hasor.dbvisitor.mapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.dto.AnnoTableBean1;
import net.hasor.dbvisitor.mapping.dto.PojoBean1;
import net.hasor.dbvisitor.types.handler.EnumTypeHandler;
import net.hasor.dbvisitor.types.handler.LongTypeHandler;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsDateTypeHandler;
import net.hasor.dbvisitor.types.handler.StringTypeHandler;
import net.hasor.test.dto.ResourceType;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class BasicXmlResultMapTest {
    @Test
    public void xmlResultMap_1_1() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapping/basic_result_1.xml");
        TableMapping<?> tab = registry.findUsingSpace("", "pojo_bean1");

        assert tab.getCatalog().equals("");
        assert tab.getSchema().equals("");
        assert tab.getTable().equals("");
        assert tab.entityType() == PojoBean1.class;
        assert tab.isAutoProperty();
        assert !tab.useDelimited();
        assert tab.isCaseInsensitive();
        assert !tab.isToCamelCase();
        assert tab.getDialect() == null;
        assert !tab.isMapEntity();

        assert tab.getDescription() == null;

        assert !tab.getPropertyByName("id").isPrimaryKey();
        assert tab.getPropertyByName("id").isInsert();
        assert tab.getPropertyByName("id").isUpdate();
        assert tab.getPropertyByName("id").getColumn().equals("id");
        assert tab.getPropertyByName("id").getJavaType() == Long.class;
        assert tab.getPropertyByName("id").getTypeHandler() instanceof LongTypeHandler;
        assert tab.getPropertyByName("id").getKeySeqHolder() == null;
        assert tab.getPropertyByName("id").getDescription() == null;

        assert !tab.getPropertyByName("gmtCreate").isPrimaryKey();
        assert tab.getPropertyByName("gmtCreate").isInsert();
        assert tab.getPropertyByName("gmtCreate").isUpdate();
        assert tab.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert tab.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtCreate").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtCreate").getDescription() == null;

        assert !tab.getPropertyByName("gmtModified").isPrimaryKey();
        assert tab.getPropertyByName("gmtModified").isInsert();
        assert tab.getPropertyByName("gmtModified").isUpdate();
        assert tab.getPropertyByName("gmtModified").getColumn().equals("gmt_modified");
        assert tab.getPropertyByName("gmtModified").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtModified").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByName("gmtModified").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtModified").getDescription() == null;

        assert !tab.getPropertyByName("instanceId").isPrimaryKey();
        assert tab.getPropertyByName("instanceId").isInsert();
        assert tab.getPropertyByName("instanceId").isUpdate();
        assert tab.getPropertyByName("instanceId").getColumn().equals("instance_id");
        assert tab.getPropertyByName("instanceId").getJavaType() == String.class;
        assert tab.getPropertyByName("instanceId").getTypeHandler() instanceof StringTypeHandler;
        assert tab.getPropertyByName("instanceId").getKeySeqHolder() == null;
        assert tab.getPropertyByName("instanceId").getDescription() == null;

        assert !tab.getPropertyByName("ownerType").isPrimaryKey();
        assert tab.getPropertyByName("ownerType").isInsert();
        assert tab.getPropertyByName("ownerType").isUpdate();
        assert tab.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert tab.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert tab.getPropertyByName("ownerType").getTypeHandler() instanceof EnumTypeHandler;
        assert tab.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert tab.getPropertyByName("ownerType").getDescription() == null;

        assert tab.getIndexes().size() == 0;
    }

    @Test
    public void xmlResultMap_1_2() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapping/basic_result_1.xml");
        TableMapping<?> tab = registry.findUsingSpace("", "pojo_bean2");

        assert tab.getCatalog().equals("");
        assert tab.getSchema().equals("");
        assert tab.getTable().equals("");
        assert tab.entityType() == AnnoTableBean1.class;
        assert tab.isAutoProperty();
        assert !tab.useDelimited();
        assert tab.isCaseInsensitive();
        assert !tab.isToCamelCase();
        assert tab.getDialect() == null;
        assert !tab.isMapEntity();

        assert tab.getDescription() == null;

        assert !tab.getPropertyByName("id").isPrimaryKey();
        assert tab.getPropertyByName("id").isInsert();
        assert tab.getPropertyByName("id").isUpdate();
        assert tab.getPropertyByName("id").getColumn().equals("id");
        assert tab.getPropertyByName("id").getJavaType() == Long.class;
        assert tab.getPropertyByName("id").getTypeHandler() instanceof LongTypeHandler;
        assert tab.getPropertyByName("id").getKeySeqHolder() == null;
        assert tab.getPropertyByName("id").getDescription() == null;

        assert !tab.getPropertyByName("gmtCreate").isPrimaryKey();
        assert tab.getPropertyByName("gmtCreate").isInsert();
        assert tab.getPropertyByName("gmtCreate").isUpdate();
        assert tab.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert tab.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtCreate").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtCreate").getDescription() == null;

        assert !tab.getPropertyByName("gmtModified").isPrimaryKey();
        assert tab.getPropertyByName("gmtModified").isInsert();
        assert tab.getPropertyByName("gmtModified").isUpdate();
        assert tab.getPropertyByName("gmtModified").getColumn().equals("gmt_modified");
        assert tab.getPropertyByName("gmtModified").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtModified").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByName("gmtModified").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtModified").getDescription() == null;

        assert !tab.getPropertyByName("instanceId").isPrimaryKey();
        assert tab.getPropertyByName("instanceId").isInsert();
        assert tab.getPropertyByName("instanceId").isUpdate();
        assert tab.getPropertyByName("instanceId").getColumn().equals("instance_id");
        assert tab.getPropertyByName("instanceId").getJavaType() == String.class;
        assert tab.getPropertyByName("instanceId").getTypeHandler() instanceof StringTypeHandler;
        assert tab.getPropertyByName("instanceId").getKeySeqHolder() == null;
        assert tab.getPropertyByName("instanceId").getDescription() == null;

        assert !tab.getPropertyByName("ownerType").isPrimaryKey();
        assert tab.getPropertyByName("ownerType").isInsert();
        assert tab.getPropertyByName("ownerType").isUpdate();
        assert tab.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert tab.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert tab.getPropertyByName("ownerType").getTypeHandler() instanceof EnumTypeHandler;
        assert tab.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert tab.getPropertyByName("ownerType").getDescription() == null;

        assert tab.getIndexes().size() == 0;
    }

    @Test
    public void xmlResultMap_2() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapping/basic_result_2.xml");

        TableMapping<?> def1 = registry.findUsingSpace(PojoBean1.class);
        TableMapping<?> def2 = registry.findUsingSpace("resultMap_test", PojoBean1.class);
        TableMapping<?> def3 = registry.findUsingSpace("resultMap_test", PojoBean1.class.getName());

        assert null == def1;
        assert def2 == def3;
        assert def2 != null;
    }

    @Test
    public void xmlResultMap_3() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapping/basic_result_3.xml");
        TableMapping<?> ent1 = registry.findUsingSpace("abc", "aac");

        TableMapping<?> def1 = registry.findUsingSpace(PojoBean1.class);
        TableMapping<?> def2 = registry.findUsingSpace("", PojoBean1.class);
        TableMapping<?> def3 = registry.findUsingSpace("", PojoBean1.class.getName());

        assert ent1 != null;
        assert null == def1;
        assert null == def2;
        assert null == def3;

        TableMapping<?> def6 = registry.findUsingSpace("abc", PojoBean1.class);
        TableMapping<?> def7 = registry.findUsingSpace("abc", PojoBean1.class.getName());
        assert null == def6;
        assert null == def7;

        TableMapping<?> def8 = registry.findUsingSpace("abc", "aac");
        assert ent1 == def8;
    }

    @Test
    public void error_1() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapping/basic_result_1.xml");
        try {
            registry.loadMapper("/dbvisitor_coverage/basic_mapping/basic_result_1.xml");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().endsWith("the resultMap 'pojo_bean1' already exists.");
        }
    }
}
