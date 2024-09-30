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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;

import java.sql.SQLException;

import static net.hasor.dbvisitor.internal.OgnlUtils.evalOgnl;

/**
 * 如果参数不为空，则生成 'and column = ?' 或者 'column = ?' 。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public abstract class ConditionRule implements SqlBuildRule {
    protected static final String[] DEFAULT_TEST_PREFIX = new String[] { "where", ",", "and", "or", "not", "!" };
    private final          String[] testPrefix;
    private final          String   mustHave;
    private final          String   mustHaveAppend;
    private final          String   append;
    protected final        boolean  usingIf;

    protected ConditionRule(boolean usingIf, String[] testPrefix, String mustHave, String mustHaveAppend, String append) {
        this.usingIf = usingIf;
        this.testPrefix = testPrefix;
        this.mustHave = mustHave;
        this.mustHaveAppend = mustHaveAppend;
        this.append = append;
    }

    @Override
    public boolean test(SqlArgSource data, DynamicContext context, String activeExpr) {
        if (this.usingIf) {
            return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(evalOgnl(activeExpr, data));
        } else {
            return true;
        }
    }

    @Override
    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String expr = "";
        if (this.usingIf) {
            expr = (StringUtils.isBlank(ruleValue) ? "" : ruleValue);
        } else {
            if (activeExpr != null) {
                expr += activeExpr;
                if (ruleValue != null) {
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

        DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(expr);
        String sql = sqlBuilder.getSqlString().toLowerCase();
        if (this.mustHave != null) {
            if (!sql.contains(this.mustHave)) {
                sqlBuilder.appendSql(this.mustHaveAppend);
                sql = sql + this.mustHaveAppend;
            }
        }

        for (String test : this.testPrefix) {
            if (sql.trim().endsWith(test)) {
                parsedSql.buildQuery(data, context, sqlBuilder);
                return;
            }
        }

        sqlBuilder.appendSql(this.append);
        parsedSql.buildQuery(data, context, sqlBuilder);
    }
}
