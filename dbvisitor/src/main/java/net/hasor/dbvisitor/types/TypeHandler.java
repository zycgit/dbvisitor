/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类型处理器接口，用于处理Java类型与JDBC类型之间的转换
 * 实现该接口可以自定义Java类型与数据库类型的转换逻辑
 * @author 赵永春 (zyc@hasor.net)
 */
public interface TypeHandler<T> {
    /**
     * 设置PreparedStatement参数
     * @param ps PreparedStatement对象
     * @param i 参数位置(从1开始)
     * @param parameter 要设置的Java参数值
     * @param jdbcType JDBC类型代码(来自java.sql.Types)
     * @throws SQLException 如果设置参数时发生错误
     */
    void setParameter(PreparedStatement ps, int i, T parameter, Integer jdbcType) throws SQLException;

    /**
     * 从ResultSet中按列名获取值
     * @param rs ResultSet对象
     * @param columnName 列名
     * @return 转换后的Java对象
     * @throws SQLException 如果获取值时发生错误
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 从ResultSet中按列索引获取值
     * @param rs ResultSet对象
     * @param columnIndex 列索引(从1开始)
     * @return 转换后的Java对象
     * @throws SQLException 如果获取值时发生错误
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * 从CallableStatement中获取输出参数值
     * @param cs CallableStatement对象
     * @param columnIndex 参数索引(从1开始)
     * @return 转换后的Java对象
     * @throws SQLException 如果获取值时发生错误
     */
    T getResult(CallableStatement cs, int columnIndex) throws SQLException;
}
