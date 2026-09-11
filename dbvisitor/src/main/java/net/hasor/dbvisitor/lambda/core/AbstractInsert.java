/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 提供 lambda insert 基础能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
public abstract class AbstractInsert<R, T, P> extends BasicLambda<R, P> implements InsertExecute<R, T> {
    protected final List<ColumnMapping>  primaryKeys;             // 主键映射。
    protected final List<ColumnMapping>  insertProperties;        // 可参与 INSERT 的列映射。
    protected final List<ColumnMapping>  fillBeforeProperties;    // INSERT 前填充值的 KeyHandler。
    protected final List<ColumnMapping>  fillAfterProperties;     // INSERT 后填充值的 KeyHandler。
    protected final List<ColumnMapping>  returnKeyProperties;     // 后填充值 Keys, 从数据库返回。
    protected final List<ColumnMapping>  customAfterProperties;   // 后填充值 Keys, 从程序本地处理。
    protected final boolean              hasKeySeqHolderColumn;
    protected final List<String>         forBuildPrimaryKeys;     // SQL 生成用主键列名。
    protected final List<String>         forBuildInsertColumns;   // SQL 生成用 INSERT 列名。
    protected final Map<String, String>  forBuildInsertColumnTerms; // INSERT value 模板。
    //
    protected       DuplicateKeyStrategy insertStrategy;
    protected final AtomicInteger        insertValuesCount;
    protected final List<InsertEntity>   insertValues;
    protected final List<InsertEntity>   fillBackEntityList;

    public AbstractInsert(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(exampleType, tableMapping, registry, jdbc, ctx);

        List<ColumnMapping> primaryKeys = new ArrayList<>();
        List<ColumnMapping> insertProperties = new ArrayList<>();
        List<ColumnMapping> fillBeforeProperties = new ArrayList<>();
        List<ColumnMapping> fillAfterProperties = new ArrayList<>();
        initProperties(primaryKeys, insertProperties, fillBeforeProperties, fillAfterProperties);

        List<String> forBuildPrimaryKeys = primaryKeys.stream().map(ColumnMapping::getColumn).toList();
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
        this.returnKeyProperties = Collections.unmodifiableList(this.fillAfterProperties.stream().filter(m -> m.getKeySeqHolder().useGeneratedKeys()).toList());
        this.customAfterProperties = Collections.unmodifiableList(this.fillAfterProperties.stream().filter(m -> !m.getKeySeqHolder().useGeneratedKeys()).toList());
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

    protected String buildInsert(List<String> primaryKeys, List<String> insertColumns, Map<String, String> insertColumnTerms, List<String> generatedColumns, GeneratedKeyStrategy generatedStrategy, int insertRows) throws SQLException {
        this.cmdBuilder.clearAll();
        this.cmdBuilder.setTable(this.getTableMapping().getCatalog(), this.getTableMapping().getSchema(), this.getTableMapping().getTable());

        for (String col : insertColumns) {
            String term = insertColumnTerms != null ? insertColumnTerms.get(col) : null;
            if (StringUtils.isBlank(term)) {
                term = "?";
            }
            this.cmdBuilder.addInsert(col, null, term);
        }

        BoundSql boundSql = this.cmdBuilder.buildInsert(isQualifier(), primaryKeys, insertRows, generatedColumns, this.insertStrategy, generatedStrategy);
        return boundSql.getSqlString();
    }

    protected PreparedStatement createPrepareStatement(Connection con, String sqlString) throws SQLException {
        if (this.getTableMapping().useGeneratedKey()) {
            return con.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
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
