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
import net.hasor.dbvisitor.mapping.dto.MixTableInfoBean1;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.handler.LongTypeHandler;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsDateTypeHandler;
import org.junit.Test;

import java.util.Date;

public class MixTableTest {

    @Test
    public void annoTableInfo_5() {
        MappingRegistry registry = new MappingRegistry();
        TableMapping<?> tab = registry.loadEntityToSpace(MixTableInfoBean1.class);

        assert tab.getCatalog().equals("master");
        assert tab.getSchema().equals("dbo");
        assert tab.getTable().equals("blob_resource");
        assert tab.entityType() == MixTableInfoBean1.class;
        assert tab.isAutoProperty();
        assert !tab.useDelimited();
        assert tab.isCaseInsensitive();
        assert !tab.isToCamelCase();
        assert tab.getDialect() == null;
        assert !tab.isMapEntity();

        assert tab.getDescription() != null;
        assert tab.getDescription().getDdlAuto() == DdlAuto.CreateDrop;
        assert tab.getDescription().getCharacterSet() == null;
        assert tab.getDescription().getCollation() == null;
        assert tab.getDescription().getComment().equals("test table");
        assert tab.getDescription().getOther() == null;

        assert tab.getPropertyByColumn("id").isPrimaryKey();
        assert tab.getPropertyByColumn("id").isInsert();
        assert tab.getPropertyByColumn("id").isUpdate();
        assert tab.getPropertyByColumn("id").getColumn().equals("id");
        assert tab.getPropertyByColumn("id").getJavaType() == Long.class;
        assert tab.getPropertyByColumn("id").getTypeHandler() instanceof LongTypeHandler;
        assert tab.getPropertyByColumn("id").getKeySeqHolder().toString().startsWith("Auto@");
        assert tab.getPropertyByColumn("id").getDescription().getSqlType().equals("bigint");
        assert !tab.getPropertyByColumn("id").getDescription().isNullable();
        assert tab.getPropertyByColumn("id").getDescription().getDefault() == null;

        assert !tab.getPropertyByColumn("gmtCreate").isPrimaryKey();
        assert tab.getPropertyByColumn("gmtCreate").isInsert();
        assert tab.getPropertyByColumn("gmtCreate").isUpdate();
        assert tab.getPropertyByColumn("gmtCreate").getColumn().equals("gmtCreate");
        assert tab.getPropertyByColumn("gmtCreate").getJavaType() == Date.class;
        assert tab.getPropertyByColumn("gmtCreate").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByColumn("gmtCreate").getKeySeqHolder() == null;
        assert tab.getPropertyByColumn("gmtCreate").getDescription() == null;
    }

    @Test
    public void annoTableInfo_6() {
        MappingRegistry registry = new MappingRegistry(null, TypeHandlerRegistry.DEFAULT, MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
        TableMapping<?> tab = registry.loadEntityToSpace(MixTableInfoBean1.class);

        assert tab.getCatalog().equals("master");
        assert tab.getSchema().equals("dbo");
        assert tab.getTable().equals("blob_resource");
        assert tab.entityType() == MixTableInfoBean1.class;
        assert tab.isAutoProperty();
        assert !tab.useDelimited();
        assert tab.isCaseInsensitive();
        assert tab.isToCamelCase();
        assert tab.getDialect() == null;
        assert !tab.isMapEntity();

        assert tab.getDescription() != null;
        assert tab.getDescription().getDdlAuto() == DdlAuto.CreateDrop;
        assert tab.getDescription().getCharacterSet() == null;
        assert tab.getDescription().getCollation() == null;
        assert tab.getDescription().getComment().equals("test table");
        assert tab.getDescription().getOther() == null;

        assert tab.getPropertyByColumn("id").isPrimaryKey();
        assert tab.getPropertyByColumn("id").isInsert();
        assert tab.getPropertyByColumn("id").isUpdate();
        assert tab.getPropertyByColumn("id").getColumn().equals("id");
        assert tab.getPropertyByColumn("id").getJavaType() == Long.class;
        assert tab.getPropertyByColumn("id").getTypeHandler() instanceof LongTypeHandler;
        assert tab.getPropertyByColumn("id").getKeySeqHolder().toString().startsWith("Auto@");
        assert tab.getPropertyByColumn("id").getDescription().getSqlType().equals("bigint");
        assert !tab.getPropertyByColumn("id").getDescription().isNullable();
        assert tab.getPropertyByColumn("id").getDescription().getDefault() == null;

        assert !tab.getPropertyByColumn("gmt_create").isPrimaryKey();
        assert tab.getPropertyByColumn("gmt_create").isInsert();
        assert tab.getPropertyByColumn("gmt_create").isUpdate();
        assert tab.getPropertyByColumn("gmt_create").getColumn().equals("gmt_create");
        assert tab.getPropertyByColumn("gmt_create").getJavaType() == Date.class;
        assert tab.getPropertyByColumn("gmt_create").getTypeHandler() instanceof SqlTimestampAsDateTypeHandler;
        assert tab.getPropertyByColumn("gmt_create").getKeySeqHolder() == null;
        assert tab.getPropertyByColumn("gmt_create").getDescription() == null;
    }
}

