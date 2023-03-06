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
package net.hasor.dbvisitor.mapping.generate;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.mapping.def.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 SQL 语法的建表语句生成器
 * @version : 2023-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class SqlTableGenerate implements SchemaGenerate {
    protected static final ColumnDescription EMPTY = new ColumnDescDef();
    protected              SqlDialect        dialect;

    public SqlTableGenerate(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public List<String> buildCreate(TableMapping<?> tableMapping) {
        List<String> beforeScripts = new ArrayList<>();
        StringBuilder scriptBuild = new StringBuilder();
        List<String> afterScripts = new ArrayList<>();

        String catalog = tableMapping.getCatalog();
        String schema = tableMapping.getSchema();
        String table = tableMapping.getTable();
        boolean delimited = tableMapping.useDelimited();

        beforeTable(beforeScripts, scriptBuild, afterScripts, tableMapping);
        scriptBuild.append("CREATE TABLE ").append(dialect.tableName(delimited, catalog, schema, table));
        scriptBuild.append("(");

        // columns
        int i = 0;
        List<String> pkColumns = new ArrayList<>();
        for (ColumnMapping colMapping : tableMapping.getProperties()) {
            if (i != 0) {
                scriptBuild.append(", ");
            }
            beforeColum(beforeScripts, scriptBuild, afterScripts, tableMapping, colMapping);
            boolean finish = buildColumn(beforeScripts, scriptBuild, afterScripts, tableMapping, colMapping);
            if (i != 0 && !finish) {
                int length = scriptBuild.length();
                scriptBuild.delete(length - 2, length);
            }
            afterColum(beforeScripts, scriptBuild, afterScripts, tableMapping, colMapping);
            if (colMapping.isPrimaryKey()) {
                pkColumns.add(colMapping.getColumn());
            }
            i++;
        }

        // PK
        if (!pkColumns.isEmpty()) {
            scriptBuild.append(", ");
            boolean finish = buildPrimaryKey(beforeScripts, scriptBuild, afterScripts, tableMapping, pkColumns);
            if (!finish) {
                int length = scriptBuild.length();
                scriptBuild.delete(length - 2, length);
            }
        }

        // UNIQUE
        for (IndexDescription index : tableMapping.getIndexes()) {
            if (index.isUnique()) {
                scriptBuild.append(", ");
                boolean finish = buildUnique(beforeScripts, scriptBuild, afterScripts, tableMapping, index);
                if (!finish) {
                    int length = scriptBuild.length();
                    scriptBuild.delete(length - 2, length);
                }
            }
        }

        // INDEX
        for (IndexDescription index : tableMapping.getIndexes()) {
            if (!index.isUnique()) {
                scriptBuild.append(", ");
                boolean finish = buildIndex(beforeScripts, scriptBuild, afterScripts, tableMapping, index);
                if (!finish) {
                    int length = scriptBuild.length();
                    scriptBuild.delete(length - 2, length);
                }
            }
        }

        scriptBuild.append(")");
        afterTable(beforeScripts, scriptBuild, afterScripts, tableMapping);

        ArrayList<String> finalScript = new ArrayList<>();
        finalScript.addAll(beforeScripts);
        finalScript.add(scriptBuild.toString());
        finalScript.addAll(afterScripts);
        return finalScript;
    }

    protected boolean buildColumn(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, ColumnMapping colMapping) {
        ColumnDescription description = colMapping.getDescription();
        boolean nullable = description == null || description.isNullable();
        String defaultValue = description == null ? null : description.getDefault();
        String sqlType = description == null ? null : description.getSqlType();

        if (StringUtils.isBlank(sqlType)) {
            sqlType = typeBuild(colMapping.getJavaType(), description);
        }

        scriptBuild.append(dialect.fmtName(tableMapping.useDelimited(), colMapping.getColumn()));
        scriptBuild.append(" ").append(sqlType);
        scriptBuild.append(nullable ? " NULL" : " NOT NULL");
        scriptBuild.append(StringUtils.isBlank(defaultValue) ? "" : (" DEFAULT " + buildDefault(sqlType, defaultValue)));
        return true;
    }

    protected boolean buildPrimaryKey(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, List<String> pkColumns) {
        boolean delimited = tableMapping.useDelimited();

        scriptBuild.append("PRIMARY KEY(");
        for (int i = 0; i < pkColumns.size(); i++) {
            String column = pkColumns.get(i);
            if (i > 0) {
                scriptBuild.append(", ");
            }
            scriptBuild.append(dialect.fmtName(delimited, column));
        }
        scriptBuild.append(")");
        return true;
    }

    protected boolean buildUnique(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, IndexDescription index) {
        String name = index.getName();
        boolean delimited = tableMapping.useDelimited();

        if (StringUtils.isNotBlank(name)) {
            scriptBuild.append("CONSTRAINT " + dialect.fmtName(delimited, name) + " UNIQUE (");
        } else {
            scriptBuild.append("UNIQUE (");
        }

        List<String> ukColumns = index.getColumns();
        for (int i = 0; i < ukColumns.size(); i++) {
            String column = ukColumns.get(i);
            if (i > 0) {
                scriptBuild.append(", ");
            }
            scriptBuild.append(dialect.fmtName(delimited, column));
        }
        scriptBuild.append(")");
        return true;
    }

    protected boolean buildIndex(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, IndexDescription index) {
        return false;
    }

    protected void beforeTable(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping) {
    }

    protected void beforeColum(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, ColumnMapping colMapping) {
    }

    protected void afterColum(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping, ColumnMapping colMapping) {
    }

    protected void afterTable(List<String> beforeScripts, StringBuilder scriptBuild, List<String> afterScripts,//
            TableMapping<?> tableMapping) {
    }

    protected abstract String typeBuild(Class<?> javaType, ColumnDescription description);

    protected abstract String buildDefault(String sqlType, String defaultValue);
}