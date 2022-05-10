/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.dbvisitor.dialect;
import net.hasor.cobble.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 默认 SqlDialect 实现
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class DefaultSqlDialect implements ConditionSqlDialect, PageSqlDialect, InsertSqlDialect {
    public static final DefaultSqlDialect DEFAULT = new DefaultSqlDialect();

    @Override
    public Set<String> keywords() {
        return Collections.emptySet();
    }

    @Override
    public String leftQualifier() {
        return "";
    }

    @Override
    public String rightQualifier() {
        return "";
    }

    @Override
    public String tableName(boolean useQualifier, String schema, String table) {
        if (StringUtils.isBlank(schema)) {
            return table;
        } else {
            return schema + "." + table;
        }
    }

    @Override
    public String columnName(boolean useQualifier, String schema, String table, String column) {
        return column;
    }

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, int start, int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportInsertInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertWithInto(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("INSERT INTO ");
        strBuilder.append(tableName(useQualifier, schema, table));
        strBuilder.append(" ");
        strBuilder.append("(");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                strBuilder.append(", ");
                argBuilder.append(", ");
            }
            strBuilder.append(columnName(useQualifier, schema, table, columns.get(i)));
            argBuilder.append("?");
        }

        strBuilder.append(") VALUES (");
        strBuilder.append(argBuilder);
        strBuilder.append(")");
        return strBuilder.toString();
    }

    @Override
    public boolean supportInsertIgnore(List<String> primaryKey, List<String> columns) {
        return false;
    }

    @Override
    public String insertWithIgnore(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportUpsert(List<String> primaryKey, List<String> columns) {
        return false;
    }

    @Override
    public String insertWithUpsert(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        throw new UnsupportedOperationException();
    }

}
