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
 * JDBC 结果集行数据处理器。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface RowCallbackHandler {
    /**
     * 实现这个方法用于处理结果集的一行记录。
     * 注意：不要调用结果集的 next() 方法。
     */
    void processRow(ResultSet rs, int rowNum) throws SQLException;
}
