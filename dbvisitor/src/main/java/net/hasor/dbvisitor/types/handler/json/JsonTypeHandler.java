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
import net.hasor.cobble.function.EFunction;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.types.NoCache;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 读写 {@link Object}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
@NoCache
public class JsonTypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final Logger                  logger = Logger.getLogger(JsonTypeHandler.class);
    private              AbstractJsonTypeHandler jsonTypeHandler;

    private static final Map<String, EFunction<Class<?>, AbstractJsonTypeHandler<?>, ClassNotFoundException>> CREATOR = new LinkedHashMap<>();

    static {
        CREATOR.put("com.fasterxml.jackson.databind.ObjectMapper", type -> {
            getClassLoader(type).loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            return new JsonUseForJacksonTypeHandler(type);
        });
        CREATOR.put("com.google.gson.Gson", type -> {
            getClassLoader(type).loadClass("com.google.gson.Gson");
            return new JsonUseForGsonTypeHandler(type);
        });
        CREATOR.put("com.alibaba.fastjson.JSON", type -> {
            getClassLoader(type).loadClass("com.alibaba.fastjson.JSON");
            return new JsonUseForFastjsonTypeHandler(type);
        });
        CREATOR.put("com.alibaba.fastjson2.JSON", type -> {
            getClassLoader(type).loadClass("com.alibaba.fastjson2.JSON");
            return new JsonUseForFastjson2TypeHandler(type);
        });
    }

    private static ClassLoader getClassLoader(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return loader == null ? ClassLoader.getSystemClassLoader() : loader;
    }

    public JsonTypeHandler(Class<?> type) throws SQLException, ClassNotFoundException {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonTypeHandler(" + type + ")");
        }
        this.rawType = type;
        this.jsonTypeHandler = this.choiceProvider(type);
    }

    protected AbstractJsonTypeHandler<?> choiceProvider(Class<?> type) throws SQLException, ClassNotFoundException {
        ClassNotFoundException lastException = null;
        for (String key : CREATOR.keySet()) {
            try {
                return CREATOR.get(key).eApply(type);
            } catch (ClassNotFoundException e) {
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        } else {
            throw new SQLException("Unable to select a json provider.");
        }
    }

    @Override
    public String toString() {
        return "JsonTypeHandler[" + this.rawType + "]@" + super.hashCode();
    }

    @Override
    protected Object parse(String json) throws Exception {
        return this.jsonTypeHandler.parse(json);
    }

    @Override
    protected String toJson(Object obj) throws Exception {
        return this.jsonTypeHandler.toJson(obj);
    }
}
