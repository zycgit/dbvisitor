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
package net.hasor.dbvisitor.dialect.provider;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Oracle 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
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
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        boolean catalogBlank = StringUtils.isBlank(catalog);
        boolean schemaBlank = StringUtils.isBlank(schema);

        if (!catalogBlank && !schemaBlank) {
            return fmtName(useQualifier, catalog) + "." + fmtName(useQualifier, table);
        }
        if (!catalogBlank) {
            return fmtName(useQualifier, catalog) + "." + fmtName(useQualifier, table);
        }
        if (!schemaBlank) {
            return fmtName(useQualifier, schema) + "." + fmtName(useQualifier, table);
        }
        return fmtName(useQualifier, table);
    }

    public String like(SqlLike likeType, Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "%";
        }
        switch (likeType) {
            case LEFT:
                return "CONCAT('%', ? )";
            case RIGHT:
                return "CONCAT( ? ,'%')";
            default:
                return "CONCAT(CONCAT('%', ? ) ,'%')";
        }
    }

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        String sqlBuilder = "SELECT COUNT(*) FROM (" + boundSql.getSqlString() + ") TEMP_T";
        return new BoundSql.BoundSqlObj(sqlBuilder, boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
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
    public boolean supportInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("INSERT INTO ");
        strBuilder.append(tableName(useQualifier, catalog, schema, table));
        strBuilder.append(" (");

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
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder mergeBuilder = new StringBuilder();

        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, mergeBuilder);

        buildMergeInfoWhenNotMatched(useQualifier, catalog, schema, table, columns, mergeBuilder);

        return mergeBuilder.toString();
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder mergeBuilder = new StringBuilder();

        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, mergeBuilder);

        buildMergeInfoWhenMatched(useQualifier, catalog, schema, table, primaryKey, columns, mergeBuilder);
        buildMergeInfoWhenNotMatched(useQualifier, catalog, schema, table, columns, mergeBuilder);

        return mergeBuilder.toString();
    }

    private void buildMergeInfoBasic(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms, StringBuilder mergeBuilder) {
        mergeBuilder.append("MERGE INTO ");
        mergeBuilder.append(tableName(useQualifier, catalog, schema, table));
        mergeBuilder.append(" TMP USING (SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                mergeBuilder.append(", ");
            }

            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            if (StringUtils.isNotBlank(valueTerm)) {
                mergeBuilder.append(valueTerm).append(" ");
            } else {
                mergeBuilder.append("?").append(" ");
            }
            mergeBuilder.append(fmtName(useQualifier, columns.get(i)));
        }

        mergeBuilder.append(" FROM dual) SRC ON (");
        for (int i = 0; i < primaryKey.size(); i++) {
            if (i != 0) {
                mergeBuilder.append(" AND ");
            }
            String pkColumn = fmtName(useQualifier, primaryKey.get(i));
            mergeBuilder.append("TMP." + pkColumn + " = SRC." + pkColumn);
        }
        mergeBuilder.append(") ");
    }

    private void buildMergeInfoWhenNotMatched(boolean useQualifier, String catalog, String schema, String table, List<String> allColumns, StringBuilder mergeBuilder) {
        mergeBuilder.append("WHEN NOT MATCHED THEN ");
        mergeBuilder.append("INSERT (");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < allColumns.size(); i++) {
            if (i > 0) {
                mergeBuilder.append(", ");
                argBuilder.append(", ");
            }
            mergeBuilder.append(fmtName(useQualifier, allColumns.get(i)));
            argBuilder.append("SRC.").append(fmtName(useQualifier, allColumns.get(i)));
        }

        mergeBuilder.append(") VALUES ( ");
        mergeBuilder.append(argBuilder);
        mergeBuilder.append(")");
    }

    private void buildMergeInfoWhenMatched(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> allColumns, StringBuilder mergeBuilder) {
        mergeBuilder.append("WHEN MATCHED THEN ");
        mergeBuilder.append("UPDATE SET ");
        List<String> updateColumns = allColumns.stream().filter(c -> !primaryKey.contains(c)).collect(Collectors.toList());
        for (int i = 0; i < updateColumns.size(); i++) {
            String column = updateColumns.get(i);
            if (i != 0) {
                mergeBuilder.append(", ");
            }
            mergeBuilder.append(fmtName(useQualifier, column));
            mergeBuilder.append(" = SRC.");
            mergeBuilder.append(fmtName(useQualifier, column));
        }
        mergeBuilder.append(" ");
    }

    public boolean supportBatch() {
        return false;
    }
}
