/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.logic;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;

/**
 * <include> 标签实现类，用于 SQL 宏定义和引用
 * 功能特点：
 * 1. 继承自 {@link PlanDynamicSql}，表示一个预定义的SQL片段
 * 2. 通过特殊语法格式 "@{macro, refSql}" 标识宏引用
 * 3. 主要用于动态SQL中的代码复用
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class MacroDynamicSql extends PlanDynamicSql {
    /**
     * 构造函数
     * @param refSql 引用的SQL宏名称或路径
     */
    public MacroDynamicSql(String refSql) {
        super("@{macro, " + refSql + "}");
    }
}
