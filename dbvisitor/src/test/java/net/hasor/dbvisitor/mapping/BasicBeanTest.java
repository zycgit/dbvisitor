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
import net.hasor.dbvisitor.mapping.dto.AnnoOnlyTableInfoBean;
import org.junit.Test;

import java.util.Date;

public class BasicBeanTest {
    @Test
    public void annoTableInfo_1() {
        TableMapping<AnnoOnlyTableInfoBean> tab = MappingRegistry.DEFAULT.loadEntity(AnnoOnlyTableInfoBean.class);

        assert tab.getCatalog().equals("master");
        assert tab.getSchema().equals("dbo");
        assert tab.getTable().equals("blob_resource");
        assert tab.entityType() == AnnoOnlyTableInfoBean.class;
        assert tab.isAutoProperty();
        assert !tab.useDelimited();
        assert tab.isCaseInsensitive();
        assert !tab.isToCamelCase();
        assert tab.getDialect() == null;
        assert !tab.isMapEntity();

        assert tab.getDescription() != null;
        assert tab.getDescription().getDdlAuto() == DdlAuto.None;
        assert tab.getDescription().getCharacterSet().equals("");
        assert tab.getDescription().getCollation().equals("");
        assert tab.getDescription().getComment().equals("test table");
        assert tab.getDescription().getOther().equals("");

        assert tab.getPropertyByName("id").isPrimaryKey();
        assert tab.getPropertyByName("id").isInsert();
        assert tab.getPropertyByName("id").isUpdate();
        assert tab.getPropertyByName("id").getColumn().equals("id");
        assert tab.getPropertyByName("id").getJavaType() == Long.class;
        assert tab.getPropertyByName("id").getKeySeqHolder().toString().startsWith("Auto@");
        assert tab.getPropertyByName("id").getDescription().getSqlType().equals("bigint");
        assert !tab.getPropertyByName("id").getDescription().isNullable();
        assert tab.getPropertyByName("id").getDescription().getDefault() == null;

        assert tab.getPropertyByName("gmtCreate").isPrimaryKey();
        assert tab.getPropertyByName("gmtCreate").isInsert();
        assert !tab.getPropertyByName("gmtCreate").isUpdate();
        assert tab.getPropertyByName("gmtCreate").getColumn().equals("gmt_create");
        assert tab.getPropertyByName("gmtCreate").getJavaType() == Date.class;
        assert tab.getPropertyByName("gmtCreate").getKeySeqHolder() == null;
        assert tab.getPropertyByName("gmtCreate").getDescription().getSqlType().equals("datetime");
        assert !tab.getPropertyByName("gmtCreate").getDescription().isNullable();
        assert tab.getPropertyByName("gmtCreate").getDescription().getDefault().equals("CURRENT_TIMESTAMP");

        //
        //    @Column(name = "gmt_modified")
        //    @ColumnDescribe(sqlType = "datetime", nullable = false, defaultValue = "CURRENT_TIMESTAMP")
        //    private Date gmtModified;
        //
        //    @Column(name = "instanceId")
        //    @ColumnDescribe(sqlType = "varchar(64)")
        //    private String instanceId;
        //
        //    @Column(name = "owner_name")
        //    @ColumnDescribe(sqlType = "varchar(255)")
        //    private String ownerName;
        //
        //    @Column(name = "owner_type")
        //    @ColumnDescribe(sqlType = "varchar(64)")
        //    private ResourceType ownerType;
        //
        //    @Column(name = "content")
        //    @ColumnDescribe(sqlType = "blob")
        //    private byte[] content;

        //    //
        //    private final List<ColumnMapping>        columnMappings;
        //    private final Map<String, ColumnMapping> mapByProperty;
        //    private final Map<String, ColumnMapping> mapByColumn;
        //    private final List<IndexDescription>     indexList;
    }
}
