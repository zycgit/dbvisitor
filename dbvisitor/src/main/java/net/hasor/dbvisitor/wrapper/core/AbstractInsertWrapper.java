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
package net.hasor.dbvisitor.wrapper.core;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.InsertSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.DuplicateKeyStrategy;

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
public abstract class AbstractInsertWrapper<R, T, P> extends BasicLambda<R, T, P> implements InsertExecute<R, T> {
    protected final List<ColumnMapping>  primaryKeys;
    protected final List<ColumnMapping>  insertProperties;
    protected final List<ColumnMapping>  fillBeforeProperties;
    protected final List<ColumnMapping>  fillAfterProperties;
    protected final boolean              hasKeySeqHolderColumn;
    protected final List<String>         forBuildPrimaryKeys;
    protected final List<String>         forBuildInsertColumns;
    protected final Map<String, String>  forBuildInsertColumnTerms;
    //
    protected       DuplicateKeyStrategy insertStrategy;
    protected final AtomicInteger        insertValuesCount;
    protected final List<InsertEntity>   insertValues;
    protected final List<InsertEntity>   fillBackEntityList;

    public AbstractInsertWrapper(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc) {
        super(exampleType, tableMapping, registry, jdbc);

        List<ColumnMapping> primaryKeys = new ArrayList<>();
        List<ColumnMapping> insertProperties = new ArrayList<>();
        List<ColumnMapping> fillBeforeProperties = new ArrayList<>();
        List<ColumnMapping> fillAfterProperties = new ArrayList<>();
        initProperties(primaryKeys, insertProperties, fillBeforeProperties, fillAfterProperties);

        List<String> forBuildPrimaryKeys = primaryKeys.stream().map(ColumnMapping::getColumn).collect(Collectors.toList());
        List<String> forBuildInsertColumns = new ArrayList<>();
        Map<String, String> forBuildInsertColumnTerms = new LinkedHashMap<>();
        for (ColumnMapping m : insertProperties) {
            forBuildInsertColumns.add(m.getColumn());
            forBuildInsertColumnTerms.put(m.getColumn(), m.getInsertTemplate());
        }

        this.primaryKeys = Collections.unmodifiableList(primaryKeys);
        this.insertProperties = Collections.unmodifiableList(insertProperties);
        this.fillBeforeProperties = Collections.unmodifiableList(fillBeforeProperties);
        this.fillAfterProperties = Collections.unmodifiableList(fillAfterProperties);
        this.forBuildPrimaryKeys = Collections.unmodifiableList(forBuildPrimaryKeys);
        this.forBuildInsertColumns = Collections.unmodifiableList(forBuildInsertColumns);
        this.forBuildInsertColumnTerms = Collections.unmodifiableMap(forBuildInsertColumnTerms);

        if (!tableMapping.isMapEntity() && this.insertProperties.isEmpty()) {
            throw new IllegalStateException("no column require INSERT.");
        }

        this.insertValuesCount = new AtomicInteger(0);
        this.insertValues = new LinkedList<>();
        this.insertStrategy = DuplicateKeyStrategy.Into;
        this.hasKeySeqHolderColumn = !this.fillBeforeProperties.isEmpty() || !this.fillAfterProperties.isEmpty();
        this.fillBackEntityList = new LinkedList<>();
    }

    protected void initProperties(List<ColumnMapping> primaryKeys, List<ColumnMapping> insert, List<ColumnMapping> fillBefore, List<ColumnMapping> fillAfter) {
        TableMapping<?> tableMapping = this.getTableMapping();

        for (String column : tableMapping.getColumns()) {
            ColumnMapping mapping = tableMapping.getPrimaryPropertyByColumn(column);
            if (mapping == null) {
                List<ColumnMapping> properties = tableMapping.getPropertyByColumn(column);
                throw new RuntimeSQLException("conflict, there are " + properties.size() + " properties mapping the same column '" + column + "', and not declare primary.");
            }

            GeneratedKeyHandler keySeqHolder = mapping.getKeySeqHolder();
            if (keySeqHolder != null) {
                if (keySeqHolder.onBefore()) {
                    fillBefore.add(mapping);
                }
                if (keySeqHolder.onAfter()) {
                    fillAfter.add(mapping);
                }
            }

            if (mapping.isPrimaryKey()) {
                primaryKeys.add(mapping);
            }

            if (mapping.isInsert()) {
                insert.add(mapping);
            }
        }
    }

    @Override
    public R reset() {
        super.reset();
        this.insertValuesCount.set(0);
        this.insertValues.clear();
        this.fillBackEntityList.clear();
        return this.getSelf();
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
        if (this.getTableMapping().useGeneratedKey()) {
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
