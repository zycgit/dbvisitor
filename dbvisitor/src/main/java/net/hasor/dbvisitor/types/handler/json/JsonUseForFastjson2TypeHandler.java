/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.json;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.types.NoCache;

/**
 * 读写 {@link Object}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
@NoCache
public class JsonUseForFastjson2TypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final Logger logger = Logger.getLogger(JsonUseForFastjson2TypeHandler.class);

    public JsonUseForFastjson2TypeHandler(Class<?> type) {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonUseForFastjson2TypeHandler(" + type + ")");
        }
        this.rawType = type;
    }

    @Override
    public String toString() {
        return "JsonUseForFastjson2TypeHandler[" + this.rawType + "]@" + super.hashCode();
    }

    @Override
    protected Object parse(String json) {
        return JSON.parseObject(json, this.rawType);
    }

    @Override
    protected String toJson(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
