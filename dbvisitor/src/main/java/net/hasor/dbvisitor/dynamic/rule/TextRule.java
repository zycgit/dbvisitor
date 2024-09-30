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
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import static net.hasor.dbvisitor.internal.OgnlUtils.evalOgnl;

/**
 * 动态参数规则，普通文本
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class TextRule implements SqlBuildRule {
    public static final SqlBuildRule INSTANCE = new TextRule(false);
    private final       boolean      usingIf;

    public TextRule(boolean usingIf) {
        this.usingIf = usingIf;
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
    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) {
        if (this.usingIf) {
            sqlBuilder.appendSql(ruleValue);
        } else {
            if (activeExpr != null) {
                sqlBuilder.appendSql(activeExpr);
                if (ruleValue != null) {
                    sqlBuilder.appendSql(",");
                }
            }

            if (ruleValue != null) {
                sqlBuilder.appendSql(ruleValue);
            }
        }
    }

    @Override
    public String toString() {
        return (this.usingIf ? "iftext [" : "text [") + this.hashCode() + "]";
    }
}
