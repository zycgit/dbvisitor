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
import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import static net.hasor.dbvisitor.internal.OgnlUtils.evalOgnl;

/**
 * in 规则，用于自动生成 in 语句后的多重参数，例如： where col in (?,?,?,?)。
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class InRule implements SqlBuildRule {
    public static final SqlBuildRule INSTANCE = new InRule(false);
    private final       boolean      usingIf;

    public InRule(boolean usingIf) {
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
    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String expr;
        if (this.usingIf) {
            expr = ruleValue != null ? ruleValue : "";
        } else {
            expr = (activeExpr != null ? activeExpr : "") + "," + (ruleValue != null ? ruleValue : "");
        }

        SqlBuilder builder = DynamicParsed.getParsedSql(expr).buildQuery(data, context);
        Object[] args = builder.getArgs();

        sqlBuilder.appendSql("in (");
        if (args != null && args.length != 0) {
            if (args.length > 1) {
                String inName = usingIf ? "IFIN" : "IN";
                throw new SQLException("role " + inName + " args error, require 1, but " + args.length);
            }
            if (args[0] != null) {
                buildIn(sqlBuilder, args[0]);
            }
        }
        sqlBuilder.appendSql(")");
    }

    private static void buildIn(final SqlBuilder sqlBuilder, final Object value) {
        Object tmpValue = value;
        Integer jdbcType = null;
        TypeHandler<?> typeHandler = null;
        if (tmpValue instanceof SqlArg) {
            tmpValue = ((SqlArg) value).getValue();
            jdbcType = ((SqlArg) value).getJdbcType();
            typeHandler = ((SqlArg) value).getTypeHandler();
        }
        if (tmpValue != null && tmpValue.getClass().isArray()) {
            tmpValue = Arrays.asList(ArraySqlArgSource.toArgs(tmpValue));
        }

        //
        if (tmpValue instanceof Iterable) {
            Iterator<?> entryIter = ((Iterable<?>) tmpValue).iterator();
            int k = 0;
            while (entryIter.hasNext()) {
                if (k > 0) {
                    sqlBuilder.appendSql(", ");
                }
                k++;

                if (jdbcType != null || typeHandler != null) {
                    sqlBuilder.appendSql("?", new SqlArg(entryIter.next(), jdbcType, typeHandler));
                } else {
                    sqlBuilder.appendSql("?", entryIter.next());
                }

            }
        } else {
            sqlBuilder.appendSql("?", value);
        }
    }

    @Override
    public String toString() {
        return (this.usingIf ? "ifin [" : "in [") + this.hashCode() + "]";
    }
}
