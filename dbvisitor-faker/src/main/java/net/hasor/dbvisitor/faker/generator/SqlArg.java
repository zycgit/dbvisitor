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
package net.hasor.dbvisitor.faker.generator;

import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 生成的数据
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlArg {
    private final Integer        jdbcType;
    private final TypeHandler<?> handler;
    private final Object         object;

    public SqlArg(Integer jdbcType, TypeHandler<?> handler, Object object) {
        this.jdbcType = jdbcType;
        this.handler = handler;
        this.object = object;
    }

    public Integer getJdbcType() {
        return jdbcType;
    }

    public TypeHandler<?> getHandler() {
        return handler;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "[" + jdbcType + "]" + this.object;
    }
}
