/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.dynamic.rule;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.internal.OgnlUtils;

/**
 * 条件规则抽象基类，实现SQL条件语句的动态生成逻辑
 * 功能特点：
 * 1. 支持IF条件判断模式
 * 2. 支持自动添加WHERE/AND等前缀
 * 3. 提供空值和多值校验机制
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public abstract class ConditionRule implements SqlRule {
    protected static final String[] DEFAULT_TEST_PREFIX = new String[] { "where", ",", "and", "or", "not", "!" };
    private final          String[] testPrefix;
    private final          String   mustHave;
    private final          String   mustHaveAppend;
    private final          String   append;
    protected final        boolean  usingIf;

    /**
     * 构造函数
     * @param usingIf 是否使用IF模式
     * @param testPrefix 测试前缀集合
     * @param mustHave 必须包含的关键字
     * @param mustHaveAppend 必须关键字的追加内容
     * @param append 默认追加内容
     */
    protected ConditionRule(boolean usingIf, String[] testPrefix, String mustHave, String mustHaveAppend, String append) {
        this.usingIf = usingIf;
        this.testPrefix = testPrefix;
        this.mustHave = mustHave;
        this.mustHaveAppend = mustHaveAppend;
        this.append = append;
    }

    /** 获取规则名称 */
    protected abstract String name();

    /**
     * 测试条件是否满足
     * @param data 参数源
     * @param context 查询上下文
     * @param activeExpr 活动表达式
     * @return 是否满足条件
     */
    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        if (this.usingIf) {
            return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(OgnlUtils.evalOgnl(activeExpr, data));
        } else {
            return true;
        }
    }

    /** 是否允许空值 */
    protected abstract boolean allowNullValue();

    /** 执行规则 */
    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String expr = "";
        if (this.usingIf) {
            expr = (StringUtils.isBlank(ruleValue) ? "" : ruleValue);
        } else {
            if (activeExpr != null) {
                expr += activeExpr;
                if (StringUtils.isNotBlank(ruleValue)) {
                    expr += ",";
                }
            }

            if (ruleValue != null) {
                expr += ruleValue;
            }
        }
        if (StringUtils.isBlank(expr)) {
            return;
        }

        PlanDynamicSql parsedSql = DynamicParsed.getParsedSql(expr);
        SqlBuilder tmp = parsedSql.buildQuery(data, context);
        String sqlString = tmp.getSqlString();
        Object[] sqlArgs = tmp.getArgs();

        if (StringUtils.isBlank(sqlString)) {
            return;
        }

        // Fix: Ensure we properly check nulls
        boolean allNulls = testNullValue(sqlArgs);
        if (!this.allowNullValue() && allNulls) {
            if (parsedSql.getInjectionList().isEmpty()) {
                return;
            }
        }

        String sql = sqlBuilder.getSqlString().toLowerCase();
        if (this.mustHave != null) {
            if (!sql.contains(this.mustHave)) {
                sqlBuilder.appendSql(this.mustHaveAppend);
                sql = sql + this.mustHaveAppend;
            }
        }

        for (String test : this.testPrefix) {
            if (sql.trim().endsWith(test)) {
                sqlBuilder.appendSql(sqlString, sqlArgs);
                return;
            }
        }

        sqlBuilder.appendSql(this.append);
        sqlBuilder.appendSql(sqlString, sqlArgs);
    }

    @Override
    public String toString() {
        return this.name() + " [" + this.hashCode() + "]";
    }

    /** 测试参数是否全为 null（自动解包 SqlArg 检查内部 value） */
    private static boolean testNullValue(Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                if (arg instanceof SqlArg) {
                    if (((SqlArg) arg).getValue() != null) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
