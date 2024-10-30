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
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.mapping.KeySeqHolder;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

/**
 * 提供 lambda insert 基础能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public abstract class AbstractInsertLambda<R, T, P> extends BasicLambda<R, T, P> implements InsertExecute<R, T> {
    protected final List<ColumnMapping>  insertProperties;
    protected final List<ColumnMapping>  fillBeforeProperties;
    protected final List<ColumnMapping>  fillAfterProperties;
    protected       DuplicateKeyStrategy insertStrategy;
    protected final List<String>         primaryKeys;
    protected final List<String>         insertColumns;
    protected final Map<String, String>  insertColumnTerms;
    protected final boolean              hasKeySeqHolderColumn;
    //
    protected final AtomicInteger        insertValuesCount;
    protected final List<InsertEntity>   insertValues;
    protected final List<InsertEntity>   fillBackEntityList;

    public AbstractInsertLambda(Class<?> exampleType, TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(exampleType, tableMapping, registry, jdbc);

        this.insertProperties = new ArrayList<>();
        this.fillBeforeProperties = new ArrayList<>();
        this.fillAfterProperties = new ArrayList<>();
        initProperties(this.insertProperties, this.fillBeforeProperties, this.fillAfterProperties);

        if (!tableMapping.isMapEntity() && this.insertProperties.isEmpty()) {
            throw new IllegalStateException("no column require INSERT.");
        }

        this.insertColumns = new ArrayList<>();
        this.insertColumnTerms = new LinkedHashMap<>();
        for (ColumnMapping colMap : this.insertProperties) {
            this.insertColumns.add(colMap.getColumn());
            if (StringUtils.isNotBlank(colMap.getInsertTemplate())) {
                this.insertColumnTerms.put(colMap.getColumn(), colMap.getInsertTemplate());
            }
        }

        this.insertValuesCount = new AtomicInteger(0);
        this.insertValues = new LinkedList<>();
        this.insertStrategy = DuplicateKeyStrategy.Into;
        this.primaryKeys = this.getPrimaryKey().stream().map(ColumnMapping::getColumn).collect(Collectors.toList());
        this.hasKeySeqHolderColumn = !this.fillBeforeProperties.isEmpty() || !this.fillAfterProperties.isEmpty();
        this.fillBackEntityList = new LinkedList<>();
    }

    protected void initProperties(List<ColumnMapping> insert, List<ColumnMapping> fillBefore, List<ColumnMapping> fillAfter) {
        TableMapping<?> tableMapping = this.getTableMapping();
        Set<String> insertColumns = new HashSet<>();

        for (ColumnMapping mapping : tableMapping.getProperties()) {
            KeySeqHolder keySeqHolder = mapping.getKeySeqHolder();
            if (keySeqHolder != null) {
                if (keySeqHolder.onBefore()) {
                    fillBefore.add(mapping);
                }
                if (keySeqHolder.onAfter()) {
                    fillAfter.add(mapping);
                }
            }

            String columnName = mapping.getColumn();
            if (!mapping.isInsert()) {
                continue;
            }

            if (insertColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            } else {
                insertColumns.add(columnName);
                insert.add(mapping);
            }
        }
    }

    @Override
    public R onDuplicateStrategy(DuplicateKeyStrategy insertStrategy) {
        this.insertStrategy = Objects.requireNonNull(insertStrategy);
        return this.getSelf();
    }

    @Override
    public R applyEntity(List<T> entityList) throws SQLException {
        this.insertValues.add(new InsertEntity(entityList, exampleIsMap()));
        this.insertValuesCount.addAndGet(entityList.size());
        return this.getSelf();
    }

    @Override
    public R applyMap(List<Map<String, Object>> entityList) throws SQLException {
        this.insertValues.add(new InsertEntity(entityList, true));
        this.insertValuesCount.addAndGet(entityList.size());
        return this.getSelf();
    }

    protected String buildInsert(SqlDialect dialect, List<String> primaryKeys, List<String> insertColumns, Map<String, String> insertColumnTerms) {
        boolean isInsertSqlDialect = dialect instanceof InsertSqlDialect;
        TableMapping<?> tableMapping = this.getTableMapping();
        String catalogName = tableMapping.getCatalog();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        if (!isInsertSqlDialect) {
            return DefaultSqlDialect.DEFAULT.insertInto(this.isQualifier(), catalogName, schemaName, tableName, primaryKeys, insertColumns, insertColumnTerms);
        }

        switch (this.insertStrategy) {
            case Into: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportInto(primaryKeys, insertColumns)) {
                    return insertDialect.insertInto(this.isQualifier(), catalogName, schemaName, tableName, primaryKeys, insertColumns, insertColumnTerms);
                }
                break;
            }
            case Ignore: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportIgnore(primaryKeys, insertColumns)) {
                    return insertDialect.insertIgnore(this.isQualifier(), catalogName, schemaName, tableName, primaryKeys, insertColumns, insertColumnTerms);
                }
                break;
            }
            case Update: {
                InsertSqlDialect insertDialect = (InsertSqlDialect) dialect;
                if (insertDialect.supportReplace(primaryKeys, insertColumns)) {
                    return insertDialect.insertReplace(this.isQualifier(), catalogName, schemaName, tableName, primaryKeys, insertColumns, insertColumnTerms);
                }
                break;
            }
        }
        throw new UnsupportedOperationException(this.insertStrategy + " Unsupported.");
    }

    protected PreparedStatement createPrepareStatement(Connection con, String sqlString) throws SQLException {
        if (this.hasKeySeqHolderColumn) {
            return con.prepareStatement(sqlString, RETURN_GENERATED_KEYS);
        } else {
            return con.prepareStatement(sqlString);
        }
    }

    protected void applyPreparedStatement(PreparedStatement ps, Object[] batchValues, TypeHandlerRegistry typeRegistry) throws SQLException {
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

    protected static class InsertEntity {
        public List<?> objList;
        public boolean isMap;

        public InsertEntity(List<?> objList, boolean isMap) {
            this.objList = objList;
            this.isMap = isMap;
        }
    }
}
