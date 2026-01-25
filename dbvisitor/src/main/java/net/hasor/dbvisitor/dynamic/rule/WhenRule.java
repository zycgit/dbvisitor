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
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.internal.OgnlUtils;

public class WhenRule extends AbstractCaseRule {
    public static final SqlRule INSTANCE_WHEN = new WhenRule(false);
    public static final SqlRule INSTANCE_ELSE = new WhenRule(true);
    private final       boolean isElse;

    public WhenRule(boolean isElse) {
        this.isElse = isElse;
    }

    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        return true;// always true
    }

    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String caseId = (String) data.getValue(CURRENT_CASE_ID_KEY);
        if (!data.hasValue(CURRENT_CASE_ID_KEY) || caseId == null) {
            throw new SQLException("The '" + (isElse ? "else" : "when") + "' rule must be used within the 'case' rule.");
        }

        // check previous WHEN has already matched
        Boolean matched = (Boolean) data.getValue(caseId);
        if (matched != null && matched) {
            return;
        }

        if (this.isElse) {
            this.executeElse(data, context, sqlBuilder, activeExpr, ruleValue, caseId);
        } else {
            this.executeWhen(data, context, sqlBuilder, activeExpr, ruleValue, caseId);
        }
    }

    private void executeElse(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue, String caseId) throws SQLException {
        data.putValue(caseId, Boolean.TRUE);

        StringBuilder contentToExecute = new StringBuilder();
        if (StringUtils.isNotBlank(activeExpr)) {
            contentToExecute.append(activeExpr);
        }
        if (StringUtils.isNotBlank(ruleValue)) {
            if (contentToExecute.length() > 0) {
                contentToExecute.append(",");
            }
            contentToExecute.append(ruleValue);
        }

        PlanDynamicSql parser = DynamicParsed.getParsedSql(contentToExecute.toString());
        parser.buildQuery(data, context, sqlBuilder);
    }

    private void executeWhen(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue, String caseId) throws SQLException {
        boolean isMatch;
        if (Boolean.TRUE.equals(data.getValue(caseId + HAS_TEST_EXPR_KEY))) {
            isMatch = this.testInSwitchMode(data, caseId, activeExpr);
        } else {
            isMatch = this.testInIfElseMode(data, activeExpr);
        }

        if (isMatch) {
            data.putValue(caseId, Boolean.TRUE);
            PlanDynamicSql parser = DynamicParsed.getParsedSql(ruleValue);
            parser.buildQuery(data, context, sqlBuilder);
        }
    }

    private boolean testInSwitchMode(SqlArgSource data, String caseId, String activeExpr) {
        Object testVal = data.getValue(caseId + TEST_EXPR_SUFFIX);
        Object whenVal = OgnlUtils.evalOgnl(activeExpr, data);

        // Basic equals check safely handling nulls
        if (testVal == whenVal) {
            return true;
        } else if (testVal != null && testVal.equals(whenVal)) {
            return true;
        } else if (whenVal != null && whenVal.equals(testVal)) {
            return true;
        } else {
            // Try string comparison as fallback if types differ but content same
            return String.valueOf(testVal).equals(String.valueOf(whenVal));
        }
    }

    private boolean testInIfElseMode(SqlArgSource data, String activeExpr) {
        Object result = OgnlUtils.evalOgnl(activeExpr, data);
        return Boolean.TRUE.equals(result);
    }
}
