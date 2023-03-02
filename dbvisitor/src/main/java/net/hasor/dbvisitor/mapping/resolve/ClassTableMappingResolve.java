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
package net.hasor.dbvisitor.mapping.resolve;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通过 Class 来解析 TableMapping
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class ClassTableMappingResolve extends AbstractTableMappingResolve<Class<?>> {
    private static final Logger                         logger          = Logger.getLogger(ClassTableMappingResolve.class);
    private static final Map<Class<?>, TableMapping<?>> CACHE_TABLE_MAP = new WeakHashMap<>();

    public ClassTableMappingResolve(MappingOptions options) {
        super(options);
    }

    public static TableMapping<?> resolveTableMapping(Class<?> entityType, TypeHandlerRegistry typeRegistry) {
        if (CACHE_TABLE_MAP.containsKey(entityType)) {
            return CACHE_TABLE_MAP.get(entityType);
        } else {
            TableDefaultInfo tableInfo = fetchDefaultInfoByEntity(entityType.getClassLoader(), entityType, MappingOptions.buildNew(), Collections.emptyMap());
            TableMapping<?> tableMapping = new ClassTableMappingResolve(null).resolveTableAndColumn(tableInfo, entityType, typeRegistry);
            CACHE_TABLE_MAP.put(entityType, tableMapping);
            return tableMapping;
        }
    }

    @Override
    public TableDef<?> resolveTableMapping(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        TableDefaultInfo tableInfo = fetchDefaultInfoByEntity(classLoader, entityType, this.options, Collections.emptyMap());
        return resolveTableAndColumn(tableInfo, entityType, typeRegistry);
    }

    protected TableDef<?> resolveTable(TableDefaultInfo tableInfo, Class<?> entityType) {
        String catalog = tableInfo.catalog();
        String schema = tableInfo.schema();
        String table = StringUtils.isNotBlank(tableInfo.table()) ? tableInfo.table() : StringUtils.isNotBlank(tableInfo.value()) ? tableInfo.value() : "";
        String comment = tableInfo.comment();
        String other = tableInfo.other();

        boolean autoProperty = tableInfo.autoMapping();
        boolean useDelimited = tableInfo.useDelimited();
        boolean caseInsensitive = tableInfo.caseInsensitive();
        boolean camelCase = tableInfo.mapUnderscoreToCamelCase();

        SqlDialect dialect = tableInfo.getSqlDialect();
        TableDef<?> tableDef = new TableDef<>(catalog, schema, table, entityType, autoProperty, useDelimited, caseInsensitive, camelCase, dialect);

        // desc
        if (StringUtils.isBlank(comment) && StringUtils.isBlank(other)) {
            tableDef.setDescription(parseDesc(entityType.getAnnotation(TableDescribe.class)));
        } else {
            tableDef.setDescription(parseDesc(tableInfo));
        }

        // index
        IndexDescribe[] indexDescribes = entityType.getAnnotationsByType(IndexDescribe.class);
        if (indexDescribes.length > 0) {
            for (IndexDescribe idx : indexDescribes) {
                String idxName = idx.name();
                String idxComment = idx.comment();
                String idxOther = idx.other();

                List<String> columns = Arrays.stream(idx.columns()).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(columns)) {
                    throw new IllegalArgumentException("entityType " + entityType + " @IndexDescribe columns is empty.");
                }

                if (StringUtils.isBlank(idxName)) {
                    throw new IllegalArgumentException("entityType " + tableDef.getTable() + " missing index name.");
                }

                IndexDef idxDef = new IndexDef();
                idxDef.setName(idxName);
                idxDef.setColumns(columns);
                idxDef.setUnique(idx.unique());
                idxDef.setComment(StringUtils.isBlank(idxComment) ? null : idxComment);
                idxDef.setOther(StringUtils.isBlank(idxOther) ? null : idxOther);
                tableDef.addIndexDescription(idxDef);
            }
        }

        return tableDef;
    }

    protected TableDef<?> resolveTableAndColumn(TableDefaultInfo tableInfo, Class<?> entityType, TypeHandlerRegistry typeRegistry) {
        TableDef<?> def = this.resolveTable(tableInfo, entityType);
        Map<String, Property> properties = BeanUtils.getPropertyFunc(entityType);

        // keep order by fields
        List<String> names = new ArrayList<>();
        List<String> fields = BeanUtils.getALLFields(entityType).values().stream().map(Field::getName).collect(Collectors.toList());
        for (String name : fields) {
            if (properties.containsKey(name)) {
                names.add(name);
            }
        }
        for (String name : properties.keySet()) {
            if (!names.contains(name)) {
                names.add(name);
            }
        }

        for (String name : names) {
            Property property = properties.get(name);
            Class<?> type = BeanUtils.getPropertyType(property);
            resolveProperty(def, name, type, property, typeRegistry, tableInfo);
        }

        return def;
    }

    protected void resolveProperty(TableDef<?> tableDef, String name, Class<?> type, Property handler, TypeHandlerRegistry typeRegistry, Table tableInfo) {
        Annotation[] annotations = BeanUtils.getPropertyAnnotation(handler);
        Column info = null;
        ColumnDescribe infoDesc = null;
        for (Annotation a : annotations) {
            if (a instanceof Column) {
                info = (Column) a;
            } else if (a instanceof ColumnDescribe) {
                infoDesc = (ColumnDescribe) a;
            } else if (a instanceof Ignore) {
                return;
            }
        }

        if (info != null) {
            String column = StringUtils.isNotBlank(info.name()) ? info.name() : info.value();
            if (StringUtils.isBlank(column)) {
                column = hump2Line(name, tableInfo.mapUnderscoreToCamelCase());
            }

            Class<?> javaType = info.specialJavaType() == Object.class ? CLASS_MAPPING_MAP.getOrDefault(type, type) : info.specialJavaType();

            int jdbcType = info.jdbcType();
            if (info.jdbcType() == Types.JAVA_OBJECT) {
                jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            }

            TypeHandler<?> typeHandler = typeRegistry.getTypeHandler(javaType, jdbcType);
            boolean insert = info.insert();
            boolean update = info.update();
            boolean primary = info.primary();
            String selectTemplate = info.selectTemplate();
            String insertTemplate = info.insertTemplate();
            String setColTemplate = info.setColTemplate();
            String setValueTemplate = info.setValueTemplate();
            String whereColTemplate = info.whereColTemplate();
            String whereValueTemplate = info.whereValueTemplate();
            ColumnDef colDef = new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler, insert, update, primary,//
                    selectTemplate, insertTemplate, setColTemplate, setValueTemplate, whereColTemplate, whereValueTemplate);

            colDef.setDescription(parseDesc(infoDesc));

            // init KeySeqHolder
            colDef.setKeySeqHolder(this.resolveKeyType(tableDef, colDef, info.keyType(), annotations, typeRegistry));
            tableDef.addMapping(colDef);

        } else if (tableDef.isAutoProperty()) {

            String column = hump2Line(name, tableInfo.mapUnderscoreToCamelCase());
            Class<?> javaType = CLASS_MAPPING_MAP.getOrDefault(type, type);
            int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            TypeHandler<?> typeHandler = typeRegistry.getTypeHandler(javaType, jdbcType);
            tableDef.addMapping(new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler, true, true, false));
        }
    }

    private TableDescription parseDesc(TableDescribe tableDesc) {
        if (tableDesc == null) {
            return null;
        }

        TableDescDef descDef = new TableDescDef();
        descDef.setComment(tableDesc.comment());
        descDef.setOther(tableDesc.other());
        return descDef;
    }

    private ColumnDescription parseDesc(ColumnDescribe columnDesc) {
        if (columnDesc == null) {
            return null;
        }

        ColumnDescDef descDef = new ColumnDescDef();
        descDef.setComment(columnDesc.comment());
        descDef.setDbType(columnDesc.dbType());
        descDef.setLength(columnDesc.length());
        descDef.setPrecision(columnDesc.precision());
        descDef.setScale(columnDesc.scale());
        descDef.setDefault(columnDesc.defaultValue());
        descDef.setNullable(columnDesc.nullable());
        descDef.setOther(columnDesc.other());
        return descDef;
    }

    private KeySeqHolder resolveKeyType(TableDef<?> tableDef, ColumnDef colDef, KeyTypeEnum keyTypeEnum, Annotation[] allAnnotations, TypeHandlerRegistry typeRegistry) {
        if (keyTypeEnum == KeyTypeEnum.None) {
            return null;
        }

        Map<String, Object> envConfig = new HashMap<>();
        if (allAnnotations != null) {
            for (Annotation anno : allAnnotations) {
                envConfig.put(anno.annotationType().getName(), anno);
            }
        }

        return keyTypeEnum.createHolder(new CreateContext(this.options, typeRegistry, tableDef, colDef, envConfig));
    }
}
