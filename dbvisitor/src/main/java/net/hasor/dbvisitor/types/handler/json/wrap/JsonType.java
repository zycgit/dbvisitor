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
