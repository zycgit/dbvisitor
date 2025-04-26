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
 * DELETE操作SQL配置类
 * 继承自DmlConfig，专门用于处理DELETE语句的配置
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public class DeleteConfig extends DmlConfig {
    /**
     * 构造函数
     * @param target 动态SQL数组
     * @param config 配置函数
     */
    public DeleteConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);
    }

    /**
     * 获取查询类型
     * @return 返回DELETE操作类型
     */
    @Override
    public QueryType getType() {
        return QueryType.Delete;
    }
}