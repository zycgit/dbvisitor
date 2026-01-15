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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * Oracle 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class OracleDialect extends AbstractSqlDialect implements PageSqlDialect, InsertSqlDialect {
    public static final SqlDialect DEFAULT = new OracleDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new OracleDialect();
    }

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

    @Override
    public String like(SqlLike likeType, Object value, String valueTerm) {
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
    public boolean supportBatch() {
        return false;
    }

    @Override
    public boolean supportGroupByAlias() {
        return true; // oracle 12 开始支持
    }

    @Override
    public boolean supportOrderByAlias() {
        return true; // oracle 12 开始支持
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        String s = "SELECT COUNT(*) FROM (" + boundSql.getSqlString() + ") TEMP_T";
        return new BoundSql.BoundSqlObj(s, boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        String sqlString = boundSql.getSqlString();
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( ");
        sb.append(sqlString);
        sb.append(" ) TMP WHERE ROWNUM <= ? ) WHERE ROW_ID > ?");

        paramArrays.add(start + limit);
        paramArrays.add(start);
        return new BoundSql.BoundSqlObj(sb.toString(), paramArrays.toArray());
    }

    // --- InsertSqlDialect impl ---

    @Override
    public boolean supportInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" (");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                sb.append(", ");
                argBuilder.append(", ");
            }

            sb.append(fmtName(useQualifier, colName));
            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            if (StringUtils.isNotBlank(valueTerm)) {
                argBuilder.append(valueTerm);
            } else {
                argBuilder.append("?");
            }
        }

        sb.append(") VALUES (");
        sb.append(argBuilder);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean supportIgnore(List<String> primaryKey, List<String> columns) {
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();

        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, sb);

        buildMergeInfoWhenNotMatched(useQualifier, catalog, schema, table, columns, sb);

        return sb.toString();
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder sb = new StringBuilder();

        buildMergeInfoBasic(useQualifier, catalog, schema, table, primaryKey, columns, columnValueTerms, sb);

        buildMergeInfoWhenMatched(useQualifier, catalog, schema, table, primaryKey, columns, sb);
        buildMergeInfoWhenNotMatched(useQualifier, catalog, schema, table, columns, sb);

        return sb.toString();
    }

    private void buildMergeInfoBasic(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms, StringBuilder sb) {
        sb.append("MERGE INTO ");
        sb.append(tableName(useQualifier, catalog, schema, table));
        sb.append(" TMP USING (SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            if (i > 0) {
                sb.append(", ");
            }

            String valueTerm = columnValueTerms != null ? columnValueTerms.get(colName) : null;
            if (StringUtils.isNotBlank(valueTerm)) {
                sb.append(valueTerm).append(" ");
            } else {
                sb.append("?").append(" ");
            }
            sb.append(fmtName(useQualifier, columns.get(i)));
        }

        sb.append(" FROM dual) SRC ON (");
        for (int i = 0; i < primaryKey.size(); i++) {
            if (i != 0) {
                sb.append(" AND ");
            }
            String pkColumn = fmtName(useQualifier, primaryKey.get(i));
            sb.append("TMP." + pkColumn + " = SRC." + pkColumn);
        }
        sb.append(") ");
    }

    private void buildMergeInfoWhenNotMatched(boolean useQualifier, String catalog, String schema, String table, List<String> allColumns, StringBuilder sb) {
        sb.append("WHEN NOT MATCHED THEN ");
        sb.append("INSERT (");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < allColumns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
                argBuilder.append(", ");
            }
            sb.append(fmtName(useQualifier, allColumns.get(i)));
            argBuilder.append("SRC.").append(fmtName(useQualifier, allColumns.get(i)));
        }

        sb.append(") VALUES ( ");
        sb.append(argBuilder);
        sb.append(")");
    }

    private void buildMergeInfoWhenMatched(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> allColumns, StringBuilder sb) {
        sb.append("WHEN MATCHED THEN ");
        sb.append("UPDATE SET ");
        List<String> updateColumns = allColumns.stream().filter(c -> !primaryKey.contains(c)).collect(Collectors.toList());
        for (int i = 0; i < updateColumns.size(); i++) {
            String column = updateColumns.get(i);
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(fmtName(useQualifier, column));
            sb.append(" = SRC.");
            sb.append(fmtName(useQualifier, column));
        }
        sb.append(" ");
    }
}