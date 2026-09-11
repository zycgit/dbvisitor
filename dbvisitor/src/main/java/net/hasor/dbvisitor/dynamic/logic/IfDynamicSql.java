/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.logic;
import java.sql.SQLException;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.internal.OgnlUtils;

/**
 * <if> 标签实现类，用于条件判断
 * 功能特点：
 * 1. 继承自 {@link ArrayDynamicSql}，可包含多个子 SQL 节点
 * 2. 支持 OGNL 表达式进行条件判断
 * 3. 条件为真时执行子节点 SQL 构建
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class IfDynamicSql extends ArrayDynamicSql {
    private final String testExpr;   // 判断表达式

    /**
     * 构造函数
     * @param testExpr 条件判断表达式(OGNL)
     */
    public IfDynamicSql(String testExpr) {
        this.testExpr = testExpr;
    }

    /** 构建SQL查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        if (test(data)) {
            this.buildBody(data, context, sqlBuilder);
        }
    }

    /** 执行子节点SQL构建 */
    public void buildBody(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        super.buildQuery(data, context, sqlBuilder);
    }

    /** 测试条件表达式 */
    protected boolean test(SqlArgSource data) {
        Object testExprResult = OgnlUtils.evalOgnl(this.testExpr, data);
        return Boolean.TRUE.equals(testExprResult);
    }
}
