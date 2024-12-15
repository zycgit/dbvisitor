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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.SqlBuildRule;

import java.sql.SQLException;

public class RuleSqlSegment implements SqlSegment {
    private final String ruleExpr;
    private final String ruleName;
    private final String activeExpr;
    private final String ruleValue;

    public RuleSqlSegment(String ruleExpr, String ruleName, String activeExpr, String ruleValue) {
        this.ruleExpr = ruleExpr;
        this.ruleName = ruleName;
        this.activeExpr = activeExpr;
        this.ruleValue = ruleValue;
    }

    public String getExpr() {
        return this.ruleExpr;
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

    @Override
    public void buildQuery(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder) throws SQLException {
        SqlBuildRule ruleByName = context.getRuleRegistry().findByName(this.ruleName);
        if (ruleByName == null) {
            throw new UnsupportedOperationException("rule `" + this.ruleName + "` Unsupported.");
        }
        if (ruleByName.test(data, context, this.activeExpr)) {
            ruleByName.executeRule(data, context, sqlBuilder, this.activeExpr, this.ruleValue);
        }
    }

    @Override
    public RuleSqlSegment clone() {
        return new RuleSqlSegment(this.ruleExpr, this.ruleName, this.activeExpr, this.ruleValue);
    }

    @Override
    public String toString() {
        return "Rule [" + this.ruleName + ", body=" + this.ruleValue + "]";
    }
}
