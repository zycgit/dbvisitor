/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.dbvisitor.mapping.def;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.*;

/**
 * 一个实体的映射信息
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class TableDef<T> implements TableMapping<T> {
    private       String              catalog;
    private       String              schema;
    private       String              table;
    private final Class<T>            entityType;
    private final boolean             autoProperty;
    private final boolean             useDelimited;
    private final boolean             caseInsensitive;
    private final TypeHandlerRegistry typeHandlerRegistry;

    private final List<ColumnMapping>        columnMappings;
    private final Map<String, ColumnMapping> mapByProperty;
    private final Map<String, ColumnMapping> mapByColumn;

    public TableDef(String catalog, String schema, String table, Class<T> entityType, boolean autoProperty, boolean useDelimited, boolean caseInsensitive, TypeHandlerRegistry typeHandlerRegistry) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.entityType = entityType;
        this.autoProperty = autoProperty;
        this.useDelimited = useDelimited;
        this.caseInsensitive = caseInsensitive;
        this.columnMappings = new ArrayList<>();
        this.mapByProperty = new HashMap<>();
        this.mapByColumn = caseInsensitive ? new LinkedCaseInsensitiveMap<>() : new HashMap<>();
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    @Override
    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getTable() {
        return this.table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public Class<T> entityType() {
        return this.entityType;
    }

    @Override
    public boolean isAutoProperty() {
        return this.autoProperty;
    }

    @Override
    public boolean useDelimited() {
        return this.useDelimited;
    }

    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    @Override
    public Collection<ColumnMapping> getProperties() {
        return this.columnMappings;
    }

    @Override
    public ColumnMapping getPropertyByColumn(String column) {
        return this.mapByColumn.get(column);
    }

    @Override
    public ColumnMapping getPropertyByName(String property) {
        return this.mapByProperty.get(property);
    }

    @Override
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return this.typeHandlerRegistry;
    }

    public void addMapping(ColumnMapping mapping) {
        String columnName = mapping.getColumn();
        String propertyName = mapping.getProperty();
        if (this.mapByColumn.containsKey(columnName) && this.mapByProperty.containsKey(propertyName)) {
            throw new IllegalStateException("mapping already added.");
        }
        this.mapByColumn.put(columnName, mapping);
        this.mapByProperty.put(propertyName, mapping);
        this.columnMappings.add(mapping);
    }
}