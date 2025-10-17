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