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
 * 查询SQL配置类，用于定义SELECT查询语句的配置
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public class SelectConfig extends DqlConfig {
    /**
     * 构造函数
     * @param target 动态SQL构建目标对象
     * @param config 配置获取函数
     */
    public SelectConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);
    }

    /**
     * 获取SQL类型
     * @return 返回Select类型，表示这是一个查询语句
     */
    @Override
    public QueryType getType() {
        return QueryType.Select;
    }
}