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
import net.hasor.db.lambda.InsertExecute;
import net.hasor.db.lambda.LambdaOperations.LambdaInsert;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提供 lambda insert 能力。是 LambdaInsert 接口的实现类。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaInsertWrapper<T> extends AbstractExecute<T> implements LambdaInsert<T> {
    private final List<ColumnMapping>  insertProperties;
    private final List<ColumnMapping>  primaryKeyProperties;
    private final List<Object[]>       insertValues;
    private       DuplicateKeyStrategy insertStrategy;

    public LambdaInsertWrapper(TableReader<T> tableReader, LambdaTemplate jdbcTemplate) {
        super(tableReader, jdbcTemplate);
        this.insertProperties = getInsertProperties();
        this.primaryKeyProperties = getPrimaryKeyColumns();
        this.insertValues = new ArrayList<>();
        this.insertStrategy = DuplicateKeyStrategy.Into;
    }

    @Override
    public LambdaInsert<T> useQualifier() {
        this.enableQualifier();
        return this;
    }

    @Override
    public InsertExecute<T> onDuplicateStrategy(DuplicateKeyStrategy insertStrategy) {
        this.insertStrategy = Objects.requireNonNull(insertStrategy);
        return this;
    }

    protected List<ColumnMapping> getInsertProperties() {
        TableMapping<T> tableMapping = this.getTableMapping();
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
        TableMapping<T> tableMapping = this.getTableMapping();
        //
        List<ColumnMapping> pkProperties = new ArrayList<>();
        Set<String> pkColumns = new HashSet<>();
        for (ColumnMapping mapping : tableMapping.getProperties()) {
            String columnName = mapping.getColumn();
            if (!mapping.isPrimaryKey()) {
                continue;
            }
            //
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
    public InsertExecute<T> applyEntity(List<T> entityList) {
        int propertyCount = this.insertProperties.size();
        for (Object entity : entityList) {
            Object[] args = new Object[propertyCount];
            for (int i = 0; i < propertyCount; i++) {
                ColumnMapping mapping = this.insertProperties.get(i);
                args[i] = mapping.getHandler().get(entity);
            }
            this.insertValues.add(args);
        }
        return this;
    }

    @Override
    public InsertExecute<T> applyMap(List<Map<String, Object>> entityList) {
        int propertyCount = this.insertProperties.size();
        for (Map<String, Object> entity : entityList) {
            Object[] args = new Object[propertyCount];
            for (int i = 0; i < propertyCount; i++) {
                ColumnMapping mapping = this.insertProperties.get(i);
                args[i] = entity.get(mapping.getProperty());
            }
            this.insertValues.add(args);
        }
        return this;
    }

    @Override
    public BoundSql getBoundSql() {
        return getBoundSql(dialect());
    }

    @Override
    public BoundSql getBoundSql(SqlDialect dialect) {
        if (this.insertValues.size() == 0) {
            throw new IllegalStateException("there is no data to insert");
        }
        boolean isInsertSqlDialect = dialect instanceof InsertSqlDialect;
        if (isInsertSqlDialect) {
            return dialectInsert((InsertSqlDialect) dialect);
        } else {
            throw new UnsupportedOperationException(dialect.getClass().getName() + " does not implement InsertSqlDialect.");
        }
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

    protected BoundSql dialectInsert(InsertSqlDialect dialect) {
        TableMapping<T> tableMapping = this.getTableMapping();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        List<String> primaryKeys = this.primaryKeyProperties.parallelStream().map(ColumnMapping::getColumn).collect(Collectors.toList());
        List<String> insertColumns = this.insertProperties.parallelStream().map(ColumnMapping::getColumn).collect(Collectors.toList());

        switch (this.insertStrategy) {
            case Into: {
                if (dialect.supportInsertInto(primaryKeys, insertColumns)) {
                    String sqlString = dialect.insertWithInto(this.isQualifier(), schemaName, tableName, primaryKeys, insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
            case Ignore: {
                if (dialect.supportInsertIgnore(primaryKeys, insertColumns)) {
                    String sqlString = dialect.insertWithIgnore(this.isQualifier(), schemaName, tableName, primaryKeys, insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
            case Update: {
                if (dialect.supportUpsert(primaryKeys, insertColumns)) {
                    String sqlString = dialect.insertWithUpsert(this.isQualifier(), schemaName, tableName, primaryKeys, insertColumns);
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
}
