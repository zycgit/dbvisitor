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
import net.hasor.dbvisitor.dynamic.SqlMode;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;

/**
 * 表示动态SQL构建后的具体SQL参数。
 * 封装了SQL参数的各种属性，包括参数名、值、JDBC类型、Java类型等，
 * 支持输入参数、输出参数和输入输出参数等多种模式。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class SqlArg {
    private String                name;         // mappingTo #{xxx}
    private String                asName;       // mappingTo #{xxx,name=asName}
    private Object                value;
    private Integer               jdbcType;     // sqlMode = in/out/inout
    private Class<?>              javaType;
    private TypeHandler<?>        typeHandler;  // sqlMode = in/out/inout
    // for procedure
    private SqlMode               sqlMode;
    private String                jdbcTypeName; // sqlMode = out/inout
    private Integer               scale;        // sqlMode = out/inout
    private ResultSetExtractor<?> extractor;    // sqlMode = cursor
    private RowCallbackHandler    rowHandler;   // sqlMode = cursor
    private RowMapper<?>          rowMapper;    // sqlMode = cursor

    public SqlArg(Object value, Integer jdbcType, TypeHandler<?> typeHandler) {
        this.value = value;
        this.typeHandler = typeHandler;
        this.jdbcType = jdbcType;
    }

    public SqlArg(String name, Object value, SqlMode sqlMode, Integer jdbcType, Class<?> javaType, TypeHandler<?> typeHandler) {
        this(value, jdbcType, typeHandler);
        this.name = name;
        this.sqlMode = sqlMode;
        this.javaType = javaType;
    }

    /**
     * 创建输入参数
     * @param obj 参数值
     */
    public static SqlArg valueOf(Object obj) {
        return new SqlArg(null, obj, SqlMode.In, null, null, null);
    }

    /**
     * 创建带有指定类型的输入参数
     * @param obj 参数值
     * @param javaType Java类型
     */
    public static SqlArg valueOf(Object obj, Class<?> javaType) {
        return new SqlArg(null, obj, SqlMode.In, null, javaType, null);
    }

    /**
     * 创建带有指定 {@link TypeHandler}。
     * @param obj 参数值
     * @param typeHandler typeHandler类型
     */
    public static SqlArg valueOf(Object obj, TypeHandler<?> typeHandler) {
        return new SqlArg(null, obj, SqlMode.In, null, null, typeHandler);
    }

    /**
     * 创建带有指定 JDBC 类型
     * @param obj 参数值
     * @param jdbcType JDBC 类型
     */
    public static SqlArg valueOf(Object obj, int jdbcType) {
        return new SqlArg(null, obj, SqlMode.In, jdbcType, null, null);
    }

    /**
     * 创建输出参数(OUT) - 根据Java类型自动推断JDBC类型
     * @param name 参数名称
     * @param javaType Java类型
     */
    public static SqlArg asOut(String name, Class<?> javaType) {
        if (javaType == null) {
            return new SqlArg(name, null, SqlMode.Out, null, null, null);
        } else {
            int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
            TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(javaType);
            return new SqlArg(name, null, SqlMode.Out, jdbcType, javaType, typeHandler);
        }
    }

    /**
     * 创建输出参数(OUT) - 指定JDBC类型
     * @param name 参数名称
     * @param jdbcType JDBC类型代码(来自java.sql.Types)
     */
    public static SqlArg asOut(String name, int jdbcType) {
        TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(jdbcType);
        return new SqlArg(name, null, SqlMode.Out, jdbcType, null, typeHandler);
    }

    /**
     * 创建输出参数(OUT) - 指定JDBC类型和自定义类型处理器
     * @param name 参数名称
     * @param jdbcType JDBC类型代码(来自java.sql.Types)
     * @param typeHandler 自定义类型处理器
     */
    public static SqlArg asOut(String name, int jdbcType, TypeHandler<?> typeHandler) {
        return new SqlArg(name, null, SqlMode.Out, jdbcType, null, typeHandler);
    }

    /**
     * 创建输入输出参数(INOUT) - 根据Java类型自动推断JDBC类型
     * @param name 参数名称
     * @param value 参数值
     * @param javaType Java类型
     */
    public static SqlArg asInOut(String name, Object value, Class<String> javaType) {
        int jdbcType = TypeHandlerRegistry.toSqlType(javaType);
        TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(javaType);
        return new SqlArg(name, value, SqlMode.InOut, jdbcType, javaType, typeHandler);
    }

    /**
     * 创建输入输出参数(INOUT) - 指定JDBC类型
     * @param name 参数名称
     * @param value 参数值
     * @param jdbcType JDBC类型代码(来自java.sql.Types)
     */
    public static SqlArg asInOut(String name, Object value, int jdbcType) {
        TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(jdbcType);
        return new SqlArg(name, value, SqlMode.InOut, jdbcType, null, typeHandler);
    }

    /**
     * 创建输入输出参数(INOUT) - 指定JDBC类型和自定义类型处理器
     * @param name 参数名称
     * @param value 参数值
     * @param jdbcType JDBC类型代码(来自java.sql.Types)
     * @param typeHandler 自定义类型处理器
     */
    public static SqlArg asInOut(String name, Object value, int jdbcType, TypeHandler<?> typeHandler) {
        return new SqlArg(name, value, SqlMode.InOut, jdbcType, null, typeHandler);
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
        return "SqlArg{" + "value=" + this.getValue() + '}';
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAsName() {
        return this.asName;
    }

    public void setAsName(String asName) {
        this.asName = asName;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
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

    public SqlMode getSqlMode() {
        return this.sqlMode;
    }

    public void setSqlMode(SqlMode sqlMode) {
        this.sqlMode = sqlMode;
    }

    public String getJdbcTypeName() {
        return this.jdbcTypeName;
    }

    public void setJdbcTypeName(String jdbcTypeName) {
        this.jdbcTypeName = jdbcTypeName;
    }

    public Integer getScale() {
        return this.scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public ResultSetExtractor<?> getExtractor() {
        return this.extractor;
    }

    public void setExtractor(ResultSetExtractor<?> extractor) {
        this.extractor = extractor;
    }

    public RowCallbackHandler getRowHandler() {
        return this.rowHandler;
    }

    public void setRowHandler(RowCallbackHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    public RowMapper<?> getRowMapper() {
        return this.rowMapper;
    }

    public void setRowMapper(RowMapper<?> rowMapper) {
        this.rowMapper = rowMapper;
    }
}