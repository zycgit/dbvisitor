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
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.jdbc.*;
import net.hasor.dbvisitor.jdbc.SqlParameter.InSqlParameter;
import net.hasor.dbvisitor.jdbc.SqlParameter.ReturnSqlParameter;
import net.hasor.dbvisitor.jdbc.extractor.*;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.MappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.MappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dbVisitor based and reimplements
 * @version : 2013-10-12
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @author 赵永春 (zyc@hasor.net)
 * @see PreparedStatementCreator
 * @see PreparedStatementSetter
 * @see CallableStatementCreator
 * @see PreparedStatementCallback
 * @see CallableStatementCallback
 * @see ResultSetExtractor
 * @see RowCallbackHandler
 * @see RowMapper
 */
public class JdbcTemplate extends JdbcConnection implements JdbcOperations {
    private static final Logger              logger                 = LoggerFactory.getLogger(JdbcTemplate.class);
    /* 当JDBC 结果集中如出现相同的列名仅仅大小写不同时。是否保留大小写列名敏感。
     * 如果为 true 表示不敏感，并且结果集Map中保留两个记录。如果为 false 则表示敏感，如出现冲突列名后者将会覆盖前者。*/
    private              boolean             resultsCaseInsensitive = true;
    private              TypeHandlerRegistry typeRegistry           = TypeHandlerRegistry.DEFAULT;

    /**
     * Construct a new JdbcTemplate for bean usage.
     * <p>Note: The DataSource has to be set before using the instance.
     * @see #setDataSource
     */
    public JdbcTemplate() {
        super();
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param typeRegistry the TypeHandlerRegistry
     */
    public JdbcTemplate(final DataSource dataSource, TypeHandlerRegistry typeRegistry) {
        super(dataSource);
        this.typeRegistry = Objects.requireNonNull(typeRegistry, "typeRegistry is null.");
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public JdbcTemplate(final Connection conn) {
        super(conn);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param typeRegistry the TypeHandlerRegistry
     */
    public JdbcTemplate(final Connection conn, TypeHandlerRegistry typeRegistry) {
        super(conn);
        this.typeRegistry = Objects.requireNonNull(typeRegistry, "typeRegistry is null.");
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of Dynamic
     */
    public JdbcTemplate(final DynamicConnection dynamicConn) {
        super(dynamicConn);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param typeRegistry the TypeHandlerRegistry
     */
    public JdbcTemplate(final DynamicConnection dynamicConn, TypeHandlerRegistry typeRegistry) {
        super(dynamicConn);
        this.typeRegistry = Objects.requireNonNull(typeRegistry, "typeRegistry is null.");
    }

    public boolean isResultsCaseInsensitive() {
        return this.resultsCaseInsensitive;
    }

    public void setResultsCaseInsensitive(final boolean resultsCaseInsensitive) {
        this.resultsCaseInsensitive = resultsCaseInsensitive;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public void loadSQL(final String sqlResource) throws IOException, SQLException {
        this.loadSplitSQL(null, StandardCharsets.UTF_8, sqlResource);
    }

    public void loadSQL(final Charset charset, final String sqlResource) throws IOException, SQLException {
        this.loadSplitSQL(null, charset, sqlResource);
    }

    public void loadSQL(final Reader sqlReader) throws IOException, SQLException {
        this.loadSplitSQL(null, sqlReader);
    }

    public void loadSplitSQL(final String splitChars, final String sqlResource) throws IOException, SQLException {
        this.loadSplitSQL(splitChars, StandardCharsets.UTF_8, sqlResource);
    }

    public void loadSplitSQL(final String splitChars, final Charset charset, final String sqlResource) throws IOException, SQLException {
        InputStream inStream = ResourcesUtils.getResourceAsStream(sqlResource);
        if (inStream == null) {
            String msg = "can't find resource '" + sqlResource + "'";
            if (logger.isDebugEnabled()) {
                logger.debug(msg);
            }
            throw new IOException(msg);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("loadSplitSQL resource '" + sqlResource + "', splitChars = " + splitChars);
        }

        this.loadSplitSQL(splitChars, new InputStreamReader(inStream, charset));
    }

    public void loadSplitSQL(final String splitChars, final Reader sqlReader) throws IOException, SQLException {
        if (sqlReader == null) {
            logger.warn("loadSplitSQL by Reader, the Reader is null.");
            return;
        }

        StringWriter outWriter = new StringWriter();
        IOUtils.copy(sqlReader, outWriter);

        List<String> taskList;
        if (StringUtils.isBlank(splitChars)) {
            taskList = Collections.singletonList(outWriter.toString());
        } else {
            taskList = Arrays.asList(outWriter.toString().split(splitChars));
        }
        taskList = taskList.parallelStream().filter(StringUtils::isNotBlank).collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            logger.debug("loadSplitSQL by Reader, taskSQL = " + outWriter);
        }

        for (String str : taskList) {
            if (str.trim().startsWith("--")) {
                continue;
            }
            this.execute(str);
        }
    }

    @Override
    public boolean execute(final String sql) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + sql + "].");
        }
        class ExecuteStatementCallback implements StatementCallback<Boolean>, SqlProvider {
            @Override
            public Boolean doInStatement(final Statement stmt) throws SQLException {
                return stmt.execute(sql);
            }

            @Override
            public String getSql() {
                return sql;
            }
        }
        return this.execute(new ExecuteStatementCallback());
    }

    private <T> T execute(SimpleStatementCreator sc, StatementCallback<T> action) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + getSql(sc) + "].");
        }

        return this.execute((ConnectionCallback<T>) con -> {
            try (Statement s = sc.createStatement(con)) {
                applyStatementSettings(s);
                T result = action.doInStatement(s);
                handleWarnings(s);
                return result;
            } catch (SQLException ex) {
                String sql = getSql(sc);
                if (this.isPrintStmtError()) {
                    logger.error("Failed SQL statement [" + sql + "].", ex);
                }
                throw new UncategorizedSQLException(sql, ex.getMessage(), ex);
            }
        });
    }

    @Override
    public <T> T executeCreator(final PreparedStatementCreator psc, final PreparedStatementCallback<T> action) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + getSql(psc) + "].");
        }

        return this.execute((ConnectionCallback<T>) con -> {
            try (PreparedStatement ps = psc.createPreparedStatement(con)) {
                applyStatementSettings(ps);
                T result = action.doInPreparedStatement(ps);
                handleWarnings(ps);
                return result;
            } catch (SQLException ex) {
                String sql = getSql(psc);
                if (this.isPrintStmtError()) {
                    logger.error("Failed SQL statement [" + sql + "].", ex);
                }
                throw new UncategorizedSQLException(sql, ex.getMessage(), ex);
            } finally {
                if (psc instanceof ParameterDisposer) {
                    ((ParameterDisposer) psc).cleanupParameters();
                }
            }
        });
    }

    @Override
    public <T> T executeCreator(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws SQLException {
        return this.executeCreator(psc, (PreparedStatementCallback<T>) cs -> {
            boolean retVal = cs.execute();
            if (retVal) {
                try (ResultSet rs = cs.getResultSet()) {
                    return rse.extractData(rs);
                }
            } else {
                int cnt = cs.getUpdateCount();
                return null;
            }
        });
    }

    @Override
    public void executeCreator(PreparedStatementCreator psc, RowCallbackHandler rch) throws SQLException {
        this.executeCreator(psc, new RowCallbackHandlerResultSetExtractor(rch));
    }

    @Override
    public <T> List<T> executeCreator(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws SQLException {
        List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper));
        return result == null ? Collections.emptyList() : result;
    }

    @Override
    public <T> T executeCall(CallableStatementCreator csc, ResultSetExtractor<T> rse) throws SQLException {
        return this.executeCall(csc, (CallableStatementCallback<T>) cs -> {
            boolean retVal = cs.execute();
            if (retVal) {
                try (ResultSet rs = cs.getResultSet()) {
                    return rse.extractData(rs);
                }
            } else {
                int cnt = cs.getUpdateCount();
                return null;
            }
        });
    }

    @Override
    public void executeCall(CallableStatementCreator csc, RowCallbackHandler rch) throws SQLException {
        this.executeCall(csc, (CallableStatementCallback<Void>) cs -> {
            boolean retVal = cs.execute();
            if (retVal) {
                try (ResultSet rs = cs.getResultSet()) {
                    return new RowCallbackHandlerResultSetExtractor(rch).extractData(rs);
                }
            } else {
                int cnt = cs.getUpdateCount();
                return null;
            }
        });
    }

    @Override
    public <T> List<T> executeCall(CallableStatementCreator csc, RowMapper<T> rowMapper) throws SQLException {
        return this.executeCall(csc, (CallableStatementCallback<List<T>>) cs -> {
            boolean retVal = cs.execute();
            if (retVal) {
                try (ResultSet rs = cs.getResultSet()) {
                    return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
                }
            } else {
                int cnt = cs.getUpdateCount();
                return Collections.emptyList();
            }
        });
    }

    @Override
    public <T> T executeCall(final CallableStatementCreator csc, final CallableStatementCallback<T> action) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + getSql(csc) + "].");
        }

        return this.execute((ConnectionCallback<T>) con -> {
            try (CallableStatement ps = csc.createCallableStatement(con)) {
                applyStatementSettings(ps);
                T result = action.doInCallableStatement(ps);
                handleWarnings(ps);
                return result;
            } catch (SQLException ex) {
                String sql = getSql(csc);
                if (this.isPrintStmtError()) {
                    logger.error("Failed SQL statement [" + sql + "].", ex);
                }
                throw new UncategorizedSQLException(sql, ex.getMessage(), ex);
            } finally {
                if (csc instanceof ParameterDisposer) {
                    ((ParameterDisposer) csc).cleanupParameters();
                }
            }
        });
    }

    @Override
    public <T> T executeCallback(final String sql, final PreparedStatementCallback<T> action) throws SQLException {
        PreparedStatementSetter EmptySetter = ps -> {
        };
        return this.executeCreator(this.getPreparedStatementCreator(sql, EmptySetter), action);
    }

    @Override
    public <T> T executeCallback(final String sql, final CallableStatementCallback<T> action) throws SQLException {
        CallableStatementSetter EmptySetter = ps -> {
        };
        return this.executeCall(this.getCallableStatementCreator(sql, EmptySetter), action);
    }

    @Override
    public List<Object> multipleExecute(final String sql) throws SQLException {
        return this.execute(getStatementCreator(sql), s -> {
            boolean retVal = s.execute(sql);
            return receiveMultipleResult(retVal, s);
        });
    }

    @Override
    public List<Object> multipleExecute(final String sql, final Object[] args) throws SQLException {
        PreparedStatementSetter setter = newArgPreparedStatementSetter(args);
        PreparedStatementCreator psc = getPreparedStatementCreator(sql, setter);
        return this.executeCreator(psc, (PreparedStatementCallback<List<Object>>) ps -> {
            boolean retVal = ps.execute();
            return receiveMultipleResult(retVal, ps);
        });
    }

    @Override
    public List<Object> multipleExecute(final String sql, final SqlParameterSource parameterSource) throws SQLException {
        PreparedStatementCreator psc = getPreparedStatementCreator(sql, parameterSource);
        return this.executeCreator(psc, (PreparedStatementCallback<List<Object>>) ps -> {
            boolean retVal = ps.execute();
            return receiveMultipleResult(retVal, ps);
        });
    }

    @Override
    public List<Object> multipleExecute(final String sql, final Map<String, ?> paramMap) throws SQLException {
        PreparedStatementCreator psc = getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap));
        return this.executeCreator(psc, (PreparedStatementCallback<List<Object>>) ps -> {
            boolean retVal = ps.execute();
            return receiveMultipleResult(retVal, ps);
        });
    }

    @Override
    public List<Object> multipleExecute(final String sql, final PreparedStatementSetter setter) throws SQLException {
        PreparedStatementCreator psc = getPreparedStatementCreator(sql, setter);
        return this.executeCreator(psc, (PreparedStatementCallback<List<Object>>) ps -> {
            boolean retVal = ps.execute();
            return receiveMultipleResult(retVal, ps);
        });
    }

    private List<Object> receiveMultipleResult(boolean retVal, Statement s) throws SQLException {
        if (logger.isTraceEnabled()) {
            logger.trace("statement.execute() returned '" + retVal + "'");
        }

        TypeHandlerRegistry typeRegistry = getTypeRegistry();

        List<Object> resultList = new ArrayList<>();
        if (retVal) {
            try (ResultSet resultSet = s.getResultSet()) {
                ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper(isResultsCaseInsensitive(), typeRegistry);
                ReturnSqlParameter result = SqlParameterUtils.withReturnResult("TMP", new RowMapperResultSetExtractor<>(columnMapRowMapper));
                resultList.add(processResultSet(typeRegistry, isResultsCaseInsensitive(), resultSet, result));
            }
        } else {
            resultList.add(s.getUpdateCount());
        }
        while ((s.getMoreResults()) || (s.getUpdateCount() != -1)) {
            int updateCount = s.getUpdateCount();
            try (ResultSet resultSet = s.getResultSet()) {
                if (resultSet != null) {
                    resultList.add(processResultSet(typeRegistry, isResultsCaseInsensitive(), resultSet, null));
                } else {
                    resultList.add(updateCount);
                }
            }
        }
        return resultList;
    }

    @Override
    public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws SQLException {
        return this.execute(getStatementCreator(sql), stmt -> {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                return rse.extractData(rs);
            }
        });
    }

    @Override
    public <T> T query(final String sql, final Object[] args, final ResultSetExtractor<T> rse) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args)), rse);
    }

    @Override
    public <T> T query(final String sql, final SqlParameterSource paramSource, final ResultSetExtractor<T> rse) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, paramSource), rse);
    }

    @Override
    public <T> T query(final String sql, final Map<String, ?> paramMap, final ResultSetExtractor<T> rse) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap)), rse);
    }

    @Override
    public <T> T query(final String sql, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, pss), rse);
    }

    @Override
    public void query(final String sql, final RowCallbackHandler rch) throws SQLException {
        boolean res = this.execute(this.getStatementCreator(sql), stmt -> {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                new RowCallbackHandlerResultSetExtractor(rch).extractData(rs);
                return true;
            }
        });
    }

    @Override
    public void query(final String sql, final Object[] args, final RowCallbackHandler rch) throws SQLException {
        this.executeCreator(this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args)), new RowCallbackHandlerResultSetExtractor(rch));
    }

    @Override
    public void query(final String sql, final SqlParameterSource paramSource, final RowCallbackHandler rch) throws SQLException {
        this.executeCreator(this.getPreparedStatementCreator(sql, paramSource), new RowCallbackHandlerResultSetExtractor(rch));
    }

    @Override
    public void query(final String sql, final Map<String, ?> paramMap, final RowCallbackHandler rch) throws SQLException {
        this.executeCreator(this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap)), new RowCallbackHandlerResultSetExtractor(rch));

    }

    @Override
    public void query(final String sql, final PreparedStatementSetter setter, final RowCallbackHandler rch) throws SQLException {
        this.executeCreator(this.getPreparedStatementCreator(sql, setter), new RowCallbackHandlerResultSetExtractor(rch));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) throws SQLException {
        return this.execute(this.getStatementCreator(sql), stmt -> {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
            }
        });
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Object[] args, final RowMapper<T> rowMapper) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args)), new RowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final SqlParameterSource paramSource, final RowMapper<T> rowMapper) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, paramSource), new RowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Map<String, ?> paramMap, final RowMapper<T> rowMapper) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap)), new RowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, setter), new RowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Class<T> elementType) throws SQLException {
        return this.query(sql, this.createBeanResultSetExtractor(elementType));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Object[] args, final Class<T> elementType) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args)), this.createBeanResultSetExtractor(elementType));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final SqlParameterSource paramSource, final Class<T> elementType) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, paramSource), this.createBeanResultSetExtractor(elementType));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Map<String, ?> paramMap, final Class<T> elementType) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap)), this.createBeanResultSetExtractor(elementType));
    }

    @Override
    public <T> List<T> queryForList(String sql, PreparedStatementSetter setter, Class<T> elementType) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, setter), this.createBeanResultSetExtractor(elementType));
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql) throws SQLException {
        return this.query(sql, new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql, final Object[] args) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args));
        return this.executeCreator(psc, new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql, final SqlParameterSource paramSource) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, paramSource), new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql, final Map<String, ?> paramMap) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap)), new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql, final PreparedStatementSetter setter) throws SQLException {
        return this.executeCreator(this.getPreparedStatementCreator(sql, setter), new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
    }

    @Override
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper) throws SQLException {
        return requiredSingleResult(this.query(sql, new RowMapperResultSetExtractor<>(rowMapper, 1)));
    }

    @Override
    public <T> T queryForObject(final String sql, final Object[] args, final RowMapper<T> rowMapper) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args));
        List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return requiredSingleResult(result);
    }

    @Override
    public <T> T queryForObject(final String sql, final SqlParameterSource paramSource, final RowMapper<T> rowMapper) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, paramSource);
        List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return requiredSingleResult(result);
    }

    @Override
    public <T> T queryForObject(final String sql, final Map<String, ?> paramMap, final RowMapper<T> rowMapper) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap));
        List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return requiredSingleResult(result);
    }

    @Override
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, setter);
        List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return requiredSingleResult(result);
    }

    @Override
    public <T> T queryForObject(final String sql, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, this.createBeanRowMapper(requiredType));
    }

    @Override
    public <T> T queryForObject(final String sql, final Object[] args, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, args, this.createBeanRowMapper(requiredType));
    }

    @Override
    public <T> T queryForObject(final String sql, final SqlParameterSource paramSource, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, paramSource, this.createBeanRowMapper(requiredType));
    }

    @Override
    public <T> T queryForObject(final String sql, final Map<String, ?> paramMap, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, paramMap, this.createBeanRowMapper(requiredType));
    }

    @Override
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, setter, this.createBeanRowMapper(requiredType));
    }

    @Override
    public Map<String, Object> queryForMap(final String sql) throws SQLException {
        return this.queryForObject(sql, this.createMapRowMapper());
    }

    @Override
    public Map<String, Object> queryForMap(final String sql, final Object[] args) throws SQLException {
        return this.queryForObject(sql, args, this.createMapRowMapper());
    }

    @Override
    public Map<String, Object> queryForMap(final String sql, final SqlParameterSource paramSource) throws SQLException {
        return this.queryForObject(sql, paramSource, this.createMapRowMapper());
    }

    @Override
    public Map<String, Object> queryForMap(final String sql, final Map<String, ?> paramMap) throws SQLException {
        return this.queryForObject(sql, paramMap, this.createMapRowMapper());
    }

    @Override
    public Map<String, Object> queryForMap(String sql, PreparedStatementSetter setter) throws SQLException {
        return this.queryForObject(sql, setter, this.createMapRowMapper());
    }

    @Override
    public long queryForLong(final String sql) throws SQLException {
        Number number = this.queryForObject(sql, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public long queryForLong(final String sql, final Object[] args) throws SQLException {
        Number number = this.queryForObject(sql, args, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public long queryForLong(final String sql, final SqlParameterSource paramSource) throws SQLException {
        Number number = this.queryForObject(sql, paramSource, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public long queryForLong(final String sql, final Map<String, ?> paramMap) throws SQLException {
        Number number = this.queryForObject(sql, paramMap, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public long queryForLong(final String sql, final PreparedStatementSetter setter) throws SQLException {
        Number number = this.queryForObject(sql, setter, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public int queryForInt(final String sql) throws SQLException {
        Number number = this.queryForObject(sql, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public int queryForInt(final String sql, final Object[] args) throws SQLException {
        Number number = this.queryForObject(sql, args, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public int queryForInt(final String sql, final SqlParameterSource paramSource) throws SQLException {
        Number number = this.queryForObject(sql, paramSource, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public int queryForInt(final String sql, final Map<String, ?> paramMap) throws SQLException {
        Number number = this.queryForObject(sql, paramMap, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public int queryForInt(final String sql, final PreparedStatementSetter setter) throws SQLException {
        Number number = this.queryForObject(sql, setter, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public String queryForString(final String sql) throws SQLException {
        return this.queryForObject(sql, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public String queryForString(final String sql, final Object[] args) throws SQLException {
        return this.queryForObject(sql, args, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public String queryForString(final String sql, final SqlParameterSource paramSource) throws SQLException {
        return this.queryForObject(sql, paramSource, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public String queryForString(final String sql, final Map<String, ?> paramMap) throws SQLException {
        return this.queryForObject(sql, paramMap, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public String queryForString(final String sql, final PreparedStatementSetter setter) throws SQLException {
        return this.queryForObject(sql, setter, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        return this.execute(getStatementCreator(sql), stmt -> stmt.executeUpdate(sql));
    }

    @Override
    public int executeUpdate(final String sql, final Object[] args) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, this.newArgPreparedStatementSetter(args));
        return this.executeCreator(psc, (PreparedStatementCallback<Integer>) PreparedStatement::executeUpdate);
    }

    @Override
    public int executeUpdate(final String sql, final SqlParameterSource paramSource) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, paramSource);
        return this.executeCreator(psc, (PreparedStatementCallback<Integer>) PreparedStatement::executeUpdate);
    }

    @Override
    public int executeUpdate(final String sql, final Map<String, ?> paramMap) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap));
        return this.executeCreator(psc, (PreparedStatementCallback<Integer>) PreparedStatement::executeUpdate);
    }

    @Override
    public int executeUpdate(String sql, PreparedStatementSetter pss) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, pss);
        return this.executeCreator(psc, (PreparedStatementCallback<Integer>) PreparedStatement::executeUpdate);
    }

    /** Create a new RowMapper for reading columns as key-value pairs. */
    protected RowMapper<Map<String, Object>> createMapRowMapper() {
        return new ColumnMapRowMapper(this.isResultsCaseInsensitive(), this.getTypeRegistry()) {
            @Override
            protected Map<String, Object> createColumnMap(final int columnCount) {
                return createResultsMap();
            }
        };
    }

    /** Create a new RowMapper for reading columns as Bean pairs. */
    protected <T> RowMapper<T> createBeanRowMapper(final Class<T> requiredType) {
        Objects.requireNonNull(requiredType, "requiredType is null.");
        if (Map.class.isAssignableFrom(requiredType)) {
            return (RowMapper<T>) this.createMapRowMapper();
        }

        if (TypeHandlerRegistry.DEFAULT.hasTypeHandler(requiredType) || requiredType.isEnum()) {
            return this.createSingleColumnRowMapper(requiredType);
        }

        return new MappingRowMapper<>(requiredType, this.getTypeRegistry());
    }

    /** Create a new RowMapper for reading result objects from a single column.*/
    protected <T> RowMapper<T> createSingleColumnRowMapper(final Class<T> requiredType) {
        Objects.requireNonNull(requiredType, "requiredType is null.");
        return new SingleColumnRowMapper<>(requiredType, this.getTypeRegistry());
    }

    /** Create a new RowMapper for reading columns as Bean pairs. */
    protected <T> ResultSetExtractor<List<T>> createBeanResultSetExtractor(final Class<T> requiredType) {
        Objects.requireNonNull(requiredType, "requiredType is null.");
        if (Map.class.isAssignableFrom(requiredType)) {
            RowMapper<T> mapRowMapper = (RowMapper<T>) this.createMapRowMapper();
            return new RowMapperResultSetExtractor<>(mapRowMapper);
        }

        if (TypeHandlerRegistry.DEFAULT.hasTypeHandler(requiredType) || requiredType.isEnum()) {
            RowMapper<T> mapRowMapper = this.createSingleColumnRowMapper(requiredType);
            return new RowMapperResultSetExtractor<>(mapRowMapper);
        }

        return new MappingResultSetExtractor<>(requiredType, this.getTypeRegistry());
    }

    /** Build a StatementCreator based on the given SQL. */
    protected SimpleStatementCreator getStatementCreator(final String sql) {
        Objects.requireNonNull(sql, "SQL must not be null.");
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL query [" + sql + "].");
        }
        return new SimpleStatementCreator(sql);
    }

    /** Build a PreparedStatementCreator based on the given SQL and args parameters. */
    protected PreparedStatementCreator getPreparedStatementCreator(final String sql) {
        Objects.requireNonNull(sql, "SQL must not be null.");
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL query [" + sql + "].");
        }
        return new SimplePreparedStatementCreator(sql, null);
    }

    /** Build a PreparedStatementCreator based on the given SQL and args parameters. */
    protected PreparedStatementCreator getPreparedStatementCreator(final String sql, final PreparedStatementSetter setter) {
        Objects.requireNonNull(sql, "SQL must not be null.");
        Objects.requireNonNull(setter, "PreparedStatementSetter must not be null.");
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL query [" + sql + "].");
        }
        return new SimplePreparedStatementCreator(sql, setter);
    }

    /** Build a PreparedStatementCreator based on the given SQL and named parameters. */
    protected PreparedStatementCreator getPreparedStatementCreator(final String sql, final SqlParameterSource paramSource) {
        Objects.requireNonNull(sql, "SQL must not be null.");
        Objects.requireNonNull(paramSource, "SqlParameterSource must not be null.");
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL query [" + sql + "].");
        }
        return new MapPreparedStatementCreator(sql, paramSource);
    }

    /** Build a PreparedStatementCreator based on the given SQL and named parameters. */
    protected CallableStatementCreator getCallableStatementCreator(final String sql, final CallableStatementSetter setter) {
        Objects.requireNonNull(sql, "SQL must not be null.");
        Objects.requireNonNull(setter, "PreparedStatementSetter must not be null.");
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL query [" + sql + "].");
        }
        return new SimpleCallableStatementCreator(sql, setter);
    }

    @Override
    public int[] executeBatch(final String[] sql) throws SQLException {
        if (sql == null || sql.length == 0) {
            return new int[0];
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing SQL batch update of " + sql.length + " statements");
        }

        class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {
            private String currSql;

            @Override
            public int[] doInStatement(final Statement stmt) throws SQLException {
                DatabaseMetaData dbmd = stmt.getConnection().getMetaData();
                int[] rowsAffected = new int[sql.length];
                if (dbmd.supportsBatchUpdates()) {
                    /*连接支持批处理*/
                    for (String sqlStmt : sql) {
                        this.currSql = sqlStmt;
                        stmt.addBatch(sqlStmt);
                    }
                    rowsAffected = stmt.executeBatch();
                } else {
                    /*连接不支持批处理*/
                    for (int i = 0; i < sql.length; i++) {
                        this.currSql = sql[i];
                        if (!stmt.execute(sql[i])) {
                            rowsAffected[i] = stmt.getUpdateCount();
                        } else {
                            throw new UncategorizedSQLException(sql[i], "Invalid batch SQL statement");
                        }
                    }
                }
                return rowsAffected;
            }

            @Override
            public String getSql() {
                return this.currSql;
            }
        }
        return this.execute(new BatchUpdateStatementCallback());
    }

    @Override
    public int[] executeBatch(String sql, Object[][] batchValues) throws SQLException {
        return this.executeBatch(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int idx = 1;
                TypeHandlerRegistry typeRegistry = getTypeRegistry();
                for (Object value : batchValues[i]) {
                    if (value == null) {
                        ps.setObject(idx, null);
                    } else {
                        typeRegistry.setParameterValue(ps, idx, value);
                    }
                    idx++;
                }
            }

            @Override
            public int getBatchSize() {
                return batchValues.length;
            }
        });
    }

    @Override
    public int[] executeBatch(final String sql, final SqlParameterSource[] batchArgs) throws SQLException {
        if (batchArgs == null || batchArgs.length == 0) {
            return new int[0];
        }
        return this.executeBatch(sql, new SqlParameterSourceBatchPreparedStatementSetter(sql, batchArgs));
    }

    @Override
    public int[] executeBatch(final String sql, final Map<String, ?>[] batchValues) throws SQLException {
        if (batchValues == null || batchValues.length == 0) {
            return new int[0];
        }

        SqlParameterSource[] batchArgs = new SqlParameterSource[batchValues.length];
        int i = 0;
        for (Map<String, ?> values : batchValues) {
            batchArgs[i] = new MapSqlParameterSource(values);
            i++;
        }
        return this.executeBatch(sql, new SqlParameterSourceBatchPreparedStatementSetter(sql, batchArgs));
    }

    @Override
    public int[] executeBatch(final String sql, final BatchPreparedStatementSetter pss) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing SQL batch update [" + sql + "].");
        }
        String buildSql = getParsedSql(sql).buildSql(null);

        return this.executeCreator(getPreparedStatementCreator(buildSql), (PreparedStatementCallback<int[]>) ps -> {
            try {
                int batchSize = pss.getBatchSize();
                DatabaseMetaData dbMetaData = ps.getConnection().getMetaData();
                if (dbMetaData.supportsBatchUpdates()) {
                    for (int i = 0; i < batchSize; i++) {
                        pss.setValues(ps, i);
                        if (pss.isBatchExhausted(i)) {
                            break;
                        }
                        ps.addBatch();
                    }
                    return ps.executeBatch();
                } else {
                    List<Integer> rowsAffected = new ArrayList<>();
                    for (int i = 0; i < batchSize; i++) {
                        pss.setValues(ps, i);
                        if (pss.isBatchExhausted(i)) {
                            break;
                        }
                        rowsAffected.add(ps.executeUpdate());
                    }
                    int[] rowsAffectedArray = new int[rowsAffected.size()];
                    for (int i = 0; i < rowsAffectedArray.length; i++) {
                        rowsAffectedArray[i] = rowsAffected.get(i);
                    }
                    return rowsAffectedArray;
                }
            } finally {
                if (pss instanceof ParameterDisposer) {
                    ((ParameterDisposer) pss).cleanupParameters();
                }
            }
        });
    }

    @Override
    public <T> T call(String callString, CallableStatementSetter setter, CallableStatementCallback<T> action) throws SQLException {
        return this.executeCall(this.getCallableStatementCreator(callString, setter), action);
    }

    @Override
    public Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws SQLException {
        return this.executeCall(csc, new SimpleCallableStatementCallback(MultipleProcessType.ALL, declaredParameters));
    }

    @Override
    public Map<String, Object> call(String callString, List<SqlParameter> declaredParameters) throws SQLException {
        final SimpleCallableStatementCallback csc = new SimpleCallableStatementCallback(MultipleProcessType.ALL, declaredParameters) {
            @Override
            public boolean isResultsCaseInsensitive() {
                return JdbcTemplate.this.isResultsCaseInsensitive();
            }

            @Override
            protected Map<String, Object> createResultsMap() {
                return JdbcTemplate.this.createResultsMap();
            }
        };
        return this.executeCallback(callString, csc);
    }

    /**
     * Process the given ResultSet from a stored procedure.
     * @param rs the ResultSet to process
     * @param param the corresponding stored procedure parameter
     * @return a Map that contains returned results
     */
    protected static Object processResultSet(TypeHandlerRegistry typeRegistry, boolean caseInsensitive, ResultSet rs, ReturnSqlParameter param) throws SQLException {
        if (rs != null) {
            if (param != null) {
                if (param.getRowMapper() != null) {
                    RowMapper<?> rowMapper = param.getRowMapper();
                    return (new RowMapperResultSetExtractor<>(rowMapper)).extractData(rs);
                } else if (param.getRowCallbackHandler() != null) {
                    RowCallbackHandler rch = param.getRowCallbackHandler();
                    new RowCallbackHandlerResultSetExtractor(rch).extractData(rs);
                    return "ResultSet returned from stored procedure was processed";
                } else if (param.getResultSetExtractor() != null) {
                    return param.getResultSetExtractor().extractData(rs);
                }
            } else {
                return new ColumnMapResultSetExtractor(0, typeRegistry, caseInsensitive).extractData(rs);
            }
        }
        return null;
    }

    /**创建用于保存结果集的数据Map。*/
    protected Map<String, Object> createResultsMap() {
        if (this.isResultsCaseInsensitive()) {
            return new LinkedCaseInsensitiveMap<>();
        } else {
            return new LinkedHashMap<>();
        }
    }

    /**获取SQL文本*/
    private static String getSql(final Object sqlProvider) {
        if (sqlProvider instanceof SqlProvider) {
            return ((SqlProvider) sqlProvider).getSql();
        } else {
            return null;
        }
    }

    /** 至返回结果集中的一条数据。*/
    private static <T> T requiredSingleResult(final Collection<T> results) throws SQLException {
        if (results == null || results.isEmpty()) {
            return null;
        }
        int size = results.size();
        if (size > 1) {
            throw new SQLException("Incorrect record count: expected 1, actual " + size);
        }
        return results.iterator().next();
    }

    /** 接口 StatementCreator 的简单实现，目的是实现 SqlProvider 接口可以打印 SQL 日志语句 */
    private static class SimpleStatementCreator implements SqlProvider {
        private final String sql;

        public SimpleStatementCreator(final String sql) {
            this.sql = Objects.requireNonNull(sql, "SQL must not be null");
        }

        public Statement createStatement(Connection con) throws SQLException {
            return con.createStatement();
        }

        @Override
        public String getSql() {
            return this.sql;
        }

    }

    /** 接口 {@link PreparedStatementCreator} 的简单实现，目的是根据 SQL 语句创建 {@link PreparedStatement}对象。*/
    private static class SimplePreparedStatementCreator implements PreparedStatementCreator, ParameterDisposer, SqlProvider {
        private final String                  sql;
        private final PreparedStatementSetter setter;

        public SimplePreparedStatementCreator(String sql, PreparedStatementSetter setter) {
            this.sql = Objects.requireNonNull(sql, "SQL must not be null");
            this.setter = setter;
        }

        @Override
        public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(this.sql);
            if (this.setter != null) {
                this.setter.setValues(ps);
            }
            return ps;
        }

        @Override
        public String getSql() {
            return this.sql;
        }

        @Override
        public void cleanupParameters() {
            if (this.setter instanceof ParameterDisposer) {
                ((ParameterDisposer) this.setter).cleanupParameters();
            }
        }
    }

    /** Simple adapter for CallableStatementCreator, allowing to use a plain SQL statement. */
    private static class SimpleCallableStatementCreator implements CallableStatementCreator, ParameterDisposer, SqlProvider {
        private final String                  sql;
        private final CallableStatementSetter setter;

        public SimpleCallableStatementCreator(String sql, CallableStatementSetter setter) {
            this.sql = Objects.requireNonNull(sql, "Call string must not be null");
            this.setter = setter;
        }

        @Override
        public CallableStatement createCallableStatement(Connection con) throws SQLException {
            if (!con.getMetaData().supportsStoredProcedures()) {
                throw new UnsupportedOperationException("target DataSource Unsupported.");
            }
            CallableStatement cs = con.prepareCall(this.sql);
            if (this.setter != null) {
                this.setter.setValues(cs);
            }
            return cs;
        }

        @Override
        public String getSql() {
            return this.sql;
        }

        @Override
        public void cleanupParameters() {
            if (this.setter instanceof ParameterDisposer) {
                ((ParameterDisposer) this.setter).cleanupParameters();
            }
        }
    }

    /** 接口 {@link PreparedStatementCreator} 的简单实现，目的是根据 SQL 语句创建 {@link PreparedStatement}对象。*/
    private class MapPreparedStatementCreator implements PreparedStatementCreator, ParameterDisposer, SqlProvider {
        private final ParsedSql          parsedSql;
        private final SqlParameterSource paramSource;

        public MapPreparedStatementCreator(final String originalSql, final SqlParameterSource paramSource) {
            Objects.requireNonNull(originalSql, "SQL must not be null");
            this.parsedSql = getParsedSql(originalSql);
            this.paramSource = paramSource;
        }

        @Override
        public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
            //1.根据参数信息生成最终会执行的SQL语句.
            String sqlToUse = this.parsedSql.buildSql(this.paramSource);
            //2.确定参数对象
            Object[] paramArray = this.parsedSql.buildValues(this.paramSource);
            //3.创建PreparedStatement对象，并设置参数
            PreparedStatement ps = con.prepareStatement(sqlToUse);
            TypeHandlerRegistry typeRegistry = getTypeRegistry();
            for (int i = 0; i < paramArray.length; i++) {
                typeRegistry.setParameterValue(ps, i + 1, paramArray[i]);
            }
            StatementSetterUtils.cleanupParameters(paramArray);
            return ps;
        }

        @Override
        public String getSql() {
            return this.parsedSql.getOriginalSql();
        }

        @Override
        public void cleanupParameters() {
            if (this.paramSource instanceof ParameterDisposer) {
                ((ParameterDisposer) this.paramSource).cleanupParameters();
            }
        }
    }

    /** 接口 {@link BatchPreparedStatementSetter} 的简单实现，目的是设置批量操作 */
    private class SqlParameterSourceBatchPreparedStatementSetter implements BatchPreparedStatementSetter, ParameterDisposer {
        private final ParsedSql            parsedSql;
        private final SqlParameterSource[] batchArgs;

        public SqlParameterSourceBatchPreparedStatementSetter(final String sql, final SqlParameterSource[] batchArgs) {
            this.parsedSql = getParsedSql(sql);
            this.batchArgs = batchArgs;
        }

        @Override
        public void setValues(final PreparedStatement ps, final int index) throws SQLException {
            SqlParameterSource paramSource = this.batchArgs[index];
            //1.确定参数对象
            Object[] sqlValue = this.parsedSql.buildValues(paramSource);
            //2.设置参数
            int sqlColIndex = 1;
            TypeHandlerRegistry typeRegistry = getTypeRegistry();

            for (Object element : sqlValue) {
                if (element instanceof InSqlParameter) {
                    Object value = ((InSqlParameter) element).getValue();
                    Integer jdbcType = ((InSqlParameter) element).getJdbcType();
                    TypeHandler typeHandler = ((InSqlParameter) element).getTypeHandler();
                    if (typeHandler != null && jdbcType != null) {
                        typeHandler.setParameter(ps, sqlColIndex++, value, jdbcType);
                        continue;
                    } else if (typeHandler != null) {
                        if (value == null) {
                            ps.setObject(sqlColIndex++, null);
                        } else {
                            typeHandler.setParameter(ps, sqlColIndex++, value, TypeHandlerRegistry.toSqlType(value.getClass()));
                        }
                        continue;
                    }
                    element = value;
                }

                typeRegistry.setParameterValue(ps, sqlColIndex++, element);
            }
        }

        @Override
        public int getBatchSize() {
            return this.batchArgs.length;
        }

        @Override
        public void cleanupParameters() {
            for (SqlParameterSource batchItem : this.batchArgs) {
                if (batchItem instanceof ParameterDisposer) {
                    ((ParameterDisposer) batchItem).cleanupParameters();
                }
            }
        }
    }

    /** Create a new PreparedStatementSetter.*/
    protected PreparedStatementSetter newArgPreparedStatementSetter(final Object[] args) {
        return new ArgPreparedStatementSetter(args, getTypeRegistry());
    }

    /* Map of original SQL String to ParsedSql representation */
    private final Map<String, ParsedSql> parsedSqlCache = new HashMap<>();

    /* Obtain a parsed representation of the given SQL statement.*/
    protected ParsedSql getParsedSql(String originalSql) {
        synchronized (this.parsedSqlCache) {
            ParsedSql parsedSql = this.parsedSqlCache.get(originalSql);
            if (parsedSql == null) {
                parsedSql = ParsedSql.getParsedSql(originalSql);
                this.parsedSqlCache.put(originalSql, parsedSql);
            }
            return parsedSql;
        }
    }
}
