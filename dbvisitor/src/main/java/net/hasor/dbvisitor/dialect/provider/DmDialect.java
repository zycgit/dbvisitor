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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;

/**
 * 达梦 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class DmDialect extends AbstractDialect implements PageSqlDialect, InsertSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/dm.keywords";
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
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sqlBuilder = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (start <= 0) {
            sqlBuilder.append(" LIMIT ?");
            paramArrays.add(limit);
        } else {
            sqlBuilder.append(" LIMIT ?, ?");
            paramArrays.add(start);
            paramArrays.add(limit);
        }

        return new BoundSql.BoundSqlObj(sqlBuilder.toString(), paramArrays.toArray());
    }

    @Override
    public boolean supportInto(List<String> primaryKey, List<String> columns) {
        return true;
    }

    @Override
    public String insertInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        return buildSql("INSERT INTO ", useQualifier, catalog, schema, table, columns, columnValueTerms);
    }

    @Override
    public boolean supportIgnore(List<String> primaryKey, List<String> columns) {
        return !primaryKey.isEmpty();
    }

    @Override
    public String insertIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        String ignoreHint = "/*+ IGNORE_ROW_ON_DUPKEY_INDEX(" + table + "(" + StringUtils.join(primaryKey.toArray(), ",") + ")) */ ";
        return buildSql("INSERT " + ignoreHint, useQualifier, catalog, schema, table, columns, columnValueTerms);
    }

    @Override
    public boolean supportReplace(List<String> primaryKey, List<String> columns) {
        return false;//!primaryKey.isEmpty();
    }

    @Override
    public String insertReplace(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns, Map<String, String> columnValueTerms) {
        throw new UnsupportedOperationException();
    }

    //
    //    @Override
    //    public String insertWithReplace(boolean useQualifier, String category, String tableName, List<FieldInfo> pkFields, List<FieldInfo> insertFields) {
    //        //        MERGE INTO DS_ENV TMP
    //        //        USING (SELECT 3            "ID",
    //        //                systimestamp GMT_CREATE,
    //        //                systimestamp GMT_MODIFIED,
    //        //                'abc'        OWNER_UID,
    //        //                'dev'        ENV_NAME,
    //        //                'dddddd'     DESCRIPTION
    //        //                FROM dual) SRC
    //        //        ON (TMP."ID" = SRC."ID")
    //        //        WHEN MATCHED THEN
    //        //                UPDATE
    //        //            SET "GMT_CREATE"   = SRC."GMT_CREATE",
    //        //                "GMT_MODIFIED" = SRC."GMT_MODIFIED",
    //        //                "OWNER_UID"    = SRC."OWNER_UID",
    //        //                "ENV_NAME"     = SRC."ENV_NAME",
    //        //                "DESCRIPTION"  = SRC."DESCRIPTION"
    //        List<FieldInfo> pkColumns = insertFields.stream().filter(FieldInfo::isPrimary).collect(Collectors.toList());
    //        StringBuilder mergeBasic = buildMergeInfoBasic(useQualifier, category, tableName, insertFields, pkColumns);
    //        StringBuilder mergeWhenMatched = buildMergeInfoWhenMatched(useQualifier, insertFields);
    //        return mergeBasic.toString() + " " + mergeWhenMatched.toString();
    //    }
    //
    //    private static StringBuilder buildMergeInfoBasic(boolean useQualifier, String category, String tableName, List<FieldInfo> allColumns, List<FieldInfo> pkColumns) {
    //        StringBuilder mergeBuilder = new StringBuilder();
    //        String finalTableName = fmtQualifier(useQualifier, category) + "." + fmtQualifier(useQualifier, tableName);
    //        mergeBuilder.append("MERGE INTO " + finalTableName + " TMP USING( SELECT ");
    //        for (int i = 0; i < allColumns.size(); i++) {
    //            FieldInfo fieldInfo = allColumns.get(i);
    //            if (i != 0) {
    //                mergeBuilder.append(",");
    //            }
    //            mergeBuilder.append("? " + fmtQualifier(useQualifier, fieldInfo.getColumnName()));
    //        }
    //        mergeBuilder.append(" FROM dual) SRC ON (");
    //        for (int i = 0; i < pkColumns.size(); i++) {
    //            if (i != 0) {
    //                mergeBuilder.append(" AND ");
    //            }
    //            String pkColumn = fmtQualifier(useQualifier, pkColumns.get(i).getColumnName());
    //            mergeBuilder.append("TMP." + pkColumn + " = SRC." + pkColumn);
    //        }
    //        mergeBuilder.append(") ");
    //        return mergeBuilder;
    //    }
    //
    //
    //    private static StringBuilder buildMergeInfoWhenMatched(boolean useQualifier, List<FieldInfo> allColumns) {
    //        StringBuilder mergeBuilder = new StringBuilder();
    //        mergeBuilder.append("WHEN MATCHED THEN ");
    //        mergeBuilder.append("UPDATE SET ");
    //        for (int i = 0; i < allColumns.size(); i++) {
    //            FieldInfo column = allColumns.get(i);
    //            if (i != 0) {
    //                mergeBuilder.append(",");
    //            }
    //            String columnName = fmtQualifier(useQualifier, column.getColumnName());
    //            mergeBuilder.append(columnName + " = SRC." + columnName);
    //        }
    //        mergeBuilder.append(" ");
    //        return mergeBuilder;
    //    }

    protected String buildSql(String markString, boolean useQualifier, String catalog, String schema, String table, List<String> columns, Map<String, String> columnValueTerms) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(markString);
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
}
