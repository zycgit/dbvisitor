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
package net.hasor.dbvisitor.mapping.def;
import net.hasor.cobble.function.Property;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.*;

/**
 * 一个实体的映射信息
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class TableDef<T> implements TableMapping<T> {
    private       String   catalog;
    private       String   schema;
    private       String   table;
    private final Class<T> entityType;
    private final boolean  autoProperty;
    private final boolean  useDelimited;
    private final boolean  caseInsensitive;
    private final boolean  mapUnderscoreToCamelCase;

    private TableDescription description;

    private final boolean                    mapBased;
    private final List<ColumnMapping>        columnMappings;
    private final Map<String, ColumnMapping> mapByProperty;
    private final Map<String, ColumnMapping> mapByColumn;
    private final List<IndexDescription>     indexList;

    public TableDef(String catalog, String schema, String table, Class<T> entityType, //
            boolean autoProperty, boolean useDelimited, boolean caseInsensitive, boolean mapUnderscoreToCamelCase) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.entityType = entityType;
        this.autoProperty = autoProperty;
        this.useDelimited = useDelimited;
        this.caseInsensitive = caseInsensitive;
        this.mapBased = Map.class.isAssignableFrom(entityType);
        this.columnMappings = new ArrayList<>();
        this.mapByProperty = (caseInsensitive && Map.class.isAssignableFrom(entityType)) ? new LinkedCaseInsensitiveMap<>() : new HashMap<>();
        this.mapByColumn = caseInsensitive ? new LinkedCaseInsensitiveMap<>() : new HashMap<>();
        this.indexList = new ArrayList<>();
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
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

    @Override
    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    public boolean isMapUnderscoreToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    @Override
    public Collection<ColumnMapping> getProperties() {
        return this.columnMappings;
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

    @Override
    public ColumnMapping getPropertyByColumn(String column) {
        if (this.mapByColumn.containsKey(column)) {
            return this.mapByColumn.get(column);
        } else if (this.mapBased) {
            return initOrGetMapMapping(this.mapByColumn, column);
        } else {
            return null;
        }
    }

    @Override
    public ColumnMapping getPropertyByName(String property) {
        if (this.mapByProperty.containsKey(property)) {
            return this.mapByProperty.get(property);
        } else if (this.mapBased) {
            return initOrGetMapMapping(this.mapByProperty, property);
        } else {
            return null;
        }
    }

    @Override
    public TableDescription getDescription() {
        return this.description;
    }

    public void setDescription(TableDescription description) {
        this.description = description;
    }

    @Override
    public List<IndexDescription> getIndexes() {
        return this.indexList;
    }

    public void addIndexDescription(IndexDescription index) {
        this.indexList.add(index);
    }

    private ColumnMapping initOrGetMapMapping(Map<String, ColumnMapping> map, String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }
        synchronized (this) {
            if (map.containsKey(name)) {
                return map.get(name);
            }

            Class<?> javaType = Object.class;
            int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();
            ColumnDef columnDef = new ColumnDef(name, name, jdbcType, javaType, typeHandler, new MapProperty(name), true, true, false);
            this.mapByColumn.put(name, columnDef);
            return columnDef;
        }
    }

    private final class MapProperty implements Property {
        private final String name;

        public MapProperty(String name) {
            this.name = name;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Object get(Object instance) {
            return ((Map) instance).get(this.name);
        }

        @Override
        public void set(Object instance, Object value) {
            ((Map) instance).put(this.name, value);
        }
    }
}