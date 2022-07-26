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
package net.hasor.dbvisitor.dal.dynamic.segment;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.rule.ArgRule;
import net.hasor.dbvisitor.dal.dynamic.rule.SqlBuildRule;
import net.hasor.dbvisitor.dal.dynamic.rule.TextRule;
import net.hasor.dbvisitor.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.hasor.dbvisitor.dal.dynamic.ognl.OgnlUtils.evalOgnl;

/**
 * 本处理器，兼容 @{...}、#{...}、${...} 三种写法。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-28
 */
public class DefaultSqlSegment implements Cloneable, DynamicSql {
    private final StringBuilder   queryStringOri  = new StringBuilder("");
    private final List<FxSegment> queryStringPlan = new LinkedList<>();
    private       boolean         havePlaceholder = false;

    /** 追加 字符串 */
    public void appendString(String append) {
        this.queryStringOri.append(append);
        if (!this.queryStringPlan.isEmpty()) {
            Object ss = this.queryStringPlan.get(this.queryStringPlan.size() - 1);
            if (ss instanceof TextFxSegment) {
                ((TextFxSegment) ss).append(append);
                return;
            }
        }
        this.queryStringPlan.add(new TextFxSegment(append));
    }

    /** 追加 注入语句 */
    public void appendPlaceholderExpr(String exprString) {
        this.queryStringOri.append("${" + exprString + "}");
        this.queryStringPlan.add(new PlaceholderFxSegment(exprString));
        this.havePlaceholder = true;
    }

    /** 追加 规则 */
    public void appendRuleExpr(String ruleName, String activeExpr, String exprString) {
        this.queryStringOri.append("@{" + ruleName + ", " + activeExpr + ", " + exprString + "}");
        this.queryStringPlan.add(new RuleFxSegment(ruleName, activeExpr, exprString));
        this.havePlaceholder = true;
    }

    /** 添加一个 SQL 参数，最终这个参数会通过 PreparedStatement 形式传递。 */
    public void appendValueExpr(String exprString, Map<String, String> exprMap) {
        this.queryStringOri.append("#{");
        this.queryStringOri.append(exprString);

        if (exprMap != null && !exprMap.isEmpty()) {
            for (String key : exprMap.keySet()) {
                String value = exprMap.get(key);
                this.queryStringOri.append(", " + key + "=" + value);
            }
        }

        this.queryStringOri.append("}");
        this.queryStringPlan.add(new ParameterFxSegment(exprString, exprMap));
    }

    /** 是否包含替换占位符，如果包含替换占位符那么不能使用批量模式 */
    public boolean isHavePlaceholder() {
        return this.havePlaceholder;
    }

    public String getOriSqlString() {
        return this.queryStringOri.toString();
    }

    @Override
    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        for (FxSegment fxSegment : this.queryStringPlan) {
            fxSegment.buildQuery(data, context, sqlBuilder);
        }
    }

    @Override
    public DynamicSql clone() {
        DefaultSqlSegment clone = new DefaultSqlSegment();
        clone.queryStringOri.append(this.queryStringOri);
        for (FxSegment fxSegment : this.queryStringPlan) {
            clone.queryStringPlan.add(fxSegment.clone());
        }
        clone.havePlaceholder = this.havePlaceholder;
        return clone;
    }

    public static interface FxSegment extends Cloneable {
        public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException;

        public FxSegment clone();
    }

    protected static class TextFxSegment implements FxSegment {
        private final StringBuilder textString;

        public TextFxSegment(String exprString) {
            this.textString = new StringBuilder(exprString);
        }

        public void append(String append) {
            this.textString.append(append);
        }

        @Override
        public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
            TextRule.INSTANCE.executeRule(data, context, sqlBuilder, "true", this.textString.toString());
        }

        @Override
        public TextFxSegment clone() {
            return new TextFxSegment(this.textString.toString());
        }

        @Override
        public String toString() {
            return "Text [" + this.textString + "]";
        }
    }

    protected static class PlaceholderFxSegment implements FxSegment {
        private final StringBuilder exprString;

        public PlaceholderFxSegment(String exprString) {
            this.exprString = new StringBuilder(exprString);
        }

        @Override
        public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
            String placeholderQuery = String.valueOf(evalOgnl(this.exprString.toString(), data));
            TextRule.INSTANCE.executeRule(data, context, sqlBuilder, "true", placeholderQuery);
        }

        @Override
        public PlaceholderFxSegment clone() {
            return new PlaceholderFxSegment(this.exprString.toString());
        }

        @Override
        public String toString() {
            return "Placeholder [" + this.exprString + "]";
        }
    }

    protected static class RuleFxSegment implements FxSegment {
        private final String ruleName;
        private final String activeExpr;
        private final String ruleValue;

        public RuleFxSegment(String ruleName, String activeExpr, String ruleValue) {
            this.ruleName = ruleName;
            this.activeExpr = activeExpr;
            this.ruleValue = ruleValue;
        }

        @Override
        public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
            SqlBuildRule ruleByName = context.findRule(this.ruleName);
            if (ruleByName == null) {
                throw new UnsupportedOperationException("rule `" + this.ruleName + "` Unsupported.");
            }
            if (ruleByName.test(data, context, this.activeExpr)) {
                ruleByName.executeRule(data, context, sqlBuilder, this.activeExpr, this.ruleValue);
            }
        }

        @Override
        public RuleFxSegment clone() {
            return new RuleFxSegment(this.ruleName, this.activeExpr, this.ruleValue);
        }

        @Override
        public String toString() {
            return "Rule [" + this.ruleName + ", body=" + this.ruleValue + "]";
        }
    }

    protected static class ParameterFxSegment implements FxSegment {
        private final String              exprString;
        private final Map<String, String> config;

        public ParameterFxSegment(String exprString, Map<String, String> config) {
            this.exprString = exprString;
            this.config = config;
        }

        @Override
        public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
            ArgRule.INSTANCE.executeRule(data, context, sqlBuilder, this.exprString, this.config);
        }

        @Override
        public ParameterFxSegment clone() {
            return new ParameterFxSegment(this.exprString, this.config);
        }

        @Override
        public String toString() {
            return "Parameter [" + this.exprString + "]";
        }
    }
}
