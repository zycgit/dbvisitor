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
package net.hasor.dbvisitor.lambda.core;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.PreparedStatementCallback;
import net.hasor.dbvisitor.jdbc.core.ParameterDisposer;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

/**
 * 提供 lambda insert 基础能力。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractInsertLambda<R, T, P> extends BasicLambda<R, T, P> implements InsertExecute<R, T> {
    protected final List<ColumnMapping>  insertProperties;
    protected final List<ColumnMapping>  primaryKeyProperties;
    protected       DuplicateKeyStrategy insertStrategy;
    protected final List<String>         primaryKeys;
    protected final List<String>         insertColumns;
    protected final boolean              hasKeySeqHolderColumn;

    protected final List<Object[]>          insertValues;
    protected final List<ParameterDisposer> parameterDisposers; // 只有 insert 需要
    protected final List<FillBackEntity>    fillBackEntityList;

    public AbstractInsertLambda(Class<?> exampleType, TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
        this.insertProperties = getInsertProperties();
        this.primaryKeyProperties = getPrimaryKeyColumns();
        this.insertValues = new ArrayList<>();
        this.insertStrategy = DuplicateKeyStrategy.Into;

        this.primaryKeys = this.primaryKeyProperties.stream().map(ColumnMapping::getColumn).collect(Collectors.toList());
        this.insertColumns = this.insertProperties.stream().map(ColumnMapping::getColumn).collect(Collectors.toList());
        this.hasKeySeqHolderColumn = this.insertProperties.stream().anyMatch(c -> c.getKeySeqHolder() != null);
        this.parameterDisposers = new ArrayList<>();
        this.fillBackEntityList = new ArrayList<>();
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
    public R applyEntity(List<T> entityList) throws SQLException {
        if (this.hasKeySeqHolderColumn) {
            return this.getJdbcTemplate().execute((ConnectionCallback<R>) con -> {
                return applyEntity0(con, entityList, exampleIsMap());
            });
        } else {
            return applyEntity0(null, entityList, exampleIsMap());
        }
    }

    @Override
    public R applyMap(List<Map<String, Object>> entityList) throws SQLException {
        if (this.hasKeySeqHolderColumn) {
            return this.getJdbcTemplate().execute((ConnectionCallback<R>) con -> {
                return applyEntity0(con, entityList, true);
            });
        } else {
            return applyEntity0(null, entityList, true);
        }
    }

    private R applyEntity0(Connection conn, List<?> entityList, boolean isMap) throws SQLException {
        boolean supportsGetGeneratedKeys = conn != null && conn.getMetaData().supportsGetGeneratedKeys();
        int propertyCount = this.insertProperties.size();

        for (Object entity : entityList) {
            Object[] args = new Object[propertyCount];
            for (int i = 0; i < propertyCount; i++) {
                ColumnMapping mapping = this.insertProperties.get(i);
                if (isMap) {
                    Map<String, String> entityKeyMap = extractKeysMap((Map) entity);
                    args[i] = ((Map) entity).get(entityKeyMap.get(mapping.getProperty()));
                } else {
                    args[i] = mapping.getHandler().get(entity);
                }

                if (conn != null && args[i] == null && mapping.getKeySeqHolder() != null) {
                    args[i] = mapping.getKeySeqHolder().beforeApply(conn, entity, mapping);

                    if (isMap && args[i] != null) {
                        ((Map) entity).put(mapping.getProperty(), args[i]);
                    }
                }
            }

            if (supportsGetGeneratedKeys) {
                this.fillBackEntityList.add(new FillBackEntity(entity, isMap));
            }

            this.insertValues.add(args);
            for (Object arg : args) {
                if (arg instanceof ParameterDisposer) {
                    this.parameterDisposers.add((ParameterDisposer) arg);
                }
            }
        }
        return this.getSelf();
    }

    @Override
    public int[] executeGetResult() throws SQLException {
        try {
            BatchBoundSql boundSql = (BatchBoundSql) getBoundSql();
            String sqlString = boundSql.getSqlString();

            if (logger.isDebugEnabled()) {
                logger.trace("Executing SQL statement [" + boundSql.getSqlString() + "].");
            }

            TypeHandlerRegistry typeRegistry = this.getJdbcTemplate().getTypeRegistry();

            if (boundSql.getArgs().length > 1) {
                return this.getJdbcTemplate().executeCreator(con -> {
                    PreparedStatement ps = createPrepareStatement(con, sqlString);
                    for (Object[] batchItem : boundSql.getArgs()) {
                        applyPreparedStatement(ps, batchItem, typeRegistry);
                        ps.addBatch();
                    }
                    return ps;
                }, (PreparedStatementCallback<int[]>) ps -> {
                    int[] res = ps.executeBatch();
                    processFillBack(ps);
                    return res;
                });
            } else {
                return this.getJdbcTemplate().executeCreator(con -> {
                    PreparedStatement ps = createPrepareStatement(con, sqlString);
                    applyPreparedStatement(ps, boundSql.getArgs()[0], typeRegistry);
                    return ps;
                }, (PreparedStatementCallback<int[]>) ps -> {
                    int res = ps.executeUpdate();
                    processFillBack(ps);
                    return new int[] { res };
                });
            }
        } finally {
            for (ParameterDisposer obj : this.parameterDisposers) {
                obj.cleanupParameters();
            }
            this.insertValues.clear();
            this.fillBackEntityList.clear();
        }
    }

    protected PreparedStatement createPrepareStatement(Connection con, String sqlString) throws SQLException {
        if (this.hasKeySeqHolderColumn) {
            return con.prepareStatement(sqlString, RETURN_GENERATED_KEYS);
        } else {
            return con.prepareStatement(sqlString);
        }
    }

    private void applyPreparedStatement(PreparedStatement ps, Object[] batchValues, TypeHandlerRegistry typeRegistry) throws SQLException {
        int idx = 1;
        for (Object value : batchValues) {
            if (value == null) {
                ps.setObject(idx, null);
            } else {
                typeRegistry.setParameterValue(ps, idx, value);
            }
            idx++;
        }
    }

    private void processFillBack(PreparedStatement fillBack) throws SQLException {
        ResultSet rs = fillBack.getGeneratedKeys();
        for (FillBackEntity entity : this.fillBackEntityList) {
            if (!rs.next()) {
                break;
            }
            for (int i = 0; i < this.insertProperties.size(); i++) {
                ColumnMapping mapping = this.insertProperties.get(i);
                if (mapping.getKeySeqHolder() != null) {
                    Object value = mapping.getKeySeqHolder().afterApply(rs, entity.object, i, mapping);
                    if (entity.isMap && value != null) {
                        ((Map) entity.object).put(mapping.getProperty(), value);
                    }
                }
            }
        }
    }

    @Override
    protected BatchBoundSql buildBoundSql(SqlDialect dialect) {
        if (this.insertValues.size() == 0) {
            throw new IllegalStateException("there is no data to insert");
        } else {
            return dialectInsert(dialect);
        }
    }

    protected BatchBoundSql dialectInsert(SqlDialect dialect) {
        boolean isInsertSqlDialect = dialect instanceof InsertSqlDialect;
        TableMapping<?> tableMapping = this.getTableMapping();
        String catalogName = tableMapping.getCatalog();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        if (!isInsertSqlDialect) {
            String sqlString = defaultDialectInsert(catalogName, schemaName, tableName, this.insertColumns, dialect);
            return buildBatchBoundSql(sqlString);
        }

        switch (this.insertStrategy) {
            case Into: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportInsertInto(this.primaryKeys, this.insertColumns)) {
                    String sqlString = insertDialect.insertWithInto(this.isQualifier(), catalogName, schemaName, tableName, this.primaryKeys, this.insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
            case Ignore: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportInsertIgnore(this.primaryKeys, this.insertColumns)) {
                    String sqlString = insertDialect.insertWithIgnore(this.isQualifier(), catalogName, schemaName, tableName, this.primaryKeys, this.insertColumns);
                    return buildBatchBoundSql(sqlString);
                }
                break;
            }
            case Update: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportUpsert(this.primaryKeys, this.insertColumns)) {
                    String sqlString = insertDialect.insertWithUpsert(this.isQualifier(), catalogName, schemaName, tableName, this.primaryKeys, this.insertColumns);
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

    protected String defaultDialectInsert(String catalog, String schema, String table, List<String> columns, SqlDialect dialect) {
        boolean useQualifier = isQualifier();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("INSERT INTO ");
        strBuilder.append(dialect.tableName(useQualifier, catalog, schema, table));
        strBuilder.append(" ");
        strBuilder.append("(");

        StringBuilder argBuilder = new StringBuilder();
        TableMapping<?> tableMapping = this.getTableMapping();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                strBuilder.append(", ");
                argBuilder.append(", ");
            }
            strBuilder.append(dialect.fmtName(useQualifier, columns.get(i)));

            String specialValue = tableMapping.getPropertyByColumn(columns.get(i)).getInsertTemplate();
            String colValue = StringUtils.isNotBlank(specialValue) ? specialValue : "?";

            argBuilder.append(colValue);
        }

        strBuilder.append(") VALUES (");
        strBuilder.append(argBuilder);
        strBuilder.append(")");
        return strBuilder.toString();
    }

    private static class FillBackEntity {
        public Object  object;
        public boolean isMap;

        public FillBackEntity(Object object, boolean isMap) {
            this.object = object;
            this.isMap = isMap;
        }
    }
}
