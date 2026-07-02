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
package net.hasor.dbvisitor.lambda.support.entity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.function.ESupplier;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.BatchBoundSql.BatchBoundSqlObj;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityInsert;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.MapInsert;
import net.hasor.dbvisitor.lambda.core.AbstractInsert;
import net.hasor.dbvisitor.lambda.support.map.MapInsertImpl;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 提供 lambda insert 能力。是 EntityInsert 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class EntityInsertImpl<T> extends AbstractInsert<Insert<T>, T, SFunction<T>> implements EntityInsert<T> {
    public EntityInsertImpl(TableMapping<T> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping.entityType(), tableMapping, registry, jdbc, ctx);
    }

    @Override
    public MapInsert asMap() {
        return new MapInsertImpl(this.getTableMapping(), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    protected Insert<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    @Override
    public Insert<T> applyEntity(T... entity) throws SQLException {
        if (this.exampleIsMap()) {
            Map<String, Object>[] array = new Map[entity.length];
            for (int i = 0; i < entity.length; i++) {
                array[i] = (Map<String, Object>) entity[i];
            }
            return this.applyMap(array);
        } else {
            return this.applyEntity(Arrays.asList(entity));
        }
    }

    @Override
    public int[] executeGetResult() throws SQLException {
        try {
            Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");
            List<String> useColumns = this.findInsertColumns();
            List<String> returnColumns = this.returnKeyProperties.stream().map(ColumnMapping::getColumn).collect(Collectors.toList());

            ESupplier<GeneratedKeyStrategy, SQLException> s = () -> {
                boolean hasCustomAfter = !this.customAfterProperties.isEmpty();
                if (this.dialect() instanceof InsertSqlDialect d && !hasCustomAfter) {
                    return d.generatedKeyStrategy(this.forBuildPrimaryKeys, useColumns, returnColumns, this.insertStrategy);
                } else {
                    return GeneratedKeyStrategy.OneByOne;
                }
            };

            return this.jdbc.execute((ConnectionCallback<int[]>) c -> switch (s.eGet()) {
                case JdbcBatch -> this.executeByBatch(c, useColumns, returnColumns);
                case JdbcBatchGeneratedKeys -> this.executeByBatchGeneratedKeys(c, useColumns, returnColumns);
                case MultiValuesResultSet -> this.executeByMultiValues(c, useColumns, returnColumns);
                case OneByOne -> this.executeByEach(c, useColumns, returnColumns);
            });
        } finally {
            this.reset();
        }
    }

    @Override
    public BoundSql getBoundSql() throws SQLException {
        List<String> insertColumns = this.findInsertColumns();
        String insertSql = super.buildInsert(this.forBuildPrimaryKeys, insertColumns, this.forBuildInsertColumnTerms, Collections.emptyList(), GeneratedKeyStrategy.JdbcBatch, this.insertValuesCount.get());
        SqlArg[][] batchBoundSql;

        if (this.jdbc != null) {
            batchBoundSql = this.jdbc.execute((ConnectionCallback<SqlArg[][]>) c -> {
                return buildInsertArgs(insertColumns, false, c);
            });
        } else {
            batchBoundSql = buildInsertArgs(insertColumns, false, null);
        }

        return new BatchBoundSqlObj(insertSql, batchBoundSql);
    }

    private List<String> findInsertColumns() {
        if (this.insertValuesCount.get() != 1) {
            return this.insertProperties.stream().filter(m -> {
                GeneratedKeyHandler holder = m.getKeySeqHolder();
                if (holder != null && holder.onBefore()) {
                    return true;
                }

                for (InsertEntity entity : this.insertValues) {
                    for (Object obj : entity.objList) {
                        if (entity.isMap) {
                            if (((Map) obj).containsKey(m.getProperty())) {
                                return true;
                            }
                        } else if (m.getHandler().get(obj) != null) {
                            return true;
                        }
                    }
                }
                return false;
            }).map(ColumnMapping::getColumn).collect(Collectors.toList());
        }

        InsertEntity entity = this.insertValues.get(0);
        if (entity.isMap) {
            Map ent = (Map) entity.objList.get(0);
            return this.insertProperties.stream().filter(c -> {
                GeneratedKeyHandler holder = c.getKeySeqHolder();
                return ent.containsKey(c.getProperty()) || (holder != null && holder.onBefore());
            }).map(ColumnMapping::getColumn).collect(Collectors.toList());
        } else {
            Object ent = entity.objList.get(0);
            return this.insertProperties.stream().filter(c -> {
                GeneratedKeyHandler holder = c.getKeySeqHolder();
                return c.getHandler().get(ent) != null || (holder != null && holder.onBefore());
            }).map(ColumnMapping::getColumn).collect(Collectors.toList());
        }
    }

    //

    private int[] executeByBatch(Connection con, List<String> insertColumns, List<String> returnColumns) throws SQLException {
        TypeHandlerRegistry typeRegistry = this.jdbc.getRegistry().getTypeRegistry();
        String insertSql = super.buildInsert(this.forBuildPrimaryKeys, insertColumns, this.forBuildInsertColumnTerms, //
                returnColumns, GeneratedKeyStrategy.JdbcBatch, this.insertValuesCount.get());
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + insertSql + "].");
        }

        SqlArg[][] batchBoundSql = buildInsertArgs(insertColumns, true, con);
        PreparedStatement ps = createPrepareStatement(con, insertSql);
        for (Object[] batchItem : batchBoundSql) {
            applyPreparedStatement(ps, batchItem, typeRegistry);
            ps.addBatch();
        }

        return ps.executeBatch();
    }

    private int[] executeByBatchGeneratedKeys(Connection con, List<String> insertColumns, List<String> returnColumns) throws SQLException {
        TypeHandlerRegistry typeRegistry = this.jdbc.getRegistry().getTypeRegistry();
        String insertSql = super.buildInsert(this.forBuildPrimaryKeys, insertColumns, this.forBuildInsertColumnTerms, //
                returnColumns, GeneratedKeyStrategy.JdbcBatchGeneratedKeys, this.insertValuesCount.get());
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + insertSql + "].");
        }

        SqlArg[][] batchBoundSql = buildInsertArgs(insertColumns, true, con);
        PreparedStatement ps = createPrepareStatement(con, insertSql);
        for (Object[] batchItem : batchBoundSql) {
            applyPreparedStatement(ps, batchItem, typeRegistry);
            ps.addBatch();
        }

        int[] res = ps.executeBatch();
        processKeySeqHolderAfter(ps, null);
        return res;
    }

    private int[] executeByMultiValues(Connection con, List<String> insertColumns, List<String> returnColumns) throws SQLException {
        TypeHandlerRegistry typeRegistry = this.jdbc.getRegistry().getTypeRegistry();
        String insertSql = super.buildInsert(this.forBuildPrimaryKeys, insertColumns, this.forBuildInsertColumnTerms, //
                returnColumns, GeneratedKeyStrategy.MultiValuesResultSet, this.insertValuesCount.get());
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + insertSql + "].");
        }

        SqlArg[][] batchBoundSql = buildInsertArgs(insertColumns, true, con);
        Object[] values = new Object[batchBoundSql.length * insertColumns.size()];
        int offset = 0;
        for (SqlArg[] rowArgs : batchBoundSql) {
            System.arraycopy(rowArgs, 0, values, offset, rowArgs.length);
            offset += rowArgs.length;
        }

        try (PreparedStatement ps = con.prepareStatement(insertSql)) {
            applyPreparedStatement(ps, values, typeRegistry);
            try (ResultSet rs = ps.executeQuery()) {
                int rows = processKeySeqHolderAfter(rs, null);
                int[] res = new int[batchBoundSql.length];
                Arrays.fill(res, 0, Math.min(rows, res.length), 1);
                return res;
            }
        }
    }

    private int[] executeByEach(Connection con, List<String> insertColumns, List<String> returnColumns) throws SQLException {
        TypeHandlerRegistry typeRegistry = this.jdbc.getRegistry().getTypeRegistry();
        String insertSql = super.buildInsert(this.forBuildPrimaryKeys, insertColumns, this.forBuildInsertColumnTerms, //
                returnColumns, GeneratedKeyStrategy.OneByOne, 1);
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + insertSql + "].");
        }

        List<ColumnMapping> mappings = this.resolveMappings(insertColumns);
        int[] res = new int[this.insertValuesCount.get()];
        int i = 0;

        for (InsertEntity entity : this.insertValues) {
            for (Object obj : entity.objList) {
                SqlArg[] args;
                if (entity.isMap) {
                    args = this.buildArgsForMap((Map) obj, mappings, true, con);
                } else {
                    args = this.buildArgsForEntity(obj, mappings, true, con);
                }

                try (PreparedStatement ps = createPrepareStatement(con, insertSql)) {
                    applyPreparedStatement(ps, args, typeRegistry);
                    InsertEntity rowEntity = new InsertEntity(Collections.singletonList(obj), entity.isMap);
                    if (ps.execute()) {
                        try (ResultSet rs = ps.getResultSet()) {
                            res[i++] = processKeySeqHolderAfter(rs, rowEntity);
                        }
                    } else {
                        res[i++] = ps.getUpdateCount();
                        processKeySeqHolderAfter(ps, rowEntity);
                    }
                }
            }
        }
        return res;
    }

    //

    @Override
    protected PreparedStatement createPrepareStatement(Connection con, String sqlString) throws SQLException {
        if (!this.returnKeyProperties.isEmpty()) {
            String[] keyColumns = this.returnKeyProperties.stream().map(ColumnMapping::getColumn).toArray(String[]::new);
            return con.prepareStatement(sqlString, keyColumns);
        } else {
            return super.createPrepareStatement(con, sqlString);
        }
    }

    protected SqlArg[][] buildInsertArgs(List<String> useColumns, boolean forExecute, Connection executeConn) throws SQLException {
        boolean hasFillBack = !this.fillAfterProperties.isEmpty();
        if (hasFillBack && forExecute) {
            this.fillBackEntityList.addAll(this.insertValues);
        }

        List<ColumnMapping> mappings = this.resolveMappings(useColumns);
        SqlArg[][] batchArgs = new SqlArg[this.insertValuesCount.get()][];
        int i = 0;
        for (InsertEntity entity : this.insertValues) {
            for (Object obj : entity.objList) {
                if (entity.isMap) {
                    batchArgs[i] = this.buildArgsForMap((Map) obj, mappings, forExecute, executeConn);
                } else {
                    batchArgs[i] = this.buildArgsForEntity(obj, mappings, forExecute, executeConn);
                }
                i++;
            }
        }
        return batchArgs;
    }

    private List<ColumnMapping> resolveMappings(List<String> useColumns) throws SQLException {
        TableMapping<?> tableMapping = this.getTableMapping();
        List<ColumnMapping> mappings = new ArrayList<>();
        for (String column : useColumns) {
            ColumnMapping primary = tableMapping.getPrimaryPropertyByColumn(column);
            if (primary == null) {
                List<ColumnMapping> properties = tableMapping.getPropertyByColumn(column);
                throw new SQLException("conflict, there are " + properties.size() + " properties mapping the same column '" + column + "', and not declare primary.");
            }
            mappings.add(primary);
        }
        return mappings;
    }

    protected SqlArg[] buildArgsForMap(Map entity, List<ColumnMapping> mappings, boolean forExecute, Connection executeConn) throws SQLException {
        SqlArg[] args = new SqlArg[mappings.size()];
        for (int j = 0; j < mappings.size(); j++) {
            ColumnMapping mapping = mappings.get(j);
            Integer jdbcType;
            Object arg;

            processKeySeqHolderBefore(executeConn, mapping, entity, true);

            if (mapping != null) {
                arg = entity.get(mapping.getProperty());
                jdbcType = mapping.getJdbcType();
            } else {
                arg = entity.get(mapping.getProperty());
                jdbcType = arg == null ? null : TypeHandlerRegistry.toSqlType(arg.getClass());
            }

            args[j] = (arg == null) ? null : new SqlArg(arg, jdbcType, null);
        }
        return args;
    }

    protected SqlArg[] buildArgsForEntity(Object entity, List<ColumnMapping> mappings, boolean forExecute, Connection executeConn) throws SQLException {
        SqlArg[] args = new SqlArg[mappings.size()];
        for (int j = 0; j < mappings.size(); j++) {
            ColumnMapping mapping = mappings.get(j);
            TypeHandler<?> typeHandler = mapping.getTypeHandler();
            Integer jdbcType = mapping.getJdbcType();

            processKeySeqHolderBefore(executeConn, mapping, entity, false);

            Object arg = (entity instanceof Map) ? ((Map<?, ?>) entity).get(mapping.getProperty()) : mapping.getHandler().get(entity);
            args[j] = (arg == null) ? null : new SqlArg(arg, jdbcType, typeHandler);
        }
        return args;
    }

    //

    protected void processKeySeqHolderBefore(Connection conn, ColumnMapping mapping, Object entity, boolean isMap) throws SQLException {
        if (!this.hasKeySeqHolderColumn || mapping.getKeySeqHolder() == null || conn == null) {
            return;
        }

        // if user specified value, then use it.
        boolean beforeProcessed;
        if (isMap) {
            beforeProcessed = ((Map) entity).containsKey(mapping.getProperty());
        } else {
            beforeProcessed = mapping.getHandler().get(entity) != null;
        }

        if (beforeProcessed) {
            return;
        }

        Object value = mapping.getKeySeqHolder().beforeApply(conn, entity, mapping);

        if (value != null) {
            if (isMap) {
                ((Map) entity).put(mapping.getProperty(), value);
            } else {
                mapping.getHandler().set(entity, value);
            }
        }
    }

    protected void processKeySeqHolderAfter(PreparedStatement fillBack, InsertEntity onlyEntity) throws SQLException {
        if (!this.hasKeySeqHolderColumn) {
            return;
        }

        try (ResultSet rs = !this.returnKeyProperties.isEmpty() ? fillBack.getGeneratedKeys() : null) {
            processKeySeqHolderAfter(rs, onlyEntity);
        }
    }

    protected int processKeySeqHolderAfter(ResultSet rs, InsertEntity onlyEntity) throws SQLException {
        if (!this.hasKeySeqHolderColumn) {
            return 0;
        }

        int rows = 0;
        List<InsertEntity> entities = onlyEntity != null ? Collections.singletonList(onlyEntity) : this.fillBackEntityList;
        boolean canReadReturnKeys = rs != null;
        for (InsertEntity entity : entities) {
            for (Object obj : entity.objList) {
                boolean processed = false;

                if (!this.returnKeyProperties.isEmpty() && canReadReturnKeys) {
                    canReadReturnKeys = rs.next();
                    if (canReadReturnKeys) {
                        processed = true;
                        for (int i = 0; i < this.returnKeyProperties.size(); i++) {
                            ColumnMapping mapping = this.returnKeyProperties.get(i);
                            Object value = mapping.getKeySeqHolder().afterApply(rs, obj, i, mapping);
                            if (entity.isMap && value != null) {
                                ((Map) obj).put(mapping.getProperty(), value);
                            }
                        }
                    }
                }

                for (int i = 0; i < this.customAfterProperties.size(); i++) {
                    processed = true;
                    ColumnMapping mapping = this.customAfterProperties.get(i);
                    Object value = mapping.getKeySeqHolder().afterApply(null, obj, i, mapping);
                    if (entity.isMap && value != null) {
                        ((Map) obj).put(mapping.getProperty(), value);
                    }
                }

                if (processed) {
                    rows++;
                }
            }
        }
        return rows;
    }
}
