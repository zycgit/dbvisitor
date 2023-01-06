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
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.asm.AnnotationVisitor;
import net.hasor.cobble.asm.ClassReader;
import net.hasor.cobble.asm.ClassVisitor;
import net.hasor.cobble.asm.Opcodes;
import net.hasor.cobble.dynamic.AsmTools;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.mapping.TableDefault;

import java.io.InputStream;
import java.util.*;

/**
 * TableMappingResolve 的公共方法
 * @version : 2022-10-37
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractTableMappingResolve<T> implements TableMappingResolve<T> {
    private static final   Logger                  logger            = Logger.getLogger(AbstractTableMappingResolve.class);
    protected final        MappingOptions          options;
    protected static final Map<Class<?>, Class<?>> CLASS_MAPPING_MAP = new HashMap<>();

    static {
        CLASS_MAPPING_MAP.put(Iterable.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Collection.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, LinkedHashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, LinkedHashMap.class);
    }

    public AbstractTableMappingResolve(MappingOptions options) {
        this.options = options == null ? MappingOptions.buildNew() : options;
    }

    protected String hump2Line(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        } else {
            return StringUtils.humpToLine(str);
        }
    }

    private static final Map<Class<?>, TableDefaultInfo> CACHE_TABLE_MAP = new WeakHashMap<>();

    protected static TableDefaultInfo fetchDefaultInfoByEntity(ClassLoader classLoader, Class<?> entityType, MappingOptions options, Map<String, String> overwriteData) {
        if (CACHE_TABLE_MAP.containsKey(entityType)) {
            return CACHE_TABLE_MAP.get(entityType);
        }

        Map<String, String> confData = new HashMap<>();
        fetchConfigXmlInfo(confData, classLoader);
        fetchPackageInfo(confData, TableDefault.class, classLoader, entityType.getName());
        fetchEntityInfo(confData, Table.class, classLoader, entityType.getName());

        if (CollectionUtils.isNotEmpty(overwriteData)) {
            overwriteData.forEach((key, val) -> {
                if (StringUtils.isNotBlank(val)) {
                    confData.put(key, val);
                }
            });
        }

        TableDefaultInfo tableInfo = new TableDefaultInfo(confData, classLoader, options);
        CACHE_TABLE_MAP.put(entityType, tableInfo);
        return tableInfo;
    }

    private static void fetchConfigXmlInfo(final Map<String, String> confData, final ClassLoader classLoader) {
        //TODO dbvisitor.xml or dbvisitor.yml or dbvisitor.yaml or dbvisitor.json
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
