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
package net.hasor.dbvisitor.dynamic;
import net.hasor.dbvisitor.types.MappedArg;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 代表一个动态 SQL Build 之后的具体 SQL 和其参数
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlArg extends MappedArg {
    private String   expr;
    private SqlMode  sqlMode;
    private Class<?> javaType;

    public SqlArg(String expr, Object value, SqlMode sqlMode, Integer jdbcType, Class<?> javaType, TypeHandler<?> typeHandler) {
        super(value, jdbcType, typeHandler);
        this.expr = expr;
        this.sqlMode = sqlMode;
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

    public SqlMode getSqlMode() {
        return this.sqlMode;
    }

    public void setSqlMode(SqlMode sqlMode) {
        this.sqlMode = sqlMode;
    }

    public Class<?> getJavaType() {
        return this.javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public String toString() {
        return "SqlArg{" + "value=" + this.getValue() + '}';
    }
}
