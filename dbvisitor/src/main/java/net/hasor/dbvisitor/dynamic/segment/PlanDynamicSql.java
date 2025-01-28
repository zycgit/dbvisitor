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

import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.*;

import java.sql.SQLException;
import java.util.*;

/**
 * 本处理器，兼容 @{...}、#{...}、${...} 三种写法。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class PlanDynamicSql implements Cloneable, DynamicSql {
    private final StringBuilder    queryStringOri  = new StringBuilder("");
    private final List<SqlSegment> queryStringPlan = new LinkedList<>();
    private       boolean          haveInjection   = false;

    public PlanDynamicSql() {
    }

    public PlanDynamicSql(String test) {
        PlanDynamicSql parsedSql = DynamicParsed.getParsedSql(test);
        this.queryStringOri.append(parsedSql.queryStringOri);
        this.queryStringPlan.addAll(parsedSql.queryStringPlan);
        this.haveInjection = parsedSql.haveInjection;
    }

    public void parsedAppend(String append) {
        DynamicParsed.parsedSqlTo(append, this);
    }

    /** 追加字符串 */
    public void appendString(char[] append, int offset, int count) {
        this.appendString(new String(append, offset, count));
    }

    /** 追加字符串 */
    public void appendString(String append) {
        if (append == null) {
            return;
        }
        if (StringUtils.isBlank(append) && this.queryStringOri.length() > 0) {
            char lastChar = this.queryStringOri.charAt(this.queryStringOri.length() - 1);
            if (lastChar == '\r' || lastChar == '\n' || lastChar == '\t' || lastChar == ' ') {
                return;
            }
        }

        this.queryStringOri.append(append);
        if (!this.queryStringPlan.isEmpty()) {
            Object ss = this.queryStringPlan.get(this.queryStringPlan.size() - 1);
            if (ss instanceof TextSqlSegment) {
                ((TextSqlSegment) ss).append(append);
                return;
            }
        }
        this.queryStringPlan.add(new TextSqlSegment(append));
    }

    /** 追加注入语句，例如：${xxx} */
    public void appendInjectionExpr(String exprString) {
        this.queryStringOri.append("${" + exprString + "}");
        this.queryStringPlan.add(new InjectionSqlSegment(exprString));
        this.haveInjection = true;
    }

    /** 追加规则，例如：@{name, active, expr} */
    public void appendRuleExpr(String ruleExpr, String ruleName, String activeExpr, String exprString) {
        this.queryStringOri.append("@{" + ruleExpr + "}");
        this.queryStringPlan.add(new RuleSqlSegment(ruleExpr, ruleName, activeExpr, exprString));
        this.haveInjection = true;
    }

    /** 追加高级名称参数，例如：#{name, active, expr} */
    public void appendNamedParameter(String ruleExpr, String exprString, Map<String, String> exprMap) {
        this.queryStringOri.append("#{");
        this.queryStringOri.append(ruleExpr);
        this.queryStringOri.append("}");

        this.queryStringPlan.add(new NamedSqlSegment(exprString, exprMap));
    }

    /** 追加名称参数，例如：:name 或 &name */
    public void appendNamedParameter(String exprString, boolean isAmp) {
        if (isAmp) {
            this.queryStringOri.append("&" + exprString);
        } else {
            this.queryStringOri.append(":" + exprString);
        }
        this.queryStringPlan.add(new NamedSqlSegment(exprString, Collections.emptyMap()));
    }

    /** 追加位置参数，例如：? */
    public void appendPositionArg(int position) {
        this.queryStringOri.append("?");
        this.queryStringPlan.add(new PositionSqlSegment(position));
    }

    /** 是否包含替换占位符，如果包含替换占位符那么不能使用批量模式 */
    public boolean isHaveInjection() {
        return this.haveInjection;
    }

    public int getSqlModifier() {
        boolean hasPosition = false;
        boolean hasNamed = false;
        boolean hasRule = false;
        boolean hasInjection = false;
        for (SqlSegment segment : this.queryStringPlan) {
            if (segment instanceof PositionSqlSegment) {
                hasPosition = true;
            } else if (segment instanceof NamedSqlSegment) {
                hasNamed = true;
            } else if (segment instanceof RuleSqlSegment) {
                hasRule = true;
            } else if (segment instanceof InjectionSqlSegment) {
                hasInjection = true;
            }
        }

        int i = 0;
        i = hasPosition ? (i | SqlModifier.POSITION) : i;
        i = hasNamed ? (i | SqlModifier.NAMED) : i;
        i = hasRule ? (i | SqlModifier.RULE) : i;
        i = hasInjection ? (i | SqlModifier.INJECTION) : i;
        return i;
    }

    public String getOriSqlString() {
        return this.queryStringOri.toString();
    }

    public List<String> getInjectionList() {
        List<String> result = new ArrayList<>();
        for (SqlSegment segment : this.queryStringPlan) {
            if (segment instanceof InjectionSqlSegment) {
                result.add(((InjectionSqlSegment) segment).getExpr());
            }
        }
        return result;
    }

    public List<Integer> getPositionList() {
        List<Integer> result = new ArrayList<>();
        for (SqlSegment segment : this.queryStringPlan) {
            if (segment instanceof PositionSqlSegment) {
                result.add(((PositionSqlSegment) segment).getPosition());
            }
        }
        return result;
    }

    public List<NameInfo> getNamedList() {
        List<NameInfo> result = new ArrayList<>();
        for (SqlSegment segment : this.queryStringPlan) {
            if (segment instanceof NamedSqlSegment) {
                result.add(new NameInfo(((NamedSqlSegment) segment).getExpr(), ((NamedSqlSegment) segment).getConfig()));
            }
        }
        return result;
    }

    public List<RuleInfo> getRuleList() {
        List<RuleInfo> result = new ArrayList<>();
        for (SqlSegment segment : this.queryStringPlan) {
            if (segment instanceof RuleSqlSegment) {
                String rule = ((RuleSqlSegment) segment).getRule();
                String activeExpr = ((RuleSqlSegment) segment).getActiveExpr();
                String value = ((RuleSqlSegment) segment).getRuleValue();
                result.add(new RuleInfo(rule, activeExpr, value));
            }
        }
        return result;
    }

    @Override
    public void buildQuery(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder) throws SQLException {
        for (SqlSegment fxSegment : this.queryStringPlan) {
            fxSegment.buildQuery(data, context, sqlBuilder);
        }
    }

    @Override
    public DynamicSql clone() {
        PlanDynamicSql clone = new PlanDynamicSql();
        clone.queryStringOri.append(this.queryStringOri);
        for (SqlSegment fxSegment : this.queryStringPlan) {
            clone.queryStringPlan.add(fxSegment.clone());
        }
        clone.haveInjection = this.haveInjection;
        return clone;
    }

    public static class NameInfo {
        private final String              expr;
        private final Map<String, String> config;

        public NameInfo(String expr, Map<String, String> config) {
            this.expr = expr;
            this.config = config;
        }

        public String getExpr() {
            return this.expr;
        }

        public Map<String, String> getConfig() {
            return this.config;
        }
    }

    public static class RuleInfo {
        private final String ruleName;
        private final String activeExpr;
        private final String ruleValue;

        public RuleInfo(String ruleName, String activeExpr, String ruleValue) {
            this.ruleName = ruleName;
            this.activeExpr = activeExpr;
            this.ruleValue = ruleValue;
        }

        public String getRule() {
            return this.ruleName;
        }

        public String getActiveExpr() {
            return this.activeExpr;
        }

        public String getRuleValue() {
            return this.ruleValue;
        }
    }
}
