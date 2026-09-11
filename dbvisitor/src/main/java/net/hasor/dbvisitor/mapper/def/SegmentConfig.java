/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.def;
import java.util.function.Function;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;

/**
 * SQL片段配置类，用于定义可重用的SQL片段
 */
public class SegmentConfig extends SqlConfig {
    /**
     * 构造函数
     * @param target 动态SQL构建目标对象
     * @param config 配置获取函数
     */
    public SegmentConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);
    }

    @Override
    public QueryType getType() {
        return QueryType.Segment;
    }
}
