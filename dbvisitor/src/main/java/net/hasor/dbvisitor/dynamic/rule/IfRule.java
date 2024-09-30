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

import java.sql.SQLException;

import static net.hasor.dbvisitor.internal.OgnlUtils.evalOgnl;

/**
 * 如果参数不为空，则生成 'column = ?'。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class IfRule implements SqlBuildRule {
    public static final SqlBuildRule INSTANCE_IF = new IfRule();

    @Override
    public boolean test(SqlArgSource data, DynamicContext context, String activeExpr) {
        return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(evalOgnl(activeExpr, data));
    }

    @Override
    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        if (ruleValue != null) {
            DynamicParsed.getParsedSql(ruleValue).buildQuery(data, context, sqlBuilder);
        }
    }

    @Override
    public String toString() {
        return "if [" + this.hashCode() + "]";
    }
}
