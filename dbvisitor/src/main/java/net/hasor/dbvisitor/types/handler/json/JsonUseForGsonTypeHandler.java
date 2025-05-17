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
package net.hasor.dbvisitor.types.handler.json;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.hasor.cobble.ObjectUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.types.NoCache;

import java.util.function.Function;

/**
 * 基于 Gson 库实现的 JSON 类型处理器，用于数据库字段与 Java 对象之间的 JSON 格式转换。
 * 支持通过静态方法全局配置 Gson 或按类型动态获取 Gson。
 * 读写 {@link Object}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
@NoCache
public class JsonUseForGsonTypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final Logger                   logger        = Logger.getLogger(JsonUseForGsonTypeHandler.class);
    private static       Gson                     GSON          = new GsonBuilder().create();
    private static       Function<Class<?>, Gson> GSON_FUNCTION = rawType -> GSON;

    /** 获取当前使用的全局 Gson 实例 */
    public static Gson getGson() {
        return GSON;
    }

    /** 设置全局使用的 Gson 实例 */
    public static void setGson(Gson gson) {
        ObjectUtils.checkNotNull(gson, "Gson should not be null");
        GSON = gson;
    }

    /** 获取当前配置的 Gson 获取函数 */
    public static Function<Class<?>, Gson> getGsonFunction() {
        return GSON_FUNCTION;
    }

    /** 设置按类型获取 Gson 的函数 */
    public static void setGsonFunction(Function<Class<?>, Gson> function) {
        ObjectUtils.checkNotNull(function, "ObjectMapperFunction should not be null");
        GSON_FUNCTION = function;
    }

    public JsonUseForGsonTypeHandler(Class<?> type) {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonUseForGsonTypeHandler(" + type + ")");
        }
        this.rawType = type;
    }

    @Override
    public String toString() {
        return "JsonUseForGsonTypeHandler[" + this.rawType + "]@" + super.hashCode();
    }

    @Override
    protected Object parse(String json) {
        return getGsonFunction().apply(this.rawType).fromJson(json, this.rawType);
    }

    @Override
    protected String toJson(Object obj) {
        return getGsonFunction().apply(this.rawType).toJson(obj);
    }
}
