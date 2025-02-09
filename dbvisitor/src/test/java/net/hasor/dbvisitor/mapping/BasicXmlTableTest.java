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
import net.hasor.dbvisitor.mapping.dto.PojoBean1;
import net.hasor.dbvisitor.types.handler.number.LongTypeHandler;
import net.hasor.dbvisitor.types.handler.string.EnumTypeHandler;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsDateTypeHandler;
import net.hasor.test.dto.ResourceType;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class BasicXmlTableTest {
    @Test
    public void xmlTableInfo_1() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_1.xml");
        TableMapping<?> tab = registry.findByTable("master", "dbo", "blob_resource");

        assert tab.getCatalog().equals("master");
        assert tab.getSchema().equals("dbo");
        assert tab.getTable().equals("blob_resource");
        assert tab.entityType() == PojoBean1.class;
        assert tab.isAutoProperty();
        assert !tab.useDelimited();
        assert tab.isCaseInsensitive();
        assert !tab.isToCamelCase();
        assert !tab.isMapEntity();

        assert tab.getDescription() != null;
        assert tab.getDescription().getDdlAuto() == DdlAuto.None;
        assert tab.getDescription().getCharacterSet() == null;
        assert tab.getDescription().getCollation() == null;
        assert tab.getDescription().getComment().equals("test table");
        assert tab.getDescription().getOther() == null;

        assert tab.getPropertyByName("id").isPrimaryKey();
        assert tab.getPropertyByName("id").isInsert();
        assert tab.getPropertyByName("id").isUpdate();
        assert tab.getPropertyByName("id").getColumn().equals("id");
        assert tab.getPropertyByName("id").getJavaType() == Long.class;
        assert tab.getPropertyByName("id").getTypeHandler() instanceof LongTypeHandler;
        assert tab.getPropertyByName("id").getKeySeqHolder().toString().startsWith("Auto@");
        assert tab.getPropertyByName("id").getDescription().getSqlType().equals("bigint");
        assert !tab.getPropertyByName("id").getDescription().isNullable();
        assert tab.getPropertyByName("id").getDescription().getDefault() == null;

        assert !tab.getPropertyByName("gmtCreate").isPrimaryKey();
        assert tab.getPropertyByName("gmtCreate").isInsert();
        assert !tab.getPropertyByName("gmtCreate").isUpdate();
        assert tab.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert tab.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtCreate").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtCreate").getDescription().getSqlType().equals("datetime");
        assert !tab.getPropertyByName("gmtCreate").getDescription().isNullable();
        assert tab.getPropertyByName("gmtCreate").getDescription().getDefault().equals("CURRENT_TIMESTAMP");

        assert !tab.getPropertyByName("gmtModified").isPrimaryKey();
        assert tab.getPropertyByName("gmtModified").isInsert();
        assert tab.getPropertyByName("gmtModified").isUpdate();
        assert tab.getPropertyByName("gmtModified").getColumn().equals("gmt_modified");
        assert tab.getPropertyByName("gmtModified").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtModified").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByName("gmtModified").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtModified").getDescription().getSqlType().equals("datetime");
        assert !tab.getPropertyByName("gmtModified").getDescription().isNullable();
        assert tab.getPropertyByName("gmtModified").getDescription().getDefault().equals("CURRENT_TIMESTAMP");

        assert !tab.getPropertyByName("instanceId").isPrimaryKey();
        assert tab.getPropertyByName("instanceId").isInsert();
        assert tab.getPropertyByName("instanceId").isUpdate();
        assert tab.getPropertyByName("instanceId").getColumn().equals("instance_id");
        assert tab.getPropertyByName("instanceId").getJavaType() == String.class;
        assert tab.getPropertyByName("instanceId").getTypeHandler() instanceof StringTypeHandler;
        assert tab.getPropertyByName("instanceId").getKeySeqHolder() == null;
        assert tab.getPropertyByName("instanceId").getDescription().getSqlType().equals("varchar(64)");
        assert tab.getPropertyByName("instanceId").getDescription().isNullable();
        assert tab.getPropertyByName("instanceId").getDescription().getDefault() == null;

        assert !tab.getPropertyByName("ownerType").isPrimaryKey();
        assert tab.getPropertyByName("ownerType").isInsert();
        assert tab.getPropertyByName("ownerType").isUpdate();
        assert tab.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert tab.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert tab.getPropertyByName("ownerType").getTypeHandler() instanceof EnumTypeHandler;
        assert tab.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert tab.getPropertyByName("ownerType").getDescription().getSqlType().equals("varchar(64)");
        assert tab.getPropertyByName("ownerType").getDescription().isNullable();
        assert tab.getPropertyByName("ownerType").getDescription().getDefault() == null;

        assert tab.getIndex("idx_a").getName().equals("idx_a");
        assert !tab.getIndex("idx_a").isUnique();
        assert tab.getIndex("idx_a").getColumns().size() == 2;
        assert tab.getIndex("idx_a").getColumns().get(0).equals("gmt_modified");
        assert tab.getIndex("idx_a").getColumns().get(1).equals("instanceId");

        assert tab.getIndex("uk_b").getName().equals("uk_b");
        assert tab.getIndex("uk_b").isUnique();
        assert tab.getIndex("uk_b").getColumns().size() == 1;
        assert tab.getIndex("uk_b").getColumns().get(0).equals("instanceId");
    }

    @Test
    public void xmlTableInfo_2_1() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_1.xml");
        TableMapping<?> ent1 = registry.findByTable("master", "dbo", "blob_resource");

        TableMapping<?> def1 = registry.findByEntity(PojoBean1.class);
        TableMapping<?> def2 = registry.findBySpace("", PojoBean1.class);
        TableMapping<?> def3 = registry.findBySpace("", PojoBean1.class.getName());
        TableMapping<?> def4 = registry.findByTable("master", "dbo", "blob_resource", null);

        assert ent1 == def1;
        assert ent1 == def2;
        assert ent1 == def3;
        assert ent1 == def4;
    }

    @Test
    public void xmlTableInfo_2_2() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_2.xml");
        TableMapping<?> ent1 = registry.findByTable("master", "dbo", "blob_resource");

        TableMapping<?> def1 = registry.findByEntity(PojoBean1.class);
        TableMapping<?> def2 = registry.findBySpace("resultMap_test", PojoBean1.class);
        TableMapping<?> def3 = registry.findBySpace("resultMap_test", PojoBean1.class.getName());
        TableMapping<?> def4 = registry.findByTable("master", "dbo", "blob_resource", null);

        assert null == def1;
        assert ent1 == def2;
        assert ent1 == def3;
        assert ent1 == def4;
    }

    @Test
    public void xmlTableInfo_3() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_3.xml");
        TableMapping<?> ent1 = registry.findBySpace("abc", "aac");

        TableMapping<?> def1 = registry.findByEntity(PojoBean1.class);
        TableMapping<?> def2 = registry.findBySpace("", PojoBean1.class);
        TableMapping<?> def3 = registry.findBySpace("", PojoBean1.class.getName());
        TableMapping<?> def4 = registry.findByTable("master", "dbo", "blob_resource");
        TableMapping<?> def5 = registry.findByTable("master", "dbo", "blob_resource", null);

        assert null == def1;
        assert null == def2;
        assert null == def3;
        assert ent1 == def4;
        assert ent1 == def5;

        TableMapping<?> def6 = registry.findBySpace("abc", PojoBean1.class);
        TableMapping<?> def7 = registry.findBySpace("abc", PojoBean1.class.getName());
        assert null == def6;
        assert null == def7;

        TableMapping<?> def8 = registry.findBySpace("abc", "aac");
        assert ent1 == def8;
    }

    @Test
    public void error_1() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_1.xml");
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_1.xml");
        assert true;
    }

    @Test
    public void error_2() throws IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_1.xml");
        try {
            registry.loaded.clear();// clear cache
            registry.loadMapping("/dbvisitor_coverage/basic_mapping/basic_entity_1.xml");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().endsWith("the entity '" + PojoBean1.class.getName() + "' already exists.");
        }
    }
}
