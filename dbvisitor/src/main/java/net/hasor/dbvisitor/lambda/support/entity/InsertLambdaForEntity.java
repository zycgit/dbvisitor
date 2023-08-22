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
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BatchBoundSql.BatchBoundSqlObj;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.jdbc.PreparedStatementCallback;
import net.hasor.dbvisitor.jdbc.core.ParameterDisposer;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.AbstractInsertLambda;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.MappedArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提供 lambda insert 能力。是 InsertOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class InsertLambdaForEntity<T> extends AbstractInsertLambda<InsertOperation<T>, T, SFunction<T>> implements InsertOperation<T> {
    public InsertLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
    }

    @Override
    protected InsertOperation<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

    @Override
    public int[] executeGetResult() throws SQLException {
        try {
            List<String> useColumns = this.findInsertColumns();
            String insertSql = super.buildInsert(this.dialect(), this.primaryKeys, useColumns, this.insertColumnTerms);
            if (logger.isDebugEnabled()) {
                logger.trace("Executing SQL statement [" + insertSql + "].");
            }

            TypeHandlerRegistry typeRegistry = this.getJdbcTemplate().getTypeRegistry();

            if (this.insertValues.size() > 1) {
                return this.getJdbcTemplate().executeCreator(new PreparedStatementCreatorWrap(insertSql, con -> {
                    boolean supportsGetGeneratedKeys = con != null && con.getMetaData().supportsGetGeneratedKeys();
                    MappedArg[][] batchBoundSql = buildInsertArgs(useColumns, supportsGetGeneratedKeys, con);

                    PreparedStatement ps = createPrepareStatement(con, insertSql);
                    for (Object[] batchItem : batchBoundSql) {
                        applyPreparedStatement(ps, batchItem, typeRegistry);
                        ps.addBatch();
                    }
                    return ps;
                }), (PreparedStatementCallback<int[]>) ps -> {
                    int[] res = ps.executeBatch();
                    processKeySeqHolderAfter(ps);
                    return res;
                });
            } else {
                return this.getJdbcTemplate().executeCreator(new PreparedStatementCreatorWrap(insertSql, con -> {
                    boolean supportsGetGeneratedKeys = con != null && con.getMetaData().supportsGetGeneratedKeys();
                    MappedArg[][] batchBoundSql = buildInsertArgs(useColumns, supportsGetGeneratedKeys, con);

                    PreparedStatement ps = createPrepareStatement(con, insertSql);
                    applyPreparedStatement(ps, batchBoundSql[0], typeRegistry);
                    return ps;
                }), (PreparedStatementCallback<int[]>) ps -> {
                    int res = ps.executeUpdate();
                    processKeySeqHolderAfter(ps);
                    return new int[] { res };
                });
            }
        } finally {
            for (ParameterDisposer obj : this.parameterDisposers) {
                obj.cleanupParameters();
            }

            this.insertValues.clear();
            this.parameterDisposers.clear();
            this.fillBackEntityList.clear();
        }
    }

    @Override
    protected BatchBoundSql buildBoundSql(SqlDialect dialect) {
        try {
            List<String> useColumns = this.findInsertColumns();
            String insertSql = super.buildInsert(this.dialect(), this.primaryKeys, useColumns, this.insertColumnTerms);
            MappedArg[][] batchBoundSql = buildInsertArgs(useColumns, false, null);
            return new BatchBoundSqlObj(insertSql, batchBoundSql);
        } catch (SQLException e) {
            throw ExceptionUtils.toRuntime(e); // never in effect
        }
    }

    private List<String> findInsertColumns() {
        if (this.insertValues.size() != 1) {
            return this.insertColumns;
        }

        InsertEntity entity = this.insertValues.get(0);
        if (entity.isMap) {
            Map<String, String> entityKeyMap = extractKeysMap((Map) entity.object);
            return this.insertProperties.stream().filter(c -> {
                KeySeqHolder holder = c.getKeySeqHolder();
                return entityKeyMap.containsKey(c.getProperty()) || (holder != null && holder.onBefore());
            }).map(ColumnMapping::getColumn).collect(Collectors.toList());
        } else {
            return this.insertProperties.stream().filter(c -> {
                KeySeqHolder holder = c.getKeySeqHolder();
                return c.getHandler().get(entity.object) != null || (holder != null && holder.onBefore());
            }).map(ColumnMapping::getColumn).collect(Collectors.toList());
        }
    }

    protected MappedArg[][] buildInsertArgs(List<String> useColumns, boolean forExecute, Connection executeConn) throws SQLException {
        boolean hasFillBack = !this.fillAfterProperties.isEmpty();

        TableMapping<?> tableMapping = this.getTableMapping();
        List<ColumnMapping> mappings = new ArrayList<>();
        for (String column : useColumns) {
            mappings.add(tableMapping.getPropertyByColumn(column));
        }

        MappedArg[][] batchArgs = new MappedArg[this.insertValues.size()][];
        for (int i = 0; i < this.insertValues.size(); i++) {
            InsertEntity entity = this.insertValues.get(i);
            if (hasFillBack && forExecute) {
                this.fillBackEntityList.add(entity);
            }

            MappedArg[] args = new MappedArg[mappings.size()];

            for (int j = 0; j < mappings.size(); j++) {
                ColumnMapping mapping = mappings.get(j);
                TypeHandler<?> typeHandler = entity.isMap ? null : mapping.getTypeHandler();
                Integer jdbcType;
                Object arg;

                processKeySeqHolderBefore(executeConn, mapping, entity);

                if (entity.isMap) {
                    Map<String, String> entityKeyMap = extractKeysMap((Map) entity.object);
                    if (mapping != null) {
                        arg = ((Map) entity.object).get(entityKeyMap.get(mapping.getProperty()));
                        jdbcType = mapping.getJdbcType();
                    } else {
                        arg = ((Map) entity.object).get(entityKeyMap.get(this.insertColumns.get(j)));
                        jdbcType = arg == null ? null : TypeHandlerRegistry.toSqlType(arg.getClass());
                    }
                } else {
                    arg = mapping.getHandler().get(entity.object);
                    jdbcType = mapping.getJdbcType();
                }

                if (forExecute) {
                    if (arg instanceof ParameterDisposer) {
                        this.parameterDisposers.add((ParameterDisposer) arg);
                    }
                }

                args[j] = (arg == null) ? null : new MappedArg(arg, jdbcType, typeHandler);
            }

            batchArgs[i] = args;
        }

        return batchArgs;
    }

    protected void processKeySeqHolderBefore(Connection conn, ColumnMapping mapping, InsertEntity entity) throws SQLException {
        if (!this.hasKeySeqHolderColumn || mapping.getKeySeqHolder() == null || conn == null) {
            return;
        }

        // if user specified value, then use it.
        boolean beforeProcessed;
        if (entity.isMap) {
            beforeProcessed = ((Map) entity.object).containsKey(mapping.getProperty());
        } else {
            beforeProcessed = mapping.getHandler().get(entity.object) != null;
        }

        if (beforeProcessed) {
            return;
        }

        Object value = mapping.getKeySeqHolder().beforeApply(conn, entity.object, mapping);
        if (value != null) {
            if (entity.isMap) {
                ((Map) entity.object).put(mapping.getProperty(), value);
            } else {
                mapping.getHandler().set(entity, value);
            }
        }
    }

    protected void processKeySeqHolderAfter(PreparedStatement fillBack) throws SQLException {
        if (!this.hasKeySeqHolderColumn) {
            return;
        }
        ResultSet rs = fillBack.getGeneratedKeys();
        for (InsertEntity entity : this.fillBackEntityList) {
            if (!rs.next()) {
                break;
            }
            for (int i = 0; i < this.fillAfterProperties.size(); i++) {
                ColumnMapping mapping = this.fillAfterProperties.get(i);
                if (mapping.getKeySeqHolder() != null) {
                    Object value = mapping.getKeySeqHolder().afterApply(rs, entity.object, i, mapping);
                    if (entity.isMap && value != null) {
                        ((Map) entity.object).put(mapping.getProperty(), value);
                    }
                }
            }
        }
    }
}
