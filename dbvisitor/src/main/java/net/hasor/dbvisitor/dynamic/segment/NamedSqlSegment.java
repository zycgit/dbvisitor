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
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.ArgRule;

import java.sql.SQLException;
import java.util.Map;

public class NamedSqlSegment implements SqlSegment {
    private final String              exprString;
    private final Map<String, String> config;

    public NamedSqlSegment(String exprString, Map<String, String> config) {
        this.exprString = exprString;
        this.config = config;
    }

    public String getExpr() {
        return this.exprString;
    }

    public Map<String, String> getConfig() {
        return this.config;
    }

    @Override
    public void buildQuery(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        ArgRule.INSTANCE.executeRule(data, context, sqlBuilder, this.exprString, this.config);
    }

    @Override
    public NamedSqlSegment clone() {
        return new NamedSqlSegment(this.exprString, this.config);
    }

    @Override
    public String toString() {
        return "Named [" + this.exprString + "]";
    }
}
