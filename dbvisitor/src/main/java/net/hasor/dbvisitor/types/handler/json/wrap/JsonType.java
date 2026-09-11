/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.json.wrap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON 类型工具类
 * <p>提供便捷的工厂方法，用于获取 JSON 包装类的 Class 对象。</p>
 * @author 赵永春 (zyc@hasor.net)
 */
public class JsonType {
    /**
     * 获取 JsonHashMap 的 Class 对象（无序 Map）
     */
    public static Class<? extends Map> jsonMap() {
        return JsonHashMap.class;
    }

    /**
     * 获取 JsonArrayList 的 Class 对象
     */
    public static Class<? extends List> jsonList() {
        return JsonArrayList.class;
    }

    /**
     * 获取 JsonHashSet 的 Class 对象
     */
    public static Class<? extends Set> jsonSet() {
        return JsonHashSet.class;
    }
}
