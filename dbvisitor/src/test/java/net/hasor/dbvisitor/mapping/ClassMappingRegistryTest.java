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
import net.hasor.dbvisitor.keyholder.sequence.JdbcKeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.handler.EnumTypeHandler;
import net.hasor.dbvisitor.types.handler.LongTypeHandler;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsDateTypeHandler;
import net.hasor.dbvisitor.types.handler.StringTypeHandler;
import net.hasor.test.dto.ResourceType;
import net.hasor.test.entity.*;
import net.hasor.test.entity2.*;
import org.junit.Test;

import java.util.Date;

/**
 * TableMappingResolve 的公共方法
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class ClassMappingRegistryTest {

    @Test
    public void loadEntityTest_0() {
        MappingRegistry registry = new MappingRegistry();

        registry.loadEntity(BlobResourceV1.class);
        registry.loadEntity(BlobResourceV2.class);
        registry.loadEntity(BlobResourceV3.class);
        registry.loadEntity(BlobResourceV4.class);
        registry.loadEntity(BlobResourceV5.class);

        TableMapping<Object> mapping1 = registry.findEntity(BlobResourceV1.class);
        TableMapping<Object> mapping2 = registry.findEntity(BlobResourceV2.class);
        TableMapping<Object> mapping3 = registry.findEntity(BlobResourceV3.class);
        TableMapping<Object> mapping4 = registry.findEntity(BlobResourceV4.class);
        TableMapping<Object> mapping5 = registry.findEntity(BlobResourceV5.class);

        assert mapping1 != null;
        assert mapping2 != null;
        assert mapping3 != null;
        assert mapping4 != null;
        assert mapping5 != null;
    }

    @Test
    public void loadEntityTest_1() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV1.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV1.class);

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription().getComment().equals("test table");
        assert mapping.entityType().equals(BlobResourceV1.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() != null;
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new JdbcKeySeqHolderFactory().createHolder(null).getClass();
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription().getSqlType().equals("bigint");
        assert mapping.getPropertyByName("id").getDescription().isNullable();

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert !mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription().getSqlType().equals("datetime");
        assert mapping.getPropertyByName("gmtCreate").getDescription().getDefault().equals("CURRENT_TIMESTAMP");
        assert !mapping.getPropertyByName("gmtCreate").getDescription().isNullable();

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription().getSqlType().equals("varchar(255)");
        assert mapping.getPropertyByName("ownerName").getDescription().getDefault().equals("");
        assert mapping.getPropertyByName("ownerName").getDescription().isNullable();

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription().getSqlType().equals("varchar(64)");
        assert mapping.getPropertyByName("ownerType").getDescription().getDefault().equals("");
        assert mapping.getPropertyByName("ownerType").getDescription().isNullable();

    }

    @Test
    public void loadEntityTest_2() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV2.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV2.class);

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV2.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() != null;
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new JdbcKeySeqHolderFactory().createHolder(null).getClass();
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert !mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_3() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV3.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV3.class);

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("blob_resource_v3");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV3.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() != null;
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new JdbcKeySeqHolderFactory().createHolder(null).getClass();
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert !mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_4() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV4.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV4.class);

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("BlobResourceV4");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV4.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert !mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmtCreate");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("ownerName");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("ownerType");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_5() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV5.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV5.class);

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("BlobResourceV5");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV5.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert !mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmtCreate");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("ownerName");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("ownerType");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_6() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV6.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV6.class);

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription().getComment().equals("test table");
        assert mapping.entityType().equals(BlobResourceV6.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() != null;
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new JdbcKeySeqHolderFactory().createHolder(null).getClass();
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription().getSqlType().equals("bigint");
        assert mapping.getPropertyByName("id").getDescription().isNullable();

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert !mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription().getSqlType().equals("datetime");
        assert mapping.getPropertyByName("gmtCreate").getDescription().getDefault().equals("CURRENT_TIMESTAMP");
        assert !mapping.getPropertyByName("gmtCreate").getDescription().isNullable();

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription().getSqlType().equals("varchar(255)");
        assert mapping.getPropertyByName("ownerName").getDescription().getDefault().equals("");
        assert mapping.getPropertyByName("ownerName").getDescription().isNullable();

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription().getSqlType().equals("varchar(64)");
        assert mapping.getPropertyByName("ownerType").getDescription().getDefault().equals("");
        assert mapping.getPropertyByName("ownerType").getDescription().isNullable();

    }

    @Test
    public void loadEntityTest_7() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV7.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV7.class);

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV7.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() != null;
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new JdbcKeySeqHolderFactory().createHolder(null).getClass();
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert !mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_8() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV8.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV8.class);

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource_v8");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV8.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() != null;
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new JdbcKeySeqHolderFactory().createHolder(null).getClass();
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert !mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_9() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV9.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV9.class);

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource_v9");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV9.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert !mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }

    @Test
    public void loadEntityTest_10() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntity(BlobResourceV10.class);
        TableMapping<?> mapping = registry.findEntity(BlobResourceV10.class);

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource_v10");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV10.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 7;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("gmtModified") != null;
        assert mapping.getPropertyByName("instanceId") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;
        assert mapping.getPropertyByName("content") != null;

        assert mapping.getPropertyByName("id") != null;
        assert !mapping.getPropertyByName("id").isPrimaryKey();
        assert mapping.getPropertyByName("id").isUpdate();
        assert mapping.getPropertyByName("id").isInsert();
        assert mapping.getPropertyByName("id").getJavaType() == Long.class;
        assert mapping.getPropertyByName("id").getTypeHandler().getClass() == LongTypeHandler.class;
        assert mapping.getPropertyByName("id").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("id").getColumn().equals("id");
        assert mapping.getPropertyByName("id").getDescription() == null;

        assert mapping.getPropertyByName("gmtCreate") != null;
        assert !mapping.getPropertyByName("gmtCreate").isPrimaryKey();
        assert mapping.getPropertyByName("gmtCreate").isUpdate();
        assert mapping.getPropertyByName("gmtCreate").isInsert();
        assert mapping.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert mapping.getPropertyByName("gmtCreate").getTypeHandler().getClass() == SqlTimestampAsDateTypeHandler.class;
        assert mapping.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert mapping.getPropertyByName("gmtCreate").getDescription() == null;

        assert mapping.getPropertyByName("ownerName") != null;
        assert !mapping.getPropertyByName("ownerName").isPrimaryKey();
        assert mapping.getPropertyByName("ownerName").isUpdate();
        assert mapping.getPropertyByName("ownerName").isInsert();
        assert mapping.getPropertyByName("ownerName").getJavaType() == String.class;
        assert mapping.getPropertyByName("ownerName").getTypeHandler().getClass() == StringTypeHandler.class;
        assert mapping.getPropertyByName("ownerName").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerName").getColumn().equals("owner_name");
        assert mapping.getPropertyByName("ownerName").getDescription() == null;

        assert mapping.getPropertyByName("ownerType") != null;
        assert !mapping.getPropertyByName("ownerType").isPrimaryKey();
        assert mapping.getPropertyByName("ownerType").isUpdate();
        assert mapping.getPropertyByName("ownerType").isInsert();
        assert mapping.getPropertyByName("ownerType").getJavaType() == ResourceType.class;
        assert mapping.getPropertyByName("ownerType").getTypeHandler().getClass() == EnumTypeHandler.class;
        assert mapping.getPropertyByName("ownerType").getKeySeqHolder() == null;
        assert mapping.getPropertyByName("ownerType").getColumn().equals("owner_type");
        assert mapping.getPropertyByName("ownerType").getDescription() == null;
    }
}