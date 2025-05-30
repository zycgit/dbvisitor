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
package net.hasor.dbvisitor.dialect;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.provider.AbstractDialect;

import java.util.List;
import java.util.Map;

/**
 * 默认 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class DefaultSqlDialect extends AbstractDialect implements ConditionSqlDialect, PageSqlDialect, InsertSqlDialect {
    public static final DefaultSqlDialect DEFAULT = new DefaultSqlDialect();

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("INSERT INTO ");
        strBuilder.append(tableName(useQualifier, catalog, schema, table));
        strBuilder.append(" ");
        strBuilder.append("(");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                strBuilder.append(", ");
                argBuilder.append(", ");
            }

            strBuilder.append(fmtName(useQualifier, colName));
            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            if (StringUtils.isNotBlank(valueTerm)) {
                argBuilder.append(valueTerm);
            } else {
                argBuilder.append("?");
            }
        }

        strBuilder.append(") VALUES (");
        strBuilder.append(argBuilder);
        strBuilder.append(")");
        return strBuilder.toString();
    }

    @Override
    public boolean supportIgnore(List<String> primaryKey, List<String> columns) {
        return false;
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return false;
    }

    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        throw new UnsupportedOperationException();
    }
}
