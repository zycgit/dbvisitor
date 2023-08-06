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
package net.hasor.dbvisitor.types.handler;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import net.hasor.cobble.logging.Logger;

/**
 * 读写 {@link Object}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
public class JsonUseForFastjson2TypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final Logger   logger = Logger.getLogger(JsonUseForFastjson2TypeHandler.class);
    private final        Class<?> type;

    public JsonUseForFastjson2TypeHandler(Class<?> type) {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonUseForFastjson2TypeHandler(" + type + ")");
        }
        this.type = type;
    }

    @Override
    public String toString() {
        return "JsonUseForFastjson2TypeHandler[" + type + "]@" + super.hashCode();
    }

    @Override
    protected Object parse(String json) {
        return JSON.parseObject(json, type);
    }

    @Override
    protected String toJson(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
