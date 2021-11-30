/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.dynamic.rule;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.SqlArg;
import net.hasor.db.dal.dynamic.SqlMode;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.db.jdbc.core.ParsedSql;
import net.hasor.db.jdbc.paramer.MapSqlParameterSource;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 如果参数不为空，则生成 'and column = ?' 或者 'column = ?' 。
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class ConditionRule implements SqlBuildRule {
    private static final TypeHandler<?> stringTypeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    private final        String         prefix;

    protected ConditionRule(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean test(Map<String, Object> data, DynamicContext context, String activeExpr) {
        return true;
    }

    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        ParsedSql parsedSql = ParsedSql.getParsedSql(activeExpr);
        String buildSql = parsedSql.buildSql(new MapSqlParameterSource(data));
        Object[] objects = parsedSql.buildValues(new MapSqlParameterSource(data));

        List<SqlArg> argList = new ArrayList<>(objects.length);
        boolean needExit = true;
        for (Object argData : objects) {
            SqlArg sqlArg = null;
            if (argData == null) {
                sqlArg = new SqlArg(ruleValue, null, SqlMode.In, Types.NULL, String.class, stringTypeHandler);
            } else {
                Class<?> argType = argData.getClass();
                int sqlType = TypeHandlerRegistry.toSqlType(argType);
                TypeHandler<?> typeHandler = context.getTypeRegistry().getTypeHandler(argType);
                sqlArg = new SqlArg("", argData, SqlMode.In, sqlType, argType, typeHandler);
                needExit = false;
            }
            argList.add(sqlArg);
        }

        if (needExit) {
            return;
        }

        String sql = sqlBuilder.getSqlString().toLowerCase();
        if (sql.contains("where")) {
            if (sql.trim().endsWith("where") || sql.trim().endsWith("and") || sql.trim().endsWith("or")) {
                sqlBuilder.appendSql(buildSql, argList.toArray());
            } else {
                sqlBuilder.appendSql(this.prefix + " " + buildSql, argList.toArray());
            }
        } else {
            sqlBuilder.appendSql("where " + buildSql, argList.toArray());
        }
    }

}
