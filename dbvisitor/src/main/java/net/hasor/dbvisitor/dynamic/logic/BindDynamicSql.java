/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.logic;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.internal.OgnlUtils;

/**
 * <bind> 标签实现类，用于动态SQL中的变量绑定
 * 功能特点：
 * 1. 支持通过OGNL表达式计算绑定值
 * 2. 将计算结果绑定到指定名称的变量
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class BindDynamicSql implements DynamicSql {
    private final String name;      // 名字
    private final String valueExpr; // 值

    /**
     * 构造函数
     * @param name 绑定的变量名
     * @param valueExpr 计算绑定值的OGNL表达式
     */
    public BindDynamicSql(String name, String valueExpr) {
        this.name = name;
        this.valueExpr = valueExpr;
    }

    /** 检查是否包含SQL注入风险，固定返回false，因为 bind 标签本身不包含 SQL 片段。 */
    @Override
    public boolean isHaveInjection() {
        return false;
    }

    /**
     * 构建SQL查询（实际执行变量绑定操作）
     * @param data 参数源
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) {
        if (StringUtils.isNotBlank(this.name)) {
            Object testExprResult = OgnlUtils.evalOgnl(this.valueExpr, data);
            data.putValue(this.name, testExprResult);
        }
    }
}
