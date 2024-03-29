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
package net.hasor.dbvisitor.dal.dynamic;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 代表一个动态 SQL Build 之后的具体 SQL 和其参数
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlArg {
    private String         expr;
    private Object         value;
    private SqlMode        sqlMode;
    private Integer        jdbcType;
    private Class<?>       javaType;
    private TypeHandler<?> typeHandler;

    public SqlArg(String expr, Object value, SqlMode sqlMode, Integer jdbcType, Class<?> javaType, TypeHandler<?> typeHandler) {
        this.expr = expr;
        this.value = value;
        this.sqlMode = sqlMode;
        this.typeHandler = typeHandler;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
    }

    public static SqlArg valueOf(Object obj) {
        return new SqlArg(null, obj, null, null, null, null);
    }

    public String getExpr() {
        return this.expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public SqlMode getSqlMode() {
        return this.sqlMode;
    }

    public void setSqlMode(SqlMode sqlMode) {
        this.sqlMode = sqlMode;
    }

    public Integer getJdbcType() {
        return this.jdbcType;
    }

    public void setJdbcType(Integer jdbcType) {
        this.jdbcType = jdbcType;
    }

    public Class<?> getJavaType() {
        return this.javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public TypeHandler<?> getTypeHandler() {
        return this.typeHandler;
    }

    public void setTypeHandler(TypeHandler<?> typeHandler) {
        this.typeHandler = typeHandler;
    }

    @Override
    public String toString() {
        return "SqlArg{" + "value=" + value + '}';
    }
}
