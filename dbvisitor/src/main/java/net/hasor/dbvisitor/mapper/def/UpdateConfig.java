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
 * 更新SQL配置类，用于定义UPDATE语句的配置
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public class UpdateConfig extends DmlConfig {

    /**
     * 构造函数
     * @param target 动态SQL构建目标对象
     * @param config 配置获取函数
     */
    public UpdateConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);
    }

    @Override
    public QueryType getType() {
        return QueryType.Update;
    }
}
