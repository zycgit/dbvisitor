/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.json;
import java.util.function.Function;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.ObjectUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.types.NoCache;

/**
 * 基于 Jackson 库实现的 JSON 类型处理器，用于数据库字段与 Java 对象之间的 JSON 格式转换。
 * 支持通过静态方法全局配置 ObjectMapper 或按类型动态获取 ObjectMapper。
 * 读写 {@link Object}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
@NoCache
public class JsonUseForJacksonTypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final Logger                           logger                 = Logger.getLogger(JsonUseForJacksonTypeHandler.class);
    private static       ObjectMapper                     OBJECT_MAPPER          = new ObjectMapper();
    private static       Function<Class<?>, ObjectMapper> OBJECT_MAPPER_FUNCTION = rawType -> OBJECT_MAPPER;

    /** 获取当前使用的全局 ObjectMapper 实例 */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /** 设置全局使用的 ObjectMapper 实例 */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        ObjectUtils.checkNotNull(objectMapper, "ObjectMapper should not be null");
        OBJECT_MAPPER = objectMapper;
    }

    /** 获取当前配置的 ObjectMapper 获取函数 */
    public static Function<Class<?>, ObjectMapper> getObjectMapperFunction() {
        return OBJECT_MAPPER_FUNCTION;
    }

    /** 设置按类型获取 ObjectMapper 的函数 */
    public static void setObjectMapperFunction(Function<Class<?>, ObjectMapper> function) {
        ObjectUtils.checkNotNull(function, "ObjectMapperFunction should not be null");
        OBJECT_MAPPER_FUNCTION = function;
    }

    public JsonUseForJacksonTypeHandler(Class<?> type) {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonUseForJacksonTypeHandler(" + type + ")");
        }
        this.rawType = type;
    }

    @Override
    public String toString() {
        return "JsonUseForJacksonTypeHandler[" + this.rawType + "]@" + super.hashCode();
    }

    @Override
    protected Object parse(String json) throws Exception {
        return getObjectMapperFunction().apply(this.rawType).readValue(json, this.rawType);
    }

    @Override
    protected String toJson(Object obj) throws Exception {
        return getObjectMapperFunction().apply(this.rawType).writeValueAsString(obj);
    }
}
