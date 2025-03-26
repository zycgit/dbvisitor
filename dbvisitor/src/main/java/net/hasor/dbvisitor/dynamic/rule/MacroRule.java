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
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.SQLException;

import static net.hasor.dbvisitor.internal.OgnlUtils.evalOgnl;

/**
 * 效果和使用 `<include refid="sqlid"/>` 标签相同
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class MacroRule implements SqlRule {
    public static final SqlRule INSTANCE = new MacroRule(false);
    private final       boolean usingIf;

    public MacroRule(boolean usingIf) {
        this.usingIf = usingIf;
    }

    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        if (this.usingIf) {
            return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(evalOgnl(activeExpr, data));
        } else {
            return true;
        }
    }

    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String name;
        if (this.usingIf) {
            name = ruleValue != null ? ruleValue.trim() : null;
        } else {
            name = activeExpr != null ? activeExpr.trim() : null;
        }

        DynamicSql macro = context.findMacro(name);
        if (macro == null) {
            String macroName = usingIf ? "ifmacro" : "macro";
            throw new SQLException(macroName + " '" + name + "' not found.");
        } else {
            macro.buildQuery(data, context, sqlBuilder);
        }
    }

    @Override
    public String toString() {
        return (this.usingIf ? "ifmacro [" : "macro [") + this.hashCode() + "]";
    }
}
