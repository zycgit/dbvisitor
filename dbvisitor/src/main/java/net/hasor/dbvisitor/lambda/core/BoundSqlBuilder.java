/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
import java.sql.SQLException;
import net.hasor.dbvisitor.dialect.BoundSql;

/**
 * BoundSql 构建器接口
 * 该函数式接口用于构建 BoundSql 对象，BoundSql 包含SQL语句和对应的参数信息。
 * 主要用于构建 SQL 时获取最终的 SQL 语句和参数。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
@FunctionalInterface
public interface BoundSqlBuilder {
    /** 获取 BoundSql 对象 */
    BoundSql getBoundSql() throws SQLException;
}
