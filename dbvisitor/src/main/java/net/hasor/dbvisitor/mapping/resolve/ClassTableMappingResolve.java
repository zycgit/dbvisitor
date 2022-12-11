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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.asm.AnnotationVisitor;
import net.hasor.cobble.asm.ClassReader;
import net.hasor.cobble.asm.ClassVisitor;
import net.hasor.cobble.asm.Opcodes;
import net.hasor.cobble.dynamic.AsmTools;
import net.hasor.cobble.function.Property;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.io.InputStream;
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
    private static final Logger                  logger            = Logger.getLogger(ClassTableMappingResolve.class);
    private static final Map<Class<?>, Class<?>> CLASS_MAPPING_MAP = new HashMap<>();
    private final        MappingOptions          options;

    static {
        CLASS_MAPPING_MAP.put(Collection.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, LinkedHashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, LinkedHashMap.class);
    }

    public ClassTableMappingResolve() {
        this(MappingOptions.buildNew());
    }

    public ClassTableMappingResolve(MappingOptions options) {
        this.options = options;
    }

    public static TableDef<?> resolveTableDef(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        TableDefaultInfo tableInfo = fetchTableInfo(classLoader, entityType, MappingOptions.buildNew());

        SqlDialect dialect = tableInfo.sqlDialect();
        if (dialect == null && StringUtils.isNotBlank(tableInfo.dialect())) {
            dialect = SqlDialectRegister.findOrCreate(tableInfo.dialect(), classLoader);
        } else if (dialect == null) {
            dialect = DefaultSqlDialect.DEFAULT;
        }

        MappingOptions options = MappingOptions.buildNew();
        options.setDefaultDialect(dialect);
        options.setAutoMapping(tableInfo.autoMapping());
        options.setMapUnderscoreToCamelCase(tableInfo.mapUnderscoreToCamelCase());
        options.setCaseInsensitive(tableInfo.caseInsensitive());

        return new ClassTableMappingResolve(options).resolveTableMapping(tableInfo, entityType, classLoader, typeRegistry);
    }

    @Override
    public TableDef<?> resolveTableMapping(Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        TableDefaultInfo tableInfo = fetchTableInfo(classLoader, entityType, this.options);
        return resolveTableMapping(tableInfo, entityType, classLoader, typeRegistry);
    }

    private TableDef<?> resolveTableMapping(Table tableInfo, Class<?> entityType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) {
        TableDef<?> def = this.resolveTable(tableInfo, entityType, typeRegistry);
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

    protected TableDef<?> resolveTable(Table tableInfo, Class<?> entityType, TypeHandlerRegistry typeRegistry) {
        String catalog = tableInfo.catalog();
        String schema = tableInfo.schema();
        String table = StringUtils.isNotBlank(tableInfo.name()) ? tableInfo.name() : StringUtils.isNotBlank(tableInfo.value()) ? tableInfo.value() : entityType.getSimpleName();

        if (tableInfo.mapUnderscoreToCamelCase()) {
            schema = hump2Line(schema, true);
            table = hump2Line(table, true);
        }
        boolean autoProperty = tableInfo.autoMapping();
        boolean useDelimited = tableInfo.useDelimited();
        boolean caseInsensitive = tableInfo.caseInsensitive();
        return new TableDef<>(catalog, schema, table, entityType, autoProperty, useDelimited, caseInsensitive, this.options.getDefaultDialect(), typeRegistry);
    }

    private void resolveProperty(TableDef<?> tableDef, String name, Class<?> type, Property handler, TypeHandlerRegistry typeRegistry, Table tableInfo) {
        Annotation[] annotations = BeanUtils.getPropertyAnnotation(handler);
        Column info = null;
        for (Annotation a : annotations) {
            if (info == null && a instanceof Column) {
                info = (Column) a;
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

    private String hump2Line(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        } else {
            return StringUtils.humpToLine(str);
        }
    }

    private static final Map<Class<?>, TableDefaultInfo> CACHE_TABLE_MAP = new WeakHashMap<>();

    private synchronized static TableDefaultInfo fetchTableInfo(ClassLoader classLoader, Class<?> entityType, MappingOptions options) {
        if (CACHE_TABLE_MAP.containsKey(entityType)) {
            return CACHE_TABLE_MAP.get(entityType);
        }

        Map<String, String> confData = new HashMap<>();
        fetchPackageInfo(confData, TableDefault.class, classLoader, entityType.getName());
        fetchEntityInfo(confData, Table.class, classLoader, entityType.getName());

        TableDefaultInfo tableInfo = new TableDefaultInfo(confData, classLoader, options);
        CACHE_TABLE_MAP.put(entityType, tableInfo);
        return tableInfo;
    }

    private static void fetchPackageInfo(final Map<String, String> confData, Class<?> matchType, final ClassLoader classLoader, final String className) {
        if (StringUtils.isBlank(className)) {
            return;
        }

        String packageName = StringUtils.substringBeforeLast(className, ".");

        for (; ; ) {
            fetchEntityInfo(confData, matchType, classLoader, packageName + ".package-info");
            if (!confData.isEmpty()) {
                break;
            }
            if (packageName.indexOf('.') == -1) {
                break;
            }
            packageName = StringUtils.substringBeforeLast(packageName, ".");
            if (StringUtils.isBlank(packageName)) {
                break;
            }
        }
    }

    private static void fetchEntityInfo(final Map<String, String> confData, Class<?> matchType, final ClassLoader classLoader, final String className) {
        if (StringUtils.isBlank(className)) {
            return;
        }

        String packageName = className.replace(".", "/");
        InputStream asStream = classLoader.getResourceAsStream(packageName + ".class");
        if (asStream == null) {
            return;
        }

        try {
            ClassReader classReader = new ClassReader(asStream);
            classReader.accept(new ClassVisitor(Opcodes.ASM9) {
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    if (!AsmTools.toAsmType(matchType).equals(desc)) {
                        return super.visitAnnotation(desc, visible);
                    }
                    return new TableDefaultVisitor(Opcodes.ASM9, super.visitAnnotation(desc, visible), confData);
                }
            }, ClassReader.SKIP_CODE);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
