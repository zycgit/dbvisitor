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
package net.hasor.dbvisitor.types;
import java.util.Objects;

/**
 * 代表一个动态 SQL Build 之后的具体 SQL 和其参数
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappedArg {
    private final Object         value;
    private final Integer        jdbcType;
    private final TypeHandler<?> typeHandler;

    public MappedArg(Object value, Integer jdbcType, TypeHandler<?> typeHandler) {
        this.value = value;
        this.typeHandler = typeHandler;
        this.jdbcType = jdbcType;
    }

    public Object getValue() {
        return this.value;
    }

    public Integer getJdbcType() {
        return this.jdbcType;
    }

    public TypeHandler<?> getTypeHandler() {
        return this.typeHandler;
    }

    @Override
    public boolean equals(Object o) {
        return Objects.equals(o, this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}