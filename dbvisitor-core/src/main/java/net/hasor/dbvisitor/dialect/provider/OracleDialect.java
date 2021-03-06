/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.dbvisitor.dialect.provider;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Oracle 的 SqlDialect 实现
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class OracleDialect extends AbstractDialect implements PageSqlDialect, InsertSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/oracle.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        String sqlBuilder = "SELECT COUNT(*) FROM (" + boundSql.getSqlString() + ") TEMP_T";
        return new BoundSql.BoundSqlObj(sqlBuilder, boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, int start, int limit) {
        String sqlString = boundSql.getSqlString();
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( ");
        sqlBuilder.append(sqlString);
        sqlBuilder.append(" ) TMP WHERE ROWNUM <= ? ) WHERE ROW_ID > ?");

        paramArrays.add(start + limit);
        paramArrays.add(start);
        return new BoundSql.BoundSqlObj(sqlBuilder.toString(), paramArrays.toArray());
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
        strBuilder.append(" (");

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
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertWithIgnore(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        StringBuilder mergeBuilder = new StringBuilder();

        buildMergeInfoBasic(useQualifier, schema, table, primaryKey, columns, mergeBuilder);

        buildMergeInfoWhenNotMatched(useQualifier, schema, table, columns, mergeBuilder);

        return mergeBuilder.toString();
    }

    @Override
    public boolean supportUpsert(List<String> primaryKey, List<String> columns) {
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertWithUpsert(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns) {
        StringBuilder mergeBuilder = new StringBuilder();

        buildMergeInfoBasic(useQualifier, schema, table, primaryKey, columns, mergeBuilder);

        buildMergeInfoWhenMatched(useQualifier, schema, table, columns, mergeBuilder);
        buildMergeInfoWhenNotMatched(useQualifier, schema, table, columns, mergeBuilder);

        return mergeBuilder.toString();
    }

    private void buildMergeInfoBasic(boolean useQualifier, String schema, String table, List<String> primaryKey, List<String> columns, StringBuilder mergeBuilder) {
        mergeBuilder.append("MERGE INTO ");
        mergeBuilder.append(tableName(useQualifier, schema, table));
        mergeBuilder.append(" TMP USING (SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                mergeBuilder.append(", ");
            }

            mergeBuilder.append("? ");
            mergeBuilder.append(columnName(useQualifier, schema, table, columns.get(i)));
        }

        mergeBuilder.append(" FROM dual ) SRC ON (");
        for (int i = 0; i < primaryKey.size(); i++) {
            if (i != 0) {
                mergeBuilder.append(" AND ");
            }
            String pkColumn = columnName(useQualifier, schema, table, primaryKey.get(i));
            mergeBuilder.append("TMP." + pkColumn + " = SRC." + pkColumn);
        }
        mergeBuilder.append(") ");
    }

    private void buildMergeInfoWhenNotMatched(boolean useQualifier, String schema, String table, List<String> allColumns, StringBuilder mergeBuilder) {
        mergeBuilder.append("WHEN NOT MATCHED THEN ");
        mergeBuilder.append("INSERT (");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < allColumns.size(); i++) {
            if (i > 0) {
                mergeBuilder.append(", ");
                argBuilder.append(", ");
            }
            mergeBuilder.append(columnName(useQualifier, schema, table, allColumns.get(i)));
            argBuilder.append("SRC.").append(columnName(useQualifier, schema, table, allColumns.get(i)));
        }

        mergeBuilder.append(") VALUES( ");
        mergeBuilder.append(argBuilder);
        mergeBuilder.append(") ");
    }

    private void buildMergeInfoWhenMatched(boolean useQualifier, String schema, String table, List<String> allColumns, StringBuilder mergeBuilder) {
        mergeBuilder.append("WHEN MATCHED THEN ");
        mergeBuilder.append("UPDATE SET ");
        for (int i = 0; i < allColumns.size(); i++) {
            String column = allColumns.get(i);
            if (i != 0) {
                mergeBuilder.append(", ");
            }
            mergeBuilder.append(columnName(useQualifier, schema, table, column));
            mergeBuilder.append(" = SRC.");
            mergeBuilder.append(columnName(useQualifier, schema, table, column));
        }
        mergeBuilder.append(" ");
    }

}
