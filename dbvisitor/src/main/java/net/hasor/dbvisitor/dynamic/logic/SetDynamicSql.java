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
/**
 * 对应XML中 <set>，继承自 {@link TrimDynamicSql}
 * 功能特点：
 * 1. 自动处理 UPDATE 语句中的 SET 子句
 * 2. 自动移除多余的逗号
 * 3. 如果子节点有输出，会自动添加 SET 关键字
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class SetDynamicSql extends TrimDynamicSql {
    /**
     * 构造函数，初始化set标签的trim参数
     * 参数说明：
     * 1. prefix: 前缀"set"
     * 2. prefixOverrides: 无需要移除的前缀
     * 3. suffixOverrides: 需要移除的后缀(",")
     */
    public SetDynamicSql() {
        super("set", "", "", ",");
    }
}