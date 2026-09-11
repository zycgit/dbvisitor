/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 这个接口用来映射 JDBC 结果集中一行数据。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface RowMapper<T> {
    /**
     * 实现这个方法为结果集的一行记录进行转换，并将最终转换结果返回。如果返回为 null 等同于忽略该行。
     * 需要注意，不要调用结果集的 next() 方法。
     * @param rs 记录集
     * @param rowNum 当前记录的行号
     */
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
