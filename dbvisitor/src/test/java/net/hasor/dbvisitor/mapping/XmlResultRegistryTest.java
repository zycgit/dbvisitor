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
import net.hasor.dbvisitor.keyholder.sequence.AutoKeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsDateTypeHandler;
import net.hasor.dbvisitor.types.handler.EnumTypeHandler;
import net.hasor.dbvisitor.types.handler.LongTypeHandler;
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
public class XmlResultRegistryTest {

    @Test
    public void loadResultTest_1() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_1");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new AutoKeySeqHolderFactory().createHolder(null).getClass();
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
    public void loadResultTest_2() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_2");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new AutoKeySeqHolderFactory().createHolder(null).getClass();
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
    public void loadResultTest_3() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_3");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new AutoKeySeqHolderFactory().createHolder(null).getClass();
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
    public void loadResultTest_4() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_4");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
    public void loadResultTest_5() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_5");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
    public void loadResultTest_6() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_6");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new AutoKeySeqHolderFactory().createHolder(null).getClass();
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
    public void loadResultTest_7() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_7");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new AutoKeySeqHolderFactory().createHolder(null).getClass();
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
    public void loadResultTest_8() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_8");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        assert mapping.getPropertyByName("id").getKeySeqHolder().getClass() == new AutoKeySeqHolderFactory().createHolder(null).getClass();
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
    public void loadResultTest_9() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_9");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
    public void loadResultTest_10() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_1.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_10");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_1");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
    public void mapper2Test_2() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_2.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_2");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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
    public void mapper2Test_5() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapper("dbvisitor_coverage/dal_mapping/mapper_2.xml");
        TableMapping<?> mapping = registry.findMapping("resultMap_test", "resultMap_5");

        assert mapping.getCatalog().equals("");
        assert mapping.getSchema().equals("");
        assert mapping.getTable().equals("");
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