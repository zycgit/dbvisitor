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
package net.hasor.dbvisitor.mapping.resolve;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Ignore;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.TableDef;
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
public class ClassTableMappingResolve implements TableMappingResolve<Class<?>> {
    private static final Map<Class<?>, Class<?>> CLASS_MAPPING_MAP = new HashMap<>();

    static {
        CLASS_MAPPING_MAP.put(Collection.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, LinkedHashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, LinkedHashMap.class);
    }

    public static TableDef<?> resolveTableMapping(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        return new ClassTableMappingResolve().resolveTableMapping(entityType, classLoader, typeRegistry, null);
    }

    @Override
    public TableDef<?> resolveTableMapping(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry, MappingOptions options) {
        options = new MappingOptions(options);

        TableDef<?> def = this.resolveTable(entityType, options, typeRegistry);
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
            resolveProperty(def, name, type, property, typeRegistry, options);
        }

        return def;
    }

    protected TableDef<?> resolveTable(Class<?> entityType, MappingOptions options, TypeHandlerRegistry typeRegistry) {
        if (entityType.isAnnotationPresent(Table.class)) {
            Table defTable = entityType.getAnnotation(Table.class);
            String schema = defTable.schema();
            String table = StringUtils.isNotBlank(defTable.name()) ? defTable.name() : StringUtils.isNotBlank(defTable.value()) ? defTable.value() : entityType.getSimpleName();

            if (defTable.mapUnderscoreToCamelCase() || Boolean.TRUE.equals(options.getMapUnderscoreToCamelCase())) {
                schema = hump2Line(schema, true);
                table = hump2Line(table, true);
                options.setMapUnderscoreToCamelCase(true); // for parserProperty
            }

            boolean autoProperty = defTable.autoMapping();
            boolean useDelimited = defTable.useDelimited();
            boolean caseInsensitive = defTable.caseInsensitive() || options.getCaseInsensitive() == null || Boolean.TRUE.equals(options.getCaseInsensitive());
            return new TableDef<>(schema, table, entityType, autoProperty, useDelimited, caseInsensitive, typeRegistry);
        } else {

            String tableName = hump2Line(entityType.getSimpleName(), options.getMapUnderscoreToCamelCase());
            return new TableDef<>(null, tableName, entityType, true, false, true, typeRegistry);
        }
    }

    private void resolveProperty(TableDef<?> tableDef, String name, Class<?> type, Property handler, TypeHandlerRegistry typeRegistry, MappingOptions options) {
        Annotation[] annotations = BeanUtils.getPropertyAnnotation(handler);
        Column info = null;
        for (Annotation a : annotations) {
            if (info == null && a instanceof Column) {
                info = (Column) a;
            } else if (a instanceof Ignore) {
                return;
            }
        }

        String column;
        Class<?> javaType;
        int jdbcType;
        TypeHandler<?> typeHandler;
        boolean insert;
        boolean update;
        boolean primary;

        if (info != null) {
            column = StringUtils.isNotBlank(info.name()) ? info.name() : info.value();
            if (StringUtils.isBlank(column)) {
                column = hump2Line(name, options.getMapUnderscoreToCamelCase());
            }

            javaType = info.specialJavaType() == Object.class ? CLASS_MAPPING_MAP.getOrDefault(type, type) : info.specialJavaType();

            jdbcType = info.jdbcType();
            if (info.jdbcType() == Types.JAVA_OBJECT) {
                jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            }

            typeHandler = typeRegistry.getTypeHandler(javaType, jdbcType);
            insert = info.insert();
            update = info.update();
            primary = info.primary();

        } else if (tableDef.isAutoProperty()) {

            column = hump2Line(name, options.getMapUnderscoreToCamelCase());
            javaType = CLASS_MAPPING_MAP.getOrDefault(type, type);
            jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            typeHandler = typeRegistry.getTypeHandler(javaType, jdbcType);
            insert = true;
            update = true;
            primary = false;
        } else {
            return;
        }

        tableDef.addMapping(new ColumnDef(column, name, jdbcType, javaType, typeHandler, handler, insert, update, primary));
    }

    private String hump2Line(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        } else {
            return StringUtils.humpToLine(str);
        }
    }
}
