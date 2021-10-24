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
package net.hasor.db.mapping.resolve;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.db.mapping.Column;
import net.hasor.db.mapping.Ignore;
import net.hasor.db.mapping.Table;
import net.hasor.db.mapping.def.ColumnDef;
import net.hasor.db.mapping.def.TableDef;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.lang.annotation.Annotation;
import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过 Class 来解析 TableMapping
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class ClassResolveTableMapping implements ResolveTableMapping<Class<?>> {
    private static final Map<Class<?>, Class<?>> CLASS_MAPPING_MAP = new HashMap<>();

    static {
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, HashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, HashMap.class);
    }

    public static <V> TableMapping<V> resolveTableMapping(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        return new ClassResolveTableMapping().resolveTableMapping(entityType, classLoader, typeRegistry, null);
    }

    @Override
    public <V> TableMapping<V> resolveTableMapping(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry, MappingOptions options) {
        options = new MappingOptions(options);

        TableDef<V> def = this.resolveTable((Class<V>) entityType, options);

        Map<String, Property> properties = BeanUtils.getPropertyFunc(entityType);
        for (String name : properties.keySet()) {
            Property property = properties.get(name);
            Class<?> type = BeanUtils.getPropertyType(property);
            resolveProperty(def, name, type, property, typeRegistry, options);
        }

        return def;
    }

    protected <V> TableDef<V> resolveTable(Class<V> entityType, MappingOptions options) {
        if (entityType.isAnnotationPresent(Table.class)) {
            Table defTable = entityType.getAnnotation(Table.class);
            String schema = defTable.schema();
            String table = StringUtils.isNotBlank(defTable.name()) ? defTable.name() : defTable.value();

            if (defTable.mapUnderscoreToCamelCase() || Boolean.TRUE.equals(options.getMapUnderscoreToCamelCase())) {
                schema = humpToLine(schema, true);
                table = humpToLine(table, true);
                options.setMapUnderscoreToCamelCase(true); // for parserProperty
            }

            boolean autoProperty = defTable.autoMapping();
            boolean useDelimited = defTable.useDelimited();
            boolean caseSensitivity = defTable.caseSensitivity() || Boolean.TRUE.equals(options.getCaseSensitivity());
            return new TableDef<>(schema, table, entityType, autoProperty, useDelimited, caseSensitivity);
        } else {

            String tableName = humpToLine(entityType.getSimpleName(), options.getMapUnderscoreToCamelCase());
            return new TableDef<>(null, tableName, entityType, true, false, false);
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
                column = humpToLine(name, options.getMapUnderscoreToCamelCase());
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

            column = humpToLine(name, options.getMapUnderscoreToCamelCase());
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

    private static final Pattern humpPattern = Pattern.compile("[A-Z]");

    private static String humpToLine(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        }
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        //
        String strString = sb.toString();
        strString = strString.replaceAll("_{2,}", "_");
        if (strString.charAt(0) == '_') {
            strString = strString.substring(1);
        }
        return strString;
    }
}
