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
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.jdbc.SqlParameter;
import net.hasor.dbvisitor.types.MappedArg;

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

    private static void buildIn(SqlBuilder sqlBuilder, Object value) {
        // unwrap
        if (value instanceof SqlParameter.InSqlParameter) {
            value = ((SqlParameter.InSqlParameter) value).getValue();
        }
        if (value instanceof MappedArg) {
            value = ((MappedArg) value).getValue();
        }
        if (value != null && value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType == char.class) {
                value = Arrays.asList(ArrayUtils.toObject((char[]) value));
            } else if (componentType == Character.class) {
                value = Arrays.asList((Character[]) value);
            } else if (componentType == short.class) {
                value = Arrays.asList(ArrayUtils.toObject((short[]) value));
            } else if (componentType == Short.class) {
                value = Arrays.asList((Short[]) value);
            } else if (componentType == int.class) {
                value = Arrays.asList(ArrayUtils.toObject((int[]) value));
            } else if (componentType == Integer.class) {
                value = Arrays.asList((Integer[]) value);
            } else if (componentType == long.class) {
                value = Arrays.asList(ArrayUtils.toObject((long[]) value));
            } else if (componentType == Long.class) {
                value = Arrays.asList((Long[]) value);
            } else if (componentType == float.class) {
                value = Arrays.asList(ArrayUtils.toObject((float[]) value));
            } else if (componentType == Float.class) {
                value = Arrays.asList((Float[]) value);
            } else if (componentType == double.class) {
                value = Arrays.asList(ArrayUtils.toObject((double[]) value));
            } else if (componentType == Double.class) {
                value = Arrays.asList((Double[]) value);
            } else if (componentType == boolean.class) {
                value = Arrays.asList(ArrayUtils.toObject((boolean[]) value));
            } else if (componentType == Boolean.class) {
                value = Arrays.asList((Boolean[]) value);
            } else {
                value = Arrays.asList((Object[]) value);
            }
        }

        //
        if (value instanceof Iterable) {
            Iterator<?> entryIter = ((Iterable<?>) value).iterator();
            int k = 0;
            while (entryIter.hasNext()) {
                if (k > 0) {
                    sqlBuilder.appendSql(", ");
                }
                k++;

                sqlBuilder.appendSql("?", entryIter.next());
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
