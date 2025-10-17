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
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BatchBoundSql.BatchBoundSqlObj;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityInsert;
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

            SqlDialect dialect = this.dialect();
            List<String> useColumns = this.findInsertColumns();
            String insertSql = super.buildInsert(dialect, this.forBuildPrimaryKeys, useColumns, this.forBuildInsertColumnTerms);
            if (logger.isDebugEnabled()) {
                logger.trace("Executing SQL statement [" + insertSql + "].");
            }

            TypeHandlerRegistry typeRegistry = this.jdbc.getRegistry().getTypeRegistry();

            if (this.insertValuesCount.get() > 1) {
                if (dialect.supportBatch()) {
                    return this.jdbc.execute((ConnectionCallback<int[]>) con -> {
                        boolean supportGetGeneratedKeys = con != null && con.getMetaData().supportsGetGeneratedKeys();
                        SqlArg[][] batchBoundSql = buildInsertArgs(useColumns, supportGetGeneratedKeys, con);

                        PreparedStatement ps = createPrepareStatement(con, insertSql);
                        for (Object[] batchItem : batchBoundSql) {
                            applyPreparedStatement(ps, batchItem, typeRegistry);
                            ps.addBatch();
                        }

                        int[] res = ps.executeBatch();
                        processKeySeqHolderAfter(ps);
                        return res;
                    });
                } else {
                    return this.jdbc.execute((ConnectionCallback<int[]>) con -> {
                        boolean supportGetGeneratedKeys = con != null && con.getMetaData().supportsGetGeneratedKeys();
                        SqlArg[][] batchBoundSql = buildInsertArgs(useColumns, supportGetGeneratedKeys, con);
                        int[] res = new int[batchBoundSql.length];

                        for (int i = 0; i < batchBoundSql.length; i++) {
                            try (PreparedStatement ps = createPrepareStatement(con, insertSql)) {
                                applyPreparedStatement(ps, batchBoundSql[i], typeRegistry);
                                res[i] = ps.executeUpdate();
                                processKeySeqHolderAfter(ps);
                            }
                        }
                        return res;
                    });
                }
            } else {
                return this.jdbc.execute((ConnectionCallback<int[]>) con -> {
                    boolean supportsGetGeneratedKeys = con != null && con.getMetaData().supportsGetGeneratedKeys();
                    SqlArg[][] batchBoundSql = buildInsertArgs(useColumns, supportsGetGeneratedKeys, con);

                    PreparedStatement ps = createPrepareStatement(con, insertSql);
                    applyPreparedStatement(ps, batchBoundSql[0], typeRegistry);

                    int res = ps.executeUpdate();
                    processKeySeqHolderAfter(ps);
                    return new int[] { res };
                });
            }
        } finally {
            this.reset();
        }
    }

    @Override
    protected BatchBoundSql buildBoundSql(SqlDialect dialect) throws SQLException {
        try {
            List<String> useColumns = this.findInsertColumns();
            String insertSql = super.buildInsert(dialect, this.forBuildPrimaryKeys, useColumns, this.forBuildInsertColumnTerms);
            SqlArg[][] batchBoundSql;

            if (this.jdbc != null) {
                batchBoundSql = this.jdbc.execute((ConnectionCallback<SqlArg[][]>) con -> {
                    return buildInsertArgs(useColumns, false, con);
                });
            } else {
                batchBoundSql = buildInsertArgs(useColumns, false, null);
            }

            return new BatchBoundSqlObj(insertSql, batchBoundSql);
        } catch (SQLException e) {
            throw ExceptionUtils.toRuntime(e); // never in effect
        }
    }

    private List<String> findInsertColumns() {
        if (this.insertValuesCount.get() != 1) {
            return this.forBuildInsertColumns;
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

    protected SqlArg[][] buildInsertArgs(List<String> useColumns, boolean forExecute, Connection executeConn) throws SQLException {
        boolean hasFillBack = !this.fillAfterProperties.isEmpty();
        if (hasFillBack && forExecute) {
            this.fillBackEntityList.addAll(this.insertValues);
        }

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

    protected void processKeySeqHolderAfter(PreparedStatement fillBack) throws SQLException {
        if (!this.hasKeySeqHolderColumn) {
            return;
        }

        ResultSet rs = null;
        if (this.getTableMapping().useGeneratedKey()) {
            rs = fillBack.getGeneratedKeys();
        }

        for (InsertEntity entity : this.fillBackEntityList) {
            for (Object obj : entity.objList) {
                if (rs != null && !rs.next()) {
                    break;
                }
                for (int i = 0; i < this.fillAfterProperties.size(); i++) {
                    ColumnMapping mapping = this.fillAfterProperties.get(i);
                    if (mapping.getKeySeqHolder() != null) {
                        Object value = mapping.getKeySeqHolder().afterApply(rs, obj, i, mapping);
                        if (entity.isMap && value != null) {
                            ((Map) obj).put(mapping.getProperty(), value);
                        }
                    }
                }
            }
        }
    }
}
