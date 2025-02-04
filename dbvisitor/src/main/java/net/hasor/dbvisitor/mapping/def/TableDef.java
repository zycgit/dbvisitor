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
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Primary;

import java.util.*;

/**
 * 一个实体的映射信息
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class TableDef<T> implements TableMapping<T> {
    private       String                           catalog;
    private       String                           schema;
    private       String                           table;
    private final Class<T>                         entityType;
    private       Annotations                      annotations;
    private final boolean                          autoProperty;
    private final boolean                          useDelimited;
    private final boolean                          caseInsensitive;
    private final boolean                          mapUnderscoreToCamelCase;
    //
    private       TableDescription                 description;
    //
    private final boolean                          mapBased;
    private       boolean                          useGeneratedKey;
    private final List<ColumnMapping>              columnMappings;
    private final Map<String, ColumnMapping>       mapByProperty;
    private final Map<String, List<ColumnMapping>> mapByColumn;
    private final Map<String, ColumnMapping>       mapByColumnForPrimary;
    private final List<IndexDescription>           indexList;

    public TableDef(String catalog, String schema, String table, Class<T> entityType,  //
            boolean autoProperty, boolean useDelimited, boolean caseInsensitive, boolean mapUnderscoreToCamelCase) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.entityType = entityType;
        this.autoProperty = autoProperty;
        this.useDelimited = useDelimited;
        this.caseInsensitive = caseInsensitive;
        this.mapBased = Map.class.isAssignableFrom(entityType);
        this.useGeneratedKey = false;
        this.columnMappings = new ArrayList<>();
        this.mapByProperty = (caseInsensitive && Map.class.isAssignableFrom(entityType)) ? new LinkedCaseInsensitiveMap<>() : new LinkedHashMap<>();
        this.mapByColumn = caseInsensitive ? new LinkedCaseInsensitiveMap<>() : new LinkedHashMap<>();
        this.mapByColumnForPrimary = caseInsensitive ? new LinkedCaseInsensitiveMap<>() : new LinkedHashMap<>();
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
    public Annotations getAnnotations() {
        return this.annotations;
    }

    @Override
    public boolean useGeneratedKey() {
        return this.useGeneratedKey;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
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

    @Override
    public boolean isToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    public boolean isMapUnderscoreToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    /** 映射的实体是否是基于 Map */
    public boolean isMapEntity() {
        return this.mapBased;
    }

    @Override
    public Collection<ColumnMapping> getProperties() {
        return this.columnMappings;
    }

    @Override
    public Collection<String> getColumns() {
        return Collections.unmodifiableCollection(this.mapByColumn.keySet());
    }

    public void addMapping(ColumnMapping mapping) {
        if (mapping == null) {
            return;
        }

        String columnName = mapping.getColumn();
        String propertyName = mapping.getProperty();
        if (this.mapByColumn.containsKey(columnName) && this.mapByProperty.containsKey(propertyName)) {
            throw new IllegalStateException("column '" + columnName + "' mapping already added.");
        }

        List<ColumnMapping> allColumnMapping = this.mapByColumn.computeIfAbsent(columnName, s -> new ArrayList<>());
        allColumnMapping.add(mapping);

        ColumnMapping primary = null;
        if (allColumnMapping.size() == 1) {
            primary = allColumnMapping.get(0);
        } else {
            for (ColumnMapping item : allColumnMapping) {
                if (item.getAnnotations().getAnnotation(Primary.class) != null) {
                    if (primary != null) {
                        throw new IllegalStateException("column '" + columnName + "' mapping declares multiple @Primary");
                    } else {
                        primary = item;
                    }
                }
            }
        }

        if (primary == null) {
            this.mapByColumnForPrimary.remove(columnName);
        } else {
            this.mapByColumnForPrimary.put(columnName, mapping);
        }

        this.mapByProperty.put(propertyName, mapping);
        this.columnMappings.add(mapping);

        if (mapping.getKeyTpe() == KeyType.Auto) {
            this.useGeneratedKey = true;
        }
    }

    @Override
    public List<ColumnMapping> getPropertyByColumn(String column) {
        return this.mapByColumn.getOrDefault(column, null);
    }

    @Override
    public ColumnMapping getPrimaryPropertyByColumn(String column) {
        return this.mapByColumnForPrimary.getOrDefault(column, null);
    }

    @Override
    public ColumnMapping getPropertyByName(String property) {
        return this.mapByProperty.getOrDefault(property, null);
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

    @Override
    public IndexDescription getIndex(String name) {
        return this.indexList.stream().filter(idx -> idx.getName().equals(name)).findFirst().orElse(null);
    }

    public void addIndexDescription(IndexDescription index) {
        this.indexList.add(index);
    }
}