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
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * KeySeqHolderFactory 的调用参数
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-12-01
 */
public class GeneratedKeyHandlerContext {
    private final MappingRegistry registry;
    private final TableMapping<?> tableDef;
    private final ColumnMapping   colDef;
    private final Annotations     annotations;

    public GeneratedKeyHandlerContext(MappingRegistry registry, TableMapping<?> tableDef, ColumnMapping colDef, Annotations annotations) {
        this.registry = registry;
        this.tableDef = tableDef;
        this.colDef = colDef;
        this.annotations = annotations;
    }

    public MappingRegistry getRegistry() {
        return this.registry;
    }

    public String getCatalog() {
        return this.tableDef.getCatalog();
    }

    public String getSchema() {
        return this.tableDef.getSchema();
    }

    public String getTable() {
        return this.tableDef.getTable();
    }

    public String getColumn() {
        return this.colDef.getColumn();
    }

    public Class<?> getEntityType() {
        return this.tableDef.entityType();
    }

    public String getProperty() {
        return this.colDef.getProperty();
    }

    public TypeHandler<?> getTypeHandler() {
        return this.colDef.getTypeHandler();
    }

    public Integer getJdbcType() {
        return this.colDef.getJdbcType();
    }

    public Class<?> getJavaType() {
        return this.colDef.getJavaType();
    }

    public boolean useDelimited() {
        return this.tableDef.useDelimited();
    }

    public boolean isCaseInsensitive() {
        return this.tableDef.isCaseInsensitive();
    }

    public boolean isToCamelCase() {
        return this.tableDef.isToCamelCase();
    }

    public SqlDialect getSqlDialect() {
        return this.registry.getGlobalOptions().getDialect();
    }

    public Annotations getAnnotations() {
        return this.annotations;
    }
}
