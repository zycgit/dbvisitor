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
import net.hasor.cobble.NumberUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.cobble.reflect.Annotation;
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * 通过 Class 来解析 TableMapping
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-21
 */
public class ClassTableMappingResolve extends AbstractTableMappingResolve<Class<?>> {
    private final Map<Class<?>, TableDef<?>> CACHE_TABLE_MAP = new WeakHashMap<>();

    @Override
    public <V> TableDef<V> resolveTableMapping(Class<?> entityType, MappingOptions usingOpt, MappingRegistry registry) throws ReflectiveOperationException, IOException {
        if (CACHE_TABLE_MAP.containsKey(entityType)) {
            return (TableDef<V>) CACHE_TABLE_MAP.get(entityType);
        } else {
            synchronized (CACHE_TABLE_MAP) {
                if (CACHE_TABLE_MAP.containsKey(entityType)) {
                    return (TableDef<V>) CACHE_TABLE_MAP.get(entityType);
                }

                TableDef<?> tableDef;
                Annotations classAnno = Annotations.ofClass(entityType);
                if (classAnno.getAnnotation(Table.class) != null) {
                    tableDef = this.resolveTableInfo(classAnno, entityType, usingOpt, registry);
                } else if (classAnno.getAnnotation(ResultMap.class) != null) {
                    tableDef = this.resolveResultInfo(classAnno, entityType, usingOpt, registry);
                } else {
                    boolean usingAutoProperty = usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping();
                    boolean usingUseDelimited = Boolean.TRUE.equals(usingOpt.getUseDelimited());
                    boolean usingMapUnderscoreToCamelCase = Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase());
                    boolean usingCaseInsensitive = MappingHelper.caseInsensitive(usingOpt);

                    tableDef = new TableDef<>(usingOpt.getCatalog(), usingOpt.getSchema(), null, entityType,  //
                            usingAutoProperty, usingUseDelimited, usingCaseInsensitive, usingMapUnderscoreToCamelCase);
                    tableDef.setAnnotations(classAnno);

                    this.resolveTableAndColumn(false, classAnno, tableDef, registry);
                }
                CACHE_TABLE_MAP.put(entityType, tableDef);
                return (TableDef<V>) tableDef;
            }
        }
    }

    private TableDef<?> resolveTableInfo(Annotations classAnno, Class<?> entityType, MappingOptions usingOpt, MappingRegistry registry) throws ReflectiveOperationException {
        Annotation tableInfo = classAnno.getAnnotation(Table.class);
        String catalog = tableInfo.getString("catalog", usingOpt.getCatalog());
        String schema = tableInfo.getString("schema", usingOpt.getSchema());
        String table = tableInfo.getString("table", null);
        if (StringUtils.isBlank(table)) {
            table = tableInfo.getString("value", null);
        }

        boolean autoMapping = tableInfo.getBoolean("autoMapping", (usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping()));
        boolean useDelimited = tableInfo.getBoolean("useDelimited", Boolean.TRUE.equals(usingOpt.getUseDelimited()));
        boolean mapUnderscoreToCamelCase = tableInfo.getBoolean("mapUnderscoreToCamelCase", Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase()));
        boolean caseInsensitive = tableInfo.getBoolean("caseInsensitive", MappingHelper.caseInsensitive(usingOpt));
        DdlAuto ddlAuto = (DdlAuto) tableInfo.getEnum("ddlAuto", DdlAuto.values(), DdlAuto.None, DdlAuto::valueOfCode);

        TableDef<?> def = new TableDef<>(catalog, schema, table, entityType,  //
                autoMapping, useDelimited, caseInsensitive, mapUnderscoreToCamelCase);
        def.setAnnotations(classAnno);

        Annotation tableDesc = classAnno.getAnnotation(TableDescribe.class);
        if (tableDesc != null) {
            TableDescDef descDef = new TableDescDef();
            descDef.setDdlAuto(ddlAuto);
            descDef.setCharacterSet(tableDesc.getString("characterSet", null));
            descDef.setCollation(tableDesc.getString("collation", null));
            descDef.setComment(tableDesc.getString("comment", null));
            descDef.setOther(tableDesc.getString("other", null));
            def.setDescription(descDef);

            this.loadTableIndex(classAnno, def);
        }

        this.resolveTableAndColumn(true, classAnno, def, registry);
        return def;
    }

    public void loadTableIndex(Annotations classAnno, TableDef<?> tableDef) {
        Class<?> entityType = tableDef.entityType();
        Annotation indexSet = classAnno.getAnnotation(IndexDescribeSet.class);
        if (indexSet == null) {
            return;
        }
        for (Annotation idx : indexSet.getAnnotationArray("value")) {
            String idxName = idx.getString("name", null);
            if (StringUtils.isBlank(idxName)) {
                throw new IllegalArgumentException("entityType " + tableDef.getTable() + " missing index name.");
            }

            List<String> colList = idx.getStringArray("columns");
            long colCnt = colList.stream().filter(StringUtils::isNotBlank).count();
            if (colList.size() != colCnt) {
                throw new IllegalArgumentException("entityType " + entityType + " @IndexDescribe columns has empty.");
            }

            IndexDef idxDef = new IndexDef();
            idxDef.setName(idxName);
            idxDef.setColumns(colList);
            idxDef.setUnique(idx.getBoolean("unique", false));
            idxDef.setComment(idx.getString("comment", null));
            idxDef.setOther(idx.getString("other", null));
            tableDef.addIndexDescription(idxDef);
        }
    }

    private TableDef<?> resolveResultInfo(Annotations classAnno, Class<?> entityType, MappingOptions usingOpt, MappingRegistry registry) throws ReflectiveOperationException {
        Annotation resultInfo = classAnno.getAnnotation(ResultMap.class);

        boolean autoMapping = resultInfo.getBoolean("autoMapping", (usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping()));
        boolean useDelimited = resultInfo.getBoolean("useDelimited", Boolean.TRUE.equals(usingOpt.getUseDelimited()));
        boolean mapUnderscoreToCamelCase = resultInfo.getBoolean("mapUnderscoreToCamelCase", Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase()));
        boolean caseInsensitive = resultInfo.getBoolean("caseInsensitive", MappingHelper.caseInsensitive(usingOpt));

        TableDef<?> def = new TableDef<>("", "", "", entityType,  //
                autoMapping, useDelimited, caseInsensitive, mapUnderscoreToCamelCase);
        def.setAnnotations(classAnno);

        this.resolveTableAndColumn(false, classAnno, def, registry);
        return def;
    }

    public void resolveTableAndColumn(boolean isEntity, Annotations classAnno, TableDef<?> def, MappingRegistry registry) throws ReflectiveOperationException {
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
            this.resolveProperty(isEntity, classAnno, def, name, type, property, registry);
        }
    }

    protected void resolveProperty(boolean isEntity, Annotations classAnno, TableDef<?> def, String name, Class<?> type, Property handler, MappingRegistry registry) throws ReflectiveOperationException {
        Annotations propertyAnno = Annotations.merge(                                          //
                classAnno.getField(name),                                                      //
                classAnno.getMethod("is" + StringUtils.firstCharToUpperCase(name)), //
                classAnno.getMethod("get" + StringUtils.firstCharToUpperCase(name)),//
                classAnno.getMethod("set" + StringUtils.firstCharToUpperCase(name)) //
        );

        if (propertyAnno.getAnnotation(Ignore.class) != null) {
            return;
        }

        Annotation info = propertyAnno.getAnnotation(Column.class);
        Annotation desc = propertyAnno.getAnnotation(ColumnDescribe.class);

        ColumnDef colDef;
        if (info != null) {
            String column = info.getString("name");
            if (StringUtils.isBlank(column)) {
                column = info.getString("value");
            }
            if (StringUtils.isBlank(column)) {
                column = hump2Line(name, def.isMapUnderscoreToCamelCase());
            }

            TypeHandlerRegistry typeRegistry = registry.getTypeRegistry();
            ClassLoader classLoader = registry.getClassLoader();
            Class<?> javaType = info.getClass("specialJavaType", classLoader, false);
            if (javaType == null) {
                javaType = CLASS_MAPPING_MAP.getOrDefault(type, type);
            } else {
                javaType = CLASS_MAPPING_MAP.getOrDefault(javaType, javaType);
            }

            String jdbcTypeStr = info.getString("jdbcType", null);
            Integer jdbcType = null;
            if (StringUtils.isNumeric(jdbcTypeStr)) {
                jdbcType = NumberUtils.createNumber(jdbcTypeStr).intValue();
            }

            Class<?> typeHandlerType = info.getClass("typeHandler", classLoader);
            TypeHandler<?> typeHandler;
            if (typeHandlerType == null) {
                if (javaType != null && jdbcType != null && typeRegistry.hasTypeHandler(javaType, jdbcType)) {
                    typeHandler = typeRegistry.getTypeHandler(javaType, jdbcType);
                } else if (javaType != null && typeRegistry.hasTypeHandler(javaType)) {
                    typeHandler = typeRegistry.getTypeHandler(javaType);
                } else if (jdbcType != null && typeRegistry.hasTypeHandler(jdbcType)) {
                    typeHandler = typeRegistry.getTypeHandler(jdbcType);
                } else {
                    typeHandler = typeRegistry.getDefaultTypeHandler();
                }
            } else {
                typeHandler = typeRegistry.createTypeHandler(typeHandlerType, javaType);
            }

            colDef = new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler);
            colDef.setAnnotations(propertyAnno);
        } else if (def.isAutoProperty()) {
            String column = hump2Line(name, def.isMapUnderscoreToCamelCase());
            Class<?> javaType = CLASS_MAPPING_MAP.getOrDefault(type, type);
            int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            TypeHandler<?> typeHandler = registry.getTypeRegistry().getTypeHandler(javaType);

            colDef = new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler);
            colDef.setAnnotations(propertyAnno);
        } else {
            return;
        }

        //
        if (isEntity) {
            if (info != null) {
                colDef.setPrimaryKey(info.getBoolean("primary", false));
                colDef.setInsert(info.getBoolean("insert", true));
                colDef.setUpdate(info.getBoolean("update", true));
                colDef.setSelectTemplate(info.getString("selectTemplate"));
                colDef.setInsertTemplate(info.getString("insertTemplate"));
                colDef.setSetColTemplate(info.getString("setColTemplate"));
                colDef.setSetValueTemplate(info.getString("setValueTemplate"));
                colDef.setWhereColTemplate(info.getString("whereColTemplate"));
                colDef.setWhereValueTemplate(info.getString("whereValueTemplate"));
                colDef.setGroupByColTemplate(info.getString("groupByColTemplate"));
                colDef.setOrderByColTemplate(info.getString("orderByColTemplate"));
            }

            // for Description
            if (desc != null) {
                ColumnDescDef descDef = new ColumnDescDef();
                descDef.setSqlType(desc.getString("sqlType", null));
                descDef.setLength(desc.getString("length", null));
                descDef.setPrecision(desc.getString("precision", null));
                descDef.setScale(desc.getString("scale", null));
                descDef.setCharacterSet(desc.getString("characterSet", null));
                descDef.setCollation(desc.getString("collation", null));
                descDef.setScale(desc.getString("scale", null));
                descDef.setNullable(!colDef.isPrimaryKey() && desc.getBoolean("nullable", true));
                descDef.setDefault(desc.getString("defaultValue", null));
                descDef.setComment(desc.getString("comment", null));
                descDef.setOther(desc.getString("other", null));
                colDef.setDescription(descDef);
            }
        }

        // init KeySeqHolder
        if (info != null) {
            String keyTypeStr = info.getString("keyType");
            KeyType keyType = KeyType.valueOfCode(keyTypeStr);
            if (StringUtils.isNotBlank(keyTypeStr) && keyType == null) {
                throw new UnsupportedOperationException("keyType '" + keyTypeStr + "' Unsupported.");
            }

            if (keyType != null) {
                GeneratedKeyHandlerContext holderCtx = new GeneratedKeyHandlerContext(registry, def, colDef, propertyAnno);
                colDef.setKeyTpe(keyType);
                colDef.setKeySeqHolder(keyType.createHolder(holderCtx));
            }
        }

        def.addMapping(colDef);
    }
}