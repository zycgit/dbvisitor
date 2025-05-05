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
 * 对应XML中 <where> 标签实现类，继承自 {@link TrimDynamicSql}
 * 功能特点：
 * 1. 自动处理WHERE条件前的AND/OR关键字
 * 2. 如果子节点有输出，会自动添加WHERE关键字
 * 3. 如果子节点无输出，不会生成任何SQL片段
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class WhereDynamicSql extends TrimDynamicSql {
    /**
     * 构造函数，初始化where标签的trim参数
     * 参数说明：
     * 1. prefix: 前缀"where"
     * 2. prefixOverrides: 需要移除的前缀("and | or")
     * 3. suffixOverrides: 无需要移除的后缀
     */
    public WhereDynamicSql() {
        super("where", "", "and | or", "");
    }
}