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
 * DML 操作 SQL 配置基类
 * 提供INSERT、UPDATE、DELETE等数据操作语言的公共配置基础
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public abstract class DmlConfig extends SqlConfig {
    /**
     * 构造函数
     * @param target 动态SQL数组
     * @param config 配置转换函数
     */
    public DmlConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);
    }
}
