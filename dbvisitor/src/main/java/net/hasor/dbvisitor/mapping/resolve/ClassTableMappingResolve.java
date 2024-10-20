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
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.UnknownTypeHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通过 Class 来解析 TableMapping
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-21
 */
public class ClassTableMappingResolve extends AbstractTableMappingResolve<Class<?>> {
    private static final Map<Class<?>, TableDef<?>> CACHE_TABLE_MAP = new WeakHashMap<>();

    @Override
    public <V> TableDef<V> resolveTableMapping(Class<?> entityType, MappingOptions usingOpt, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        TableDef<?> def = CACHE_TABLE_MAP.computeIfAbsent(entityType, type -> {
            if (type.isAnnotationPresent(Table.class)) {
                return this.resolveTableInfo(type, usingOpt, typeRegistry);
            } else if (type.isAnnotationPresent(ResultMap.class)) {
                return this.resolveResultInfo(type, usingOpt, typeRegistry);
            } else {
                boolean usingAutoProperty = usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping();
                boolean usingUseDelimited = Boolean.TRUE.equals(usingOpt.getUseDelimited());
                boolean usingMapUnderscoreToCamelCase = Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase());
                boolean usingCaseInsensitive = usingOpt.getCaseInsensitive() == null || usingOpt.getCaseInsensitive();

                TableDef<?> tableDef = new TableDef<>(usingOpt.getCatalog(), usingOpt.getSchema(), null, type, usingOpt.getDefaultDialect(), //
                        usingAutoProperty, usingUseDelimited, usingCaseInsensitive, usingMapUnderscoreToCamelCase);
                this.resolveTableAndColumn(false, tableDef, typeRegistry);

                return tableDef;
            }
        });
        return (TableDef<V>) def;
    }

    private TableDef<?> resolveTableInfo(Class<?> entityType, MappingOptions usingOpt, TypeHandlerRegistry typeRegistry) {
        Map<String, Object> annoInfo;
        try {
            annoInfo = ClassUtils.readAnnotation(entityType, Table.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String catalog = (String) annoInfo.getOrDefault("catalog", usingOpt.getCatalog());
        String schema = (String) annoInfo.getOrDefault("schema", usingOpt.getSchema());
        String table = (String) annoInfo.getOrDefault("table", null);
        if (StringUtils.isBlank(table)) {
            table = (String) annoInfo.getOrDefault("value", null);
        }

        String autoMapping = (String) annoInfo.getOrDefault("autoMapping", null);
        String useDelimited = (String) annoInfo.getOrDefault("useDelimited", null);
        String mapUnderscoreToCamelCase = (String) annoInfo.getOrDefault("mapUnderscoreToCamelCase", null);
        String caseInsensitive = (String) annoInfo.getOrDefault("caseInsensitive", null);
        String ddlAuto = (String) annoInfo.getOrDefault("ddlAuto", null);

        boolean usingAutoProperty = StringUtils.isBlank(autoMapping) ? (usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping()) : Boolean.parseBoolean(autoMapping);
        boolean usingUseDelimited = StringUtils.isBlank(useDelimited) ? Boolean.TRUE.equals(usingOpt.getUseDelimited()) : Boolean.parseBoolean(useDelimited);
        boolean usingMapUnderscoreToCamelCase = StringUtils.isBlank(mapUnderscoreToCamelCase) ? Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase()) : Boolean.parseBoolean(mapUnderscoreToCamelCase);
        boolean usingCaseInsensitive = StringUtils.isBlank(caseInsensitive) ? (usingOpt.getCaseInsensitive() == null || usingOpt.getCaseInsensitive()) : Boolean.parseBoolean(caseInsensitive);
        DdlAuto usingDdlAuto = DdlAuto.valueOfCode(ddlAuto);
        SqlDialect dialect = usingOpt.getDefaultDialect();

        TableDef<?> def = new TableDef<>(catalog, schema, table, entityType, dialect, //
                usingAutoProperty, usingUseDelimited, usingCaseInsensitive, usingMapUnderscoreToCamelCase);

        if (entityType.isAnnotationPresent(TableDescribe.class)) {
            TableDescribe desc = entityType.getAnnotation(TableDescribe.class);
            TableDescDef tableDesc = new TableDescDef();
            tableDesc.setDdlAuto(usingDdlAuto);
            tableDesc.setCharacterSet(desc.characterSet());
            tableDesc.setCollation(desc.collation());
            tableDesc.setComment(desc.comment());
            tableDesc.setOther(desc.other());
            def.setDescription(tableDesc);

            this.loadTableIndex(def);
        }

        this.resolveTableAndColumn(true, def, typeRegistry);
        return def;
    }

    private void loadTableIndex(TableDef<?> tableDef) {
        Class<?> entityType = tableDef.entityType();
        for (IndexDescribe idx : entityType.getAnnotationsByType(IndexDescribe.class)) {
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

    private TableDef<?> resolveResultInfo(Class<?> entityType, MappingOptions usingOpt, TypeHandlerRegistry typeRegistry) {
        Map<String, Object> annoInfo;
        try {
            annoInfo = ClassUtils.readAnnotation(entityType, ResultMap.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String autoMapping = (String) annoInfo.getOrDefault("autoMapping", null);
        String useDelimited = (String) annoInfo.getOrDefault("useDelimited", null);
        String mapUnderscoreToCamelCase = (String) annoInfo.getOrDefault("mapUnderscoreToCamelCase", null);
        String caseInsensitive = (String) annoInfo.getOrDefault("caseInsensitive", null);

        boolean usingAutoProperty = StringUtils.isBlank(autoMapping) ? (usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping()) : Boolean.parseBoolean(autoMapping);
        boolean usingUseDelimited = StringUtils.isBlank(useDelimited) ? Boolean.TRUE.equals(usingOpt.getUseDelimited()) : Boolean.parseBoolean(useDelimited);
        boolean usingMapUnderscoreToCamelCase = StringUtils.isBlank(mapUnderscoreToCamelCase) ? Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase()) : Boolean.parseBoolean(mapUnderscoreToCamelCase);
        boolean usingCaseInsensitive = StringUtils.isBlank(caseInsensitive) ? (usingOpt.getCaseInsensitive() == null || usingOpt.getCaseInsensitive()) : Boolean.parseBoolean(caseInsensitive);
        SqlDialect dialect = usingOpt.getDefaultDialect();

        TableDef<?> def = new TableDef<>("", "", "", entityType, dialect, //
                usingAutoProperty, usingUseDelimited, usingCaseInsensitive, usingMapUnderscoreToCamelCase);

        this.resolveTableAndColumn(false, def, typeRegistry);
        return def;
    }

    public void resolveTableAndColumn(boolean isEntity, TableDef<?> def, TypeHandlerRegistry typeRegistry) {
        Class<?> entityType = def.entityType();
        Map<String, Property> properties = BeanUtils.getPropertyFunc(entityType);

        // keep order by fields
        List<String> names = new ArrayList<>();
        List<String> fields = BeanUtils.getALLFieldToList(entityType).stream().map(Field::getName).collect(Collectors.toList());
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
            this.resolveProperty(isEntity, def, name, type, property, typeRegistry);
        }
    }

    protected void resolveProperty(boolean isEntity, TableDef<?> def, String name, Class<?> type, Property handler, TypeHandlerRegistry typeRegistry) {
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

        ColumnDef colDef;
        if (info != null) {
            String column = StringUtils.isNotBlank(info.name()) ? info.name() : info.value();
            if (StringUtils.isBlank(column)) {
                column = hump2Line(name, def.isMapUnderscoreToCamelCase());
            }

            Class<?> javaType = info.specialJavaType() == Object.class ? CLASS_MAPPING_MAP.getOrDefault(type, type) : info.specialJavaType();

            int jdbcType = info.jdbcType();
            if (info.jdbcType() == Types.JAVA_OBJECT) {
                jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            }

            TypeHandler<?> typeHandler;
            if (info.typeHandler() == UnknownTypeHandler.class) {
                typeHandler = typeRegistry.getTypeHandler(javaType, jdbcType);
            } else {
                typeHandler = typeRegistry.createTypeHandler(info.typeHandler(), javaType);
            }

            colDef = new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler);
        } else if (def.isAutoProperty()) {
            String column = hump2Line(name, def.isMapUnderscoreToCamelCase());
            Class<?> javaType = CLASS_MAPPING_MAP.getOrDefault(type, type);
            int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            TypeHandler<?> typeHandler = typeRegistry.getTypeHandler(javaType);

            colDef = new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler);
        } else {
            return;
        }

        //
        if (isEntity) {
            if (info != null) {
                colDef.setPrimaryKey(info.primary());
                colDef.setInsert(info.insert());
                colDef.setUpdate(info.update());
                colDef.setSelectTemplate(info.selectTemplate());
                colDef.setInsertTemplate(info.insertTemplate());
                colDef.setSetColTemplate(info.setColTemplate());
                colDef.setSetValueTemplate(info.setValueTemplate());
                colDef.setWhereColTemplate(info.whereColTemplate());
                colDef.setWhereValueTemplate(info.whereValueTemplate());
            }

            // for Description
            if (infoDesc != null) {
                ColumnDescDef descDef = new ColumnDescDef();
                descDef.setSqlType(infoDesc.sqlType());
                descDef.setLength(infoDesc.length());
                descDef.setPrecision(infoDesc.precision());
                descDef.setScale(infoDesc.scale());
                descDef.setCharacterSet(infoDesc.characterSet());
                descDef.setCollation(infoDesc.collation());
                descDef.setScale(infoDesc.scale());
                descDef.setNullable(!colDef.isPrimaryKey() && infoDesc.nullable());
                descDef.setDefault(infoDesc.defaultValue());
                descDef.setComment(infoDesc.comment());
                descDef.setOther(infoDesc.other());
                colDef.setDescription(descDef);
            }
        }

        // init KeySeqHolder
        if (info != null) {
            colDef.setKeySeqHolder(this.resolveKeyType(def, colDef, info.keyType(), annotations, typeRegistry));
        }

        def.addMapping(colDef);
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

        return keyTypeEnum.createHolder(new KeySeqHolderContext(typeRegistry, tableDef, colDef, envConfig));
    }
}