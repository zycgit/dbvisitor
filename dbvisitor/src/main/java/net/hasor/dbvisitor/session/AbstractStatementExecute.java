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
package net.hasor.dbvisitor.session;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.mapper.def.DmlConfig;
import net.hasor.dbvisitor.mapper.def.DqlConfig;
import net.hasor.dbvisitor.mapper.def.ExecuteConfig;
import net.hasor.dbvisitor.mapper.def.SqlConfig;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.template.jdbc.extractor.BeanMappingResultSetExtractor;
import net.hasor.dbvisitor.template.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.template.jdbc.extractor.RowCallbackHandlerResultSetExtractor;
import net.hasor.dbvisitor.template.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行器基类
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-07-20
 */
public abstract class AbstractStatementExecute {
    protected static final Logger          logger = LoggerFactory.getLogger(AbstractStatementExecute.class);
    protected final        RegistryManager registry;

    public AbstractStatementExecute(RegistryManager registry) {
        this.registry = registry;
    }

    protected void doCheck(Connection conn, SqlConfig config, Map<String, Object> data, Page pageInfo) throws SQLException {
        boolean hasOutBind;
        if (config instanceof ExecuteConfig) {
            hasOutBind = ((ExecuteConfig) config).getBindOut().length > 0;
        } else if (config instanceof DqlConfig) {
            hasOutBind = ((DqlConfig) config).getBindOut().length > 0;
        } else {
            hasOutBind = false;
        }

        if (hasOutBind && SessionHelper.usingPage(pageInfo)) {
            throw new SQLException("cannot use paging queries when using bindOut.");
        }
    }

    public final Object execute(Connection conn, StatementDef def, Map<String, Object> data) throws SQLException {
        return this.execute(conn, def, data, null, false);
    }

    public final Object execute(Connection conn, StatementDef def, Map<String, Object> data, Page pageInfo, boolean pageResult) throws SQLException {
        SqlConfig config = def.getConfig();
        this.doCheck(conn, config, data, pageInfo);

        // prepare sql
        MergedMap<String, Object> dataCtx = new MergedMap<>();
        dataCtx.appendMap(data, true);
        SqlBuilder oriSql = config.buildQuery(dataCtx, this.registry);
        BoundSql execSql = oriSql;
        BoundSql countSql = null;

        // prepare page
        long resultCount = 0L;
        if (SessionHelper.usingPage(pageInfo)) {
            PageSqlDialect dialect = this.registry.getDialect();
            long position = pageInfo.getFirstRecordPosition();
            long pageSize = pageInfo.getPageSize();
            execSql = dialect.pageSql(oriSql, position, pageSize);

            if (pageInfo.isRefreshTotalCount() || pageInfo.getTotalCount() <= 0) {
                countSql = dialect.countSql(oriSql);
            }

            resultCount = pageInfo.getTotalCount(); // old value
        }

        // query count
        if (countSql != null && pageResult) {
            try (PreparedStatement stat = conn.prepareStatement(countSql.getSqlString())) {
                if (logger.isTraceEnabled()) {
                    logger.trace(SessionHelper.fmtBoundSql(countSql).toString());
                }
                this.configStatement(stat, config);
                resultCount = this.executeCount(stat, countSql.getArgs());
            } catch (SQLException e) {
                logger.error("executeCount failed, " + ExceptionUtils.getRootCauseMessage(e) + ", " + SessionHelper.fmtBoundSql(countSql), e);
                throw e;
            }
        }

        // query data
        try (Statement stat = this.createStatement(conn, config, execSql)) {
            if (logger.isTraceEnabled()) {
                logger.trace(SessionHelper.fmtBoundSql(execSql).toString());
            }

            this.configStatement(stat, config);

            boolean retVal = this.executeQuery(stat, config, execSql);
            return this.fetchResult(retVal, stat, def, oriSql, dataCtx, pageInfo, resultCount, pageResult);
        } catch (SQLException e) {
            logger.error("executeQuery failed, " + ExceptionUtils.getRootCauseMessage(e) + ", " + SessionHelper.fmtBoundSql(countSql), e);
            throw e;
        }
    }

    protected abstract Statement createStatement(Connection conn, SqlConfig config, BoundSql execSql) throws SQLException;

    protected void configStatement(Statement stat, SqlConfig config) throws SQLException {
        if (config.getTimeout() > 0) {
            stat.setQueryTimeout(config.getTimeout());
        }
        if (config instanceof DqlConfig && ((DqlConfig) config).getFetchSize() > 0) {
            stat.setFetchSize(((DqlConfig) config).getFetchSize());
        }
    }

    private long executeCount(PreparedStatement cntStat, Object[] args) throws SQLException {
        for (int j = 0; j < args.length; j++) {
            TypeHandlerRegistry.DEFAULT.setParameterValue(cntStat, j + 1, args[j]);
        }

        try (ResultSet resultSet = cntStat.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return -1;
            }
        }
    }

    protected abstract boolean executeQuery(Statement stat, SqlConfig config, BoundSql execSql) throws SQLException;

    protected Object fetchResult(boolean retVal, Statement stat, StatementDef def, SqlBuilder oriSql, Map<String, Object> ctx, Page oriPageInfo, long newPageCnt, boolean pageResult) throws SQLException {
        String[] bindOut = null;
        boolean usingMultipleResultFetch = false;

        if (def.getConfig() instanceof DqlConfig) {
            bindOut = ((DqlConfig) def.getConfig()).getBindOut();
            usingMultipleResultFetch = bindOut.length > 0;
        } else if (def.getConfig() instanceof ExecuteConfig) {
            bindOut = ((ExecuteConfig) def.getConfig()).getBindOut();
            usingMultipleResultFetch = bindOut.length > 0;
        } else {
            bindOut = ArrayUtils.EMPTY_STRING_ARRAY;
        }

        if (usingMultipleResultFetch) {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> multipleResult = this.multipleResultFetch(oriSql, stat, retVal);
            for (String argName : bindOut) {
                if (multipleResult.containsKey(argName)) {
                    result.put(argName, multipleResult.get(argName));
                } else if (ctx.containsKey(argName)) {
                    result.put(argName, ctx.get(argName));
                } else {
                    result.put(argName, null);
                }
            }
            return result;
        } else {

            if (def.getConfig() instanceof DmlConfig) {
                return stat.getUpdateCount();
            }

            if (retVal) {
                try (ResultSet rs = stat.getResultSet()) {
                    if (rs.isLast()) {
                        return Collections.emptyList();
                    }

                    if (def.getResultExtractor() != null) {
                        return def.getResultExtractor().extractData(rs);
                    } else if (def.getResultRowCallback() != null) {
                        new RowCallbackHandlerResultSetExtractor(def.getResultRowCallback()).extractData(rs);
                        boolean isSingle = !def.isUsingCollection();
                        return isSingle ? null : pageOrNot(Collections.emptyList(), oriPageInfo, newPageCnt, pageResult);
                    } else if (def.getResultRowMapper() != null) {
                        boolean isSingle = !def.isUsingCollection();
                        List<?> objects = new RowMapperResultSetExtractor<>(def.getResultRowMapper(), (isSingle ? 1 : 0)).extractData(rs);
                        return isSingle ? (objects.isEmpty() ? null : objects.get(0)) : pageOrNot(objects, oriPageInfo, newPageCnt, pageResult);
                    } else if (def.getResultType() != null) {
                        boolean isSingle = !def.isUsingCollection();
                        List<?> objects = new BeanMappingResultSetExtractor<>(def.getResultType(), this.registry.getMappingRegistry(), (isSingle ? 1 : 0)).extractData(rs);
                        return isSingle ? (objects.isEmpty() ? null : objects.get(0)) : pageOrNot(objects, oriPageInfo, newPageCnt, pageResult);
                    } else {
                        boolean isSingle = !def.isUsingCollection();
                        TypeHandlerRegistry typeRegistry = this.registry.getMappingRegistry().getTypeRegistry();
                        boolean caseInsensitive = MappingHelper.caseInsensitive(this.registry.getMappingRegistry().getGlobalOptions());
                        List<?> objects = new ColumnMapResultSetExtractor((isSingle ? 1 : 0), typeRegistry, caseInsensitive).extractData(rs);
                        return isSingle ? (objects.isEmpty() ? null : objects.get(0)) : pageOrNot(objects, oriPageInfo, newPageCnt, pageResult);
                    }
                }
            } else {
                return stat.getUpdateCount();
            }
        }
    }

    protected Object pageOrNot(List<?> objects, Page oriPageInfo, long newPageCnt, boolean pageResult) {
        if (pageResult) {
            PageResult<?> page = new PageResult<>(oriPageInfo, objects);
            page.setTotalCount(newPageCnt);
            return page;
        } else {
            return objects;
        }
    }

    protected abstract Map<String, Object> multipleResultFetch(SqlBuilder buildSql, Statement stat, boolean retVal) throws SQLException;
}