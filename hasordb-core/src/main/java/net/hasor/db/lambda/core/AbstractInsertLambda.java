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
package net.hasor.db.lambda.core;
import net.hasor.db.dialect.BatchBoundSql;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.dialect.InsertSqlDialect;
import net.hasor.db.dialect.SqlDialect;
import net.hasor.db.lambda.DuplicateKeyStrategy;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提供 lambda insert 基础能力。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractInsertLambda<R, T, P> extends BasicLambda<R, T, P> implements InsertExecute<R, T> {
    protected final List<ColumnMapping>  insertProperties;
    protected final List<ColumnMapping>  primaryKeyProperties;
    protected final List<Object[]>       insertValues;
    protected       DuplicateKeyStrategy insertStrategy;

    protected final List<String> primaryKeys;
    protected final List<String> insertColumns;

    public AbstractInsertLambda(Class<?> exampleType, TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
        this.insertProperties = getInsertProperties();
        this.primaryKeyProperties = getPrimaryKeyColumns();
        this.insertValues = new ArrayList<>();
        this.insertStrategy = DuplicateKeyStrategy.Into;

        this.primaryKeys = this.primaryKeyProperties.parallelStream().map(ColumnMapping::getColumn).collect(Collectors.toList());
        this.insertColumns = this.insertProperties.parallelStream().map(ColumnMapping::getColumn).collect(Collectors.toList());
    }

    protected List<ColumnMapping> getInsertProperties() {
        TableMapping<?> tableMapping = this.getTableMapping();
        List<ColumnMapping> toInsertProperties = new ArrayList<>();
        Set<String> insertColumns = new HashSet<>();

        for (ColumnMapping mapping : tableMapping.getProperties()) {
            String columnName = mapping.getColumn();
            if (!mapping.isInsert()) {
                continue;
            }

            if (insertColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            } else {
                insertColumns.add(columnName);
                toInsertProperties.add(mapping);
            }
        }

        if (toInsertProperties.size() == 0) {
            throw new IllegalStateException("no column require INSERT.");
        }
        return toInsertProperties;
    }

    protected List<ColumnMapping> getPrimaryKeyColumns() {
        TableMapping<?> tableMapping = this.getTableMapping();

        List<ColumnMapping> pkProperties = new ArrayList<>();
        Set<String> pkColumns = new HashSet<>();
        for (ColumnMapping mapping : tableMapping.getProperties()) {
            String columnName = mapping.getColumn();
            if (!mapping.isPrimaryKey()) {
                continue;
            }

            if (pkColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            } else {
                pkColumns.add(columnName);
                pkProperties.add(mapping);
            }
        }
        return pkProperties;
    }

    @Override
    public R onDuplicateStrategy(DuplicateKeyStrategy insertStrategy) {
        this.insertStrategy = Objects.requireNonNull(insertStrategy);
        return this.getSelf();
    }

    @Override
    public R applyEntity(List<T> entityList) {
        int propertyCount = this.insertProperties.size();
        entityList.parallelStream().map(entity -> {
            Object[] args = new Object[propertyCount];
            for (int i = 0; i < propertyCount; i++) {
                ColumnMapping mapping = insertProperties.get(i);
                if (exampleIsMap()) {
                    args[i] = ((Map) entity).get(mapping.getProperty());
                } else {
                    args[i] = mapping.getHandler().get(entity);
                }
            }
            return args;
        }).forEach(insertValues::add);
        return this.getSelf();
    }

    @Override
    public R applyMap(List<Map<String, Object>> entityList) {
        int propertyCount = this.insertProperties.size();
        entityList.parallelStream().map(entity -> {
            Object[] args = new Object[propertyCount];
            for (int i = 0; i < propertyCount; i++) {
                ColumnMapping mapping = insertProperties.get(i);
                args[i] = entity.get(mapping.getProperty());
            }
            return args;
        }).forEach(insertValues::add);
        return this.getSelf();
    }

    @Override
    public int[] executeGetResult() throws SQLException {
        try {
            BoundSql boundSql = getBoundSql();
            String sqlString = boundSql.getSqlString();
            if (boundSql instanceof BatchBoundSql) {
                if (boundSql.getArgs().length > 1) {
                    return this.getJdbcTemplate().executeBatch(sqlString, ((BatchBoundSql) boundSql).getArgs());
                } else {
                    int i = this.getJdbcTemplate().executeUpdate(sqlString, (Object[]) boundSql.getArgs()[0]);
                    return new int[] { i };
                }
            } else {
                int i = this.getJdbcTemplate().executeUpdate(sqlString, boundSql.getArgs());
                return new int[] { i };
            }
        } finally {
            this.insertValues.clear();
        }
    }

    @Override
    protected BoundSql buildBoundSql(SqlDialect dialect) {
        if (this.insertValues.size() == 0) {
            throw new IllegalStateException("there is no data to insert");
        } else {
            return dialectInsert(dialect);
        }
    }

    protected BoundSql dialectInsert(SqlDialect dialect) {
        boolean isInsertSqlDialect = dialect instanceof InsertSqlDialect;
        TableMapping<?> tableMapping = this.getTableMapping();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        if (!isInsertSqlDialect) {
            String sqlString = defaultDialectInsert(this.isQualifier(), schemaName, tableName, this.insertColumns, dialect);
            return buildBatchBoundSql(sqlString);
        }

        switch (this.insertStrategy) {
            case Into: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportInsertInto(this.primaryKeys, this.insertColumns)) {
                    String sqlString = insertDialect.insertWithInto(this.isQualifier(), schemaName, tableName, this.primaryKeys, this.insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
            case Ignore: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportInsertIgnore(this.primaryKeys, this.insertColumns)) {
                    String sqlString = insertDialect.insertWithIgnore(this.isQualifier(), schemaName, tableName, this.primaryKeys, this.insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
            case Update: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportUpsert(this.primaryKeys, this.insertColumns)) {
                    String sqlString = insertDialect.insertWithUpsert(this.isQualifier(), schemaName, tableName, this.primaryKeys, this.insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
        }
        throw new UnsupportedOperationException(this.insertStrategy + " Unsupported.");
    }

    protected BatchBoundSql buildBatchBoundSql(String batchSql) {
        Object[][] args = new Object[this.insertValues.size()][];
        for (int i = 0; i < this.insertValues.size(); i++) {
            args[i] = this.insertValues.get(i);
        }
        return new BatchBoundSql.BatchBoundSqlObj(batchSql, args);
    }

    protected String defaultDialectInsert(boolean useQualifier, String schema, String table, List<String> columns, SqlDialect dialect) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("INSERT INTO ");
        strBuilder.append(dialect.tableName(useQualifier, schema, table));
        strBuilder.append(" ");
        strBuilder.append("(");

        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                strBuilder.append(", ");
                argBuilder.append(", ");
            }
            strBuilder.append(dialect.columnName(useQualifier, schema, table, columns.get(i)));
            argBuilder.append("?");
        }

        strBuilder.append(") VALUES (");
        strBuilder.append(argBuilder);
        strBuilder.append(")");
        return strBuilder.toString();
    }
}
