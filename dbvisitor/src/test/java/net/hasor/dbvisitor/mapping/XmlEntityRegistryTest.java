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
import net.hasor.dbvisitor.mapping.def.IndexDescription;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.handler.EnumTypeHandler;
import net.hasor.dbvisitor.types.handler.LongTypeHandler;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsDateTypeHandler;
import net.hasor.dbvisitor.types.handler.StringTypeHandler;
import net.hasor.test.dto.ResourceType;
import net.hasor.test.entity.*;
import net.hasor.test.entity2.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TableMappingResolve 的公共方法
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class XmlEntityRegistryTest {

    @Test
    public void findEntityTest_1() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_1");

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription().getComment().equals("test table");
        assert mapping.entityType().equals(BlobResourceV1.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();
        assert mapping.getDescription().getDdlAuto() == DdlAuto.None;

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

        List<IndexDescription> indexes = mapping.getIndexes();
        Map<String, IndexDescription> idxMap = indexes.stream().collect(Collectors.toMap(IndexDescription::getName, i -> i));
        assert idxMap.get("idx_a").getColumns().contains("gmt_modified");
        assert idxMap.get("idx_a").getColumns().contains("instanceId");
        assert !idxMap.get("idx_a").isUnique();
        assert idxMap.get("uk_b").getColumns().contains("instanceId");
        assert idxMap.get("uk_b").isUnique();
    }

    @Test
    public void findEntityTest_2() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_2");

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
    public void findEntityTest_3() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_3");

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
    public void findEntityTest_4() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_4");

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
    public void findEntityTest_5() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_5");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("blob_resource");
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
    public void findEntityTest_6() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_6");

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
    public void findEntityTest_7() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_7");

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
    public void findEntityTest_8() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_8");

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
    public void findEntityTest_9() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_9");

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
    public void findEntityTest_10() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findEntity("entityMap_10");

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource");
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

    @Test
    public void mapper2Test_1() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_2.xml");
        assert registry.findEntity("entityMap_1") == registry.findMapping("", "entityMap_1");
        TableMapping<?> mapping = registry.findEntity("entityMap_1");

        assert mapping.getCatalog().equals("master");
        assert mapping.getSchema().equals("dbo");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription().getComment().equals("test table");
        assert mapping.entityType().equals(BlobResourceV1.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 4;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
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
    public void mapper2Test_2() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_2.xml");
        assert registry.findEntity("entityMap_2") == registry.findMapping("", "entityMap_2");
        TableMapping<?> mapping = registry.findEntity("entityMap_2");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV2.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 4;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
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
    public void mapper2Test_5() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_2.xml");
        assert registry.findEntity("entityMap_5") == registry.findMapping("", "entityMap_5");
        TableMapping<?> mapping = registry.findEntity("entityMap_5");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("blob_resource");
        assert mapping.getDescription() == null;
        assert mapping.entityType().equals(BlobResourceV5.class);
        assert mapping.isAutoProperty();
        assert !mapping.useDelimited();
        assert mapping.isCaseInsensitive();

        assert mapping.getProperties().size() == 4;
        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("gmtCreate") != null;
        assert mapping.getPropertyByName("ownerName") != null;
        assert mapping.getPropertyByName("ownerType") != null;

        assert mapping.getPropertyByName("id") != null;
        assert mapping.getPropertyByName("id").isPrimaryKey();
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
    public void mapper2Test_6() throws ReflectiveOperationException, IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/entity_1.xml");
        TableMapping<BlobResourceV5> tableMapping = registry.findEntity(BlobResourceV5.class);

        assert tableMapping.getDescription().getDdlAuto() == DdlAuto.CreateDrop;

        assert tableMapping.getDescription().getComment().equals("表备注");
        assert tableMapping.getPropertyByName("id").getDescription().getSqlType().equals("bigint");
        assert tableMapping.getPropertyByName("id").getDescription().getComment().equals("ID列");
        assert !tableMapping.getPropertyByName("id").getDescription().isNullable();
        assert tableMapping.getPropertyByColumn("gmt_create").getDescription().getSqlType().equals("timestamp");
        assert !tableMapping.getPropertyByColumn("gmt_create").getDescription().isNullable();
        assert tableMapping.getPropertyByColumn("gmt_create").getDescription().getDefault().equals("current_timestamp");
        assert tableMapping.getPropertyByColumn("gmt_create").getDescription().getComment().equals("创建时间");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getSqlType().equals("timestamp");
        assert !tableMapping.getPropertyByName("gmtModified").getDescription().isNullable();
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getDefault().equals("current_timestamp");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getOther().equals("on update current_timestamp");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getComment().equals("修改时间");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getSqlType().equals("timestamp");
        assert tableMapping.getPropertyByName("content").getDescription().getLength().equals("32");
        assert tableMapping.getPropertyByName("instanceId").getDescription().getCharacterSet().equals("utf-8");
        assert tableMapping.getPropertyByName("instanceId").getDescription().getCollation().equals("abc");

        List<IndexDescription> indexes = tableMapping.getIndexes();
        Map<String, IndexDescription> idxMap = indexes.stream().collect(Collectors.toMap(IndexDescription::getName, i -> i));
        assert idxMap.get("idx_a").getColumns().contains("gmt_modified");
        assert idxMap.get("idx_a").getColumns().contains("instanceId");
        assert !idxMap.get("idx_a").isUnique();
        assert idxMap.get("uk_b").getColumns().contains("instanceId");
        assert idxMap.get("uk_b").isUnique();
    }

    @Test
    public void mapper2Test_7() throws ReflectiveOperationException, IOException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/entity_2.xml");
        TableMapping<BlobResourceV1> tableMapping = registry.findEntity(BlobResourceV1.class);

        assert tableMapping.getDescription().getComment().equals("表备注");
        assert tableMapping.getPropertyByName("id").getDescription().getSqlType().equals("bigint");
        assert tableMapping.getPropertyByName("id").getDescription().getComment().equals("ID列");
        assert !tableMapping.getPropertyByName("id").getDescription().isNullable();
        assert tableMapping.getPropertyByColumn("gmt_create").getDescription().getSqlType().equals("timestamp");
        assert !tableMapping.getPropertyByColumn("gmt_create").getDescription().isNullable();
        assert tableMapping.getPropertyByColumn("gmt_create").getDescription().getDefault().equals("current_timestamp");
        assert tableMapping.getPropertyByColumn("gmt_create").getDescription().getComment().equals("创建时间");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getSqlType().equals("timestamp");
        assert !tableMapping.getPropertyByName("gmtModified").getDescription().isNullable();
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getDefault().equals("current_timestamp");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getOther().equals("on update current_timestamp");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getComment().equals("修改时间");
        assert tableMapping.getPropertyByName("gmtModified").getDescription().getSqlType().equals("timestamp");
        assert tableMapping.getPropertyByName("content").getDescription().getLength().equals("32");

        List<IndexDescription> indexes = tableMapping.getIndexes();
        Map<String, IndexDescription> idxMap = indexes.stream().collect(Collectors.toMap(IndexDescription::getName, i -> i));
        assert idxMap.get("idx_a").getColumns().contains("gmt_modified");
        assert idxMap.get("idx_a").getColumns().contains("instanceId");
        assert !idxMap.get("idx_a").isUnique();
        assert idxMap.get("uk_b").getColumns().contains("instanceId");
        assert idxMap.get("uk_b").isUnique();
    }
}