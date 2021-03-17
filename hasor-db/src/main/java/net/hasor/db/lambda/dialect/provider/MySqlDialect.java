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
package net.hasor.db.lambda.dialect.provider;
import net.hasor.db.lambda.dialect.BoundSql;
import net.hasor.db.lambda.dialect.BoundSql.BoundSqlObj;
import net.hasor.db.lambda.dialect.InsertSqlDialect;
import net.hasor.db.lambda.dialect.SqlDialect;
import net.hasor.db.lambda.mapping.FieldInfo;
import net.hasor.utils.StringUtils;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MySQL 的 SqlDialect 实现
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlDialect implements SqlDialect, InsertSqlDialect {
    @Override
    public String tableName(boolean useQualifier, String category, String tableName) {
        String qualifier = useQualifier ? "`" : "";
        if (StringUtils.isBlank(category)) {
            return qualifier + tableName + qualifier;
        } else {
            return qualifier + category + qualifier + "." + qualifier + tableName + qualifier;
        }
    }

    @Override
    public String columnName(boolean useQualifier, String category, String tableName, String columnName, JDBCType jdbcType, Class<?> javaType) {
        String qualifier = useQualifier ? "`" : "";
        return qualifier + columnName + qualifier;
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, int start, int limit) {
        StringBuilder sqlBuilder = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));
        //
        if (start <= 0) {
            sqlBuilder.append(" LIMIT ?");
            paramArrays.add(limit);
        } else {
            sqlBuilder.append(" LIMIT ?, ?");
            paramArrays.add(start);
            paramArrays.add(limit);
        }
        //
        return new BoundSqlObj(sqlBuilder.toString(), paramArrays.toArray());
    }

    @Override
    public boolean supportInsertInto() {
        return true;
    }

    @Override
    public String insertWithInto(boolean useQualifier, String category, String tableName, List<FieldInfo> pkFields, List<FieldInfo> insertFields) {
        // insert into t(id, name) values (?, ?);
        String allColumns = buildAllColumns(useQualifier, category, tableName, insertFields);
        int fieldCount = insertFields.size();
        return "INSERT INTO " + tableName(useQualifier, category, tableName) + "(" + allColumns + ") VALUES (" + StringUtils.repeat(",?", fieldCount).substring(1) + ")";
    }

    @Override
    public boolean supportInsertIgnore() {
        return true;
    }

    @Override
    public String insertWithIgnore(boolean useQualifier, String category, String tableName, List<FieldInfo> pkFields, List<FieldInfo> insertFields) {
        // insert ignore t(id, name) values (?, ?);
        String allColumns = buildAllColumns(useQualifier, category, tableName, insertFields);
        int fieldCount = insertFields.size();
        return "INSERT IGNORE " + tableName(useQualifier, category, tableName) + "(" + allColumns + ") VALUES (" + StringUtils.repeat(",?", fieldCount).substring(1) + ")";
    }

    @Override
    public boolean supportInsertReplace() {
        return true;
    }

    @Override
    public String insertWithReplace(boolean useQualifier, String category, String tableName, List<FieldInfo> pkFields, List<FieldInfo> insertFields) {
        // replace into t(id, name) values (?, ?);
        String allColumns = buildAllColumns(useQualifier, category, tableName, insertFields);
        int fieldCount = insertFields.size();
        return "REPLACE INTO " + tableName(useQualifier, category, tableName) + "(" + allColumns + ") VALUES (" + StringUtils.repeat(",?", fieldCount).substring(1) + ")";
    }

    private String buildAllColumns(boolean useQualifier, String category, String tableName, List<FieldInfo> insertFields) {
        return insertFields.stream().map(fieldInfo -> {
            return columnName(useQualifier, category, tableName, fieldInfo.getColumnName(), fieldInfo.getJdbcType(), fieldInfo.getJavaType());
        }).reduce((s1, s2) -> {
            return s1 + "." + s2;
        }).orElse("");
    }
}
