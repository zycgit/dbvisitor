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
package net.hasor.dbvisitor.mapper.def;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;

import java.util.function.Function;

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