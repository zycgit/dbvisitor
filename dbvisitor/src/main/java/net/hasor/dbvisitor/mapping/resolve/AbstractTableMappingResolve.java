/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.resolve;
import java.util.*;
import net.hasor.cobble.StringUtils;

/**
 * TableMappingResolve 的公共方法
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-10-37
 */
public abstract class AbstractTableMappingResolve<T> implements TableMappingResolve<T> {
    protected static final Map<Class<?>, Class<?>> CLASS_MAPPING_MAP = new HashMap<>();

    static {
        CLASS_MAPPING_MAP.put(Iterable.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Collection.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, LinkedHashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, LinkedHashMap.class);
    }

    protected String hump2Line(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        } else {
            return StringUtils.humpToLine(str);
        }
    }
}
