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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.hasor.cobble.logging.Logger;

/**
 * 读写 {@link Object}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
public class JsonUseForFastjsonTypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final Logger        logger = Logger.getLogger(JsonUseForFastjsonTypeHandler.class);
    private final        Class<Object> type;

    public JsonUseForFastjsonTypeHandler(Class<Object> type) {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonUseForFastjsonTypeHandler(" + type + ")");
        }
        this.type = type;
    }

    @Override
    public String toString() {
        return "JsonUseForFastjsonTypeHandler[" + type + "]@" + super.hashCode();
    }

    @Override
    protected Object parse(String json) {
        return JSON.parseObject(json, type);
    }

    @Override
    protected String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}
