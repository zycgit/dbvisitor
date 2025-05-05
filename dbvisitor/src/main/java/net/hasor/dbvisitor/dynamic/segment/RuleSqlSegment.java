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
package net.hasor.dbvisitor.dynamic.segment;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;

import java.sql.SQLException;

/**
 * 规则 SQL 片段实现类，用于处理动态 SQL 规则
 * 功能特点：
 * 1. 实现 {@link SqlSegment} 接口，提供 SQL 片段构建功能
 * 2. 支持动态规则处理（如if、where、set等）
 * 3. 根据规则条件决定是否执行 SQL 片段
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public class RuleSqlSegment implements SqlSegment {
    private final String ruleExpr;
    private final String ruleName;
    private final String activeExpr;
    private final String ruleValue;

    /**
     * 构造函数
     * @param ruleExpr 规则表达式
     * @param ruleName 规则名称
     * @param activeExpr 激活表达式
     * @param ruleValue 规则值
     */
    public RuleSqlSegment(String ruleExpr, String ruleName, String activeExpr, String ruleValue) {
        this.ruleExpr = ruleExpr;
        this.ruleName = ruleName;
        this.activeExpr = activeExpr;
        this.ruleValue = ruleValue;
    }

    /** 获取规则表达式 */
    public String getExpr() {
        return this.ruleExpr;
    }

    /** 获取规则名称 */
    public String getRule() {
        return this.ruleName;
    }

    /** 获取激活表达式 */
    public String getActiveExpr() {
        return this.activeExpr;
    }

    /** 获取规则值 */
    public String getRuleValue() {
        return this.ruleValue;
    }

    /** 构建 SQL 查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        SqlRule ruleByName = context.findRule(this.ruleName);
        if (ruleByName == null) {
            throw new UnsupportedOperationException("rule `" + this.ruleName + "` Unsupported.");
        }
        if (ruleByName.test(data, context, this.activeExpr)) {
            ruleByName.executeRule(data, context, sqlBuilder, this.activeExpr, this.ruleValue);
        }
    }

    /** 克隆当前对象，返回新的 {@link RuleSqlSegment} 实例 */
    @Override
    public RuleSqlSegment clone() {
        return new RuleSqlSegment(this.ruleExpr, this.ruleName, this.activeExpr, this.ruleValue);
    }

    @Override
    public String toString() {
        return "Rule [" + this.ruleName + ", body=" + this.ruleValue + "]";
    }
}
