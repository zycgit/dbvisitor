/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.rule;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.internal.OgnlUtils;

/**
 * IF 条件规则如果参数不为空，则生成 'column = ?'。
 * 功能特点：
 * 1. 实现 SqlRule 接口，提供条件判断和执行能力
 * 2. 支持OGNL表达式进行条件判断
 * 3. 条件为真时执行SQL片段构建
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class IfRule implements SqlRule {
    public static final SqlRule INSTANCE_IF = new IfRule();

    /**
     * 测试条件是否满足
     * @param data 参数源
     * @param context 查询上下文
     * @param activeExpr 条件表达式(OGNL)
     * @return 如果表达式为空或计算结果为true则返回true
     */
    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(OgnlUtils.evalOgnl(activeExpr, data));
    }

    /**
     * 执行规则逻辑
     * @param data 参数源
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     * @param activeExpr 活动表达式
     * @param ruleValue 规则值(要构建的SQL片段)
     * @throws SQLException 如果构建过程中出错
     */
    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        if (ruleValue != null) {
            DynamicParsed.getParsedSql(ruleValue).buildQuery(data, context, sqlBuilder);
        }
    }

    @Override
    public String toString() {
        return "if [" + this.hashCode() + "]";
    }
}
