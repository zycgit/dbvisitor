/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.segment;
import java.sql.SQLException;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

/**
 * SQL 片段接口，表示动态 SQL 中的一个可构建片段。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public interface SqlSegment extends Cloneable {
    /**
     * 构建 SQL 查询片段
     * @param data 参数数据源
     * @param context 查询上下文
     * @param sqlBuilder SQL 构建器
     */
    void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException;

    /** 克隆当前 SQL 片段 */
    SqlSegment clone();
}
