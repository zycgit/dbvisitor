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
 * 回调接口，用于 JDBC 结果集转换。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {
    /** 将结果集内容转换 */
    T extractData(ResultSet rs) throws SQLException;
}
