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
import java.util.Iterator;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.args.StackSqlArgSource;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.types.SqlArg;

/**
 * 处理 Bean/Map/List/Array 对象，将其以 “键值对” 形式参数化，并生成对应的 SQL 片段。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-11-12
 */
public class PairsRule implements SqlRule {
    public static final SqlRule INSTANCE = new PairsRule();

    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        return true;
    }

    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        SqlBuilder builder = DynamicParsed.getParsedSql(activeExpr).buildQuery(data, context);
        Object[] args = builder.getArgs();
        if (args.length != 1) {
            throw new SQLException("role PAIRS args error, require 1, but " + args.length);
        }

        if (args[0] == null) {
            return;
        }

        PlanDynamicSql pairTemplate = DynamicParsed.getParsedSql(ruleValue);
        Object argValue = args[0] instanceof SqlArg ? ((SqlArg) args[0]).getValue() : args[0];
        if (argValue instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) argValue;
            int index = 0;
            for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                this.buildPair(sqlBuilder, pairTemplate, data, context, entry.getKey(), entry.getValue(), index++);
            }
        } else if (argValue instanceof Iterable<?>) {
            Iterable<?> it = (Iterable<?>) argValue;
            Iterator<?> iterator = it.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                int keyIndex = index++;
                Object value = iterator.next();
                this.buildPair(sqlBuilder, pairTemplate, data, context, String.valueOf(keyIndex), value, keyIndex);
            }
        } else if (argValue.getClass().isArray()) {
            Object[] array = (Object[]) argValue;
            int index = 0;
            for (Object obj : array) {
                int keyIndex = index++;
                this.buildPair(sqlBuilder, pairTemplate, data, context, String.valueOf(keyIndex), obj, keyIndex);
            }
        } else {
            throw new SQLException("role PAIRS require Map type parameter, but " + argValue.getClass().getName());
        }
    }

    private void buildPair(SqlBuilder sqlBuilder, PlanDynamicSql pairTemplate, SqlArgSource data, QueryContext context, Object k, Object v, int i) throws SQLException {
        StackSqlArgSource tmpSource = new StackSqlArgSource(data);
        tmpSource.putValue("k", k);
        tmpSource.putValue("v", v);
        tmpSource.putValue("i", i);

        SqlBuilder tmp = pairTemplate.buildQuery(tmpSource, context);
        if (!sqlBuilder.lastSpaceCharacter()) {
            sqlBuilder.appendSql(" ");
            sqlBuilder.appendBuilder(tmp);
        } else {
            sqlBuilder.appendBuilder(tmp);
        }
    }

    @Override
    public String toString() {
        return "pairs [" + this.hashCode() + "]";
    }
}
