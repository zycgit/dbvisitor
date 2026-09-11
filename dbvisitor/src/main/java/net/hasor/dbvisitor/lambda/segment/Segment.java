/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.segment;
import java.sql.SQLException;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * SQL 片段。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-02
 */
@FunctionalInterface
public interface Segment {
    /** 获取 SQL 内容 */
    String getSqlSegment(boolean delimited, SqlDialect dialect) throws SQLException;
}
