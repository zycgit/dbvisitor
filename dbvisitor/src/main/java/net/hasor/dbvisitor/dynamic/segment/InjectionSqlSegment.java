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
import net.hasor.dbvisitor.internal.OgnlUtils;

/**
 * 注入式 SQL 片段实现类，用于动态 SQL 中的 OGNL 表达式注入
 * 功能特点：
 * 1. 实现 {@link SqlSegment} 接口，提供 SQL 片段构建功能
 * 2. 支持 OGNL 表达式动态求值
 * 3. 将表达式结果直接注入到 SQL 中
 * 4. 实现了克隆功能
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public class InjectionSqlSegment implements SqlSegment {
    private final String exprString;

    /**
     * 构造函数
     * @param exprString OGN 表达式字符串
     */
    public InjectionSqlSegment(String exprString) {
        this.exprString = exprString;
    }

    /** 获取表达式字符串 */
    public String getExpr() {
        return this.exprString;
    }

    /** 构建 SQL 查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        sqlBuilder.appendSql(String.valueOf(OgnlUtils.evalOgnl(this.exprString, data)));
    }

    /** 克隆当前对象，返回新的 {@link InjectionSqlSegment} 实例 */
    @Override
    public InjectionSqlSegment clone() {
        return new InjectionSqlSegment(this.exprString);
    }

    @Override
    public String toString() {
        return "Injection [" + this.exprString + "]";
    }
}
