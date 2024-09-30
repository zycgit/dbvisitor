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
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.dynamic.args.SqlArgDisposer;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.error.UncategorizedSQLException;
import net.hasor.dbvisitor.jdbc.*;
import net.hasor.dbvisitor.jdbc.extractor.CallableMultipleResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.PreparedMultipleResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowCallbackHandlerResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.MappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.MappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.types.SqlArg;
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
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-10-12
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
    private static final Logger         logger                 = LoggerFactory.getLogger(JdbcTemplate.class);
    /* 当JDBC 结果集中如出现相同的列名仅仅大小写不同时。是否保留大小写列名敏感。
     * 如果为 true 表示不敏感，并且结果集Map中保留两个记录。如果为 false 则表示敏感，如出现冲突列名后者将会覆盖前者。*/
    private              boolean        resultsCaseInsensitive = true;
    private              DynamicContext registry               = DynamicContext.DEFAULT;

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
     * @param registry the DynamicContext
     */
    public JdbcTemplate(final DataSource dataSource, DynamicContext registry) {
        super(dataSource);
        this.registry = Objects.requireNonNull(registry, "registry is null.");
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
     * @param registry the DynamicContext
     */
    public JdbcTemplate(final Connection conn, DynamicContext registry) {
        super(conn);
        this.registry = Objects.requireNonNull(registry, "registry is null.");
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
     * @param registry the DynamicContext
     */
    public JdbcTemplate(final DynamicConnection dynamicConn, DynamicContext registry) {
        super(dynamicConn);
        this.registry = Objects.requireNonNull(registry, "registry is null.");
    }

    public boolean isResultsCaseInsensitive() {
        return this.resultsCaseInsensitive;
    }

    public void setResultsCaseInsensitive(final boolean resultsCaseInsensitive) {
        this.resultsCaseInsensitive = resultsCaseInsensitive;
    }

    public DynamicContext getRegistry() {
        return this.registry;
    }

    public void setRegistry(DynamicContext registry) {
        this.registry = registry;
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
    public void execute(final String sql) throws SQLException {
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
        this.execute(new ExecuteStatementCallback());
    }

    protected <T> T executeCreator(final PreparedStatementCreator psc, final PreparedStatementCallback<T> action) throws SQLException {
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
                if (psc instanceof SqlArgDisposer) {
                    ((SqlArgDisposer) psc).cleanupParameters();
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
    public <T> T executeCreator(CallableStatementCreator csc, CallableStatementCallback<T> action) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + getSql(csc) + "].");
        }

        return this.execute((ConnectionCallback<T>) con -> {
            try (CallableStatement cs = csc.createCallableStatement(con)) {
                applyStatementSettings(cs);
                T result = action.doInCallableStatement(cs);
                handleWarnings(cs);
                return result;
            } catch (SQLException ex) {
                String sql = getSql(csc);
                if (this.isPrintStmtError()) {
                    logger.error("Failed SQL statement [" + sql + "].", ex);
                }
                throw new UncategorizedSQLException(sql, ex.getMessage(), ex);
            } finally {
                if (csc instanceof SqlArgDisposer) {
                    ((SqlArgDisposer) csc).cleanupParameters();
                }
            }
        });
    }

    @Override
    public Map<String, Object> call(String callString) throws SQLException {
        return this.call(callString, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public Map<String, Object> call(String callString, Object args) throws SQLException {
        if (args instanceof CallableStatementSetter) {
            throw new UnsupportedOperationException("please use method call(String, CallableStatementSetter, CallableStatementCallback<T>).");
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(callString);
            SqlBuilder buildSql = parsedSql.buildQuery(toSqlArgSource(args), this.getRegistry());

            CallableStatementCreator creator = this.getCallableStatementCreator(buildSql.getSqlString(), null);
            CallableMultipleResultSetExtractor callback = new CallableMultipleResultSetExtractor(parsedSql, buildSql.getArgs());

            return this.executeCreator(creator, callback);
        }
    }

    @Override
    public <T> T call(String callString, CallableStatementSetter args, CallableStatementCallback<T> callback) throws SQLException {
        return this.executeCreator(this.getCallableStatementCreator(callString, args), callback);
    }

    @Override
    public Map<String, Object> multipleExecute(final String sql) throws SQLException {
        return this.multipleExecute(sql, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public Map<String, Object> multipleExecute(final String sql, final Object args) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.multipleExecute(sql, (PreparedStatementSetter) args);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            SqlBuilder buildSql = parsedSql.buildQuery(argSource, this.getRegistry());

            String sqlToUse = buildSql.getSqlString();
            Object[] paramArray = buildSql.getArgs();
            try {
                return this.executeCreator(con -> {
                    PreparedStatement ps = con.prepareStatement(sqlToUse);
                    if (paramArray.length > 0) {
                        TypeHandlerRegistry typeRegistry = getRegistry().getTypeRegistry();
                        for (int i = 0; i < paramArray.length; i++) {
                            typeRegistry.setParameterValue(ps, i + 1, paramArray[i]);
                        }
                    }
                    return ps;
                }, (PreparedStatementCallback<Map<String, Object>>) ps -> {
                    return new PreparedMultipleResultSetExtractor(parsedSql).doInPreparedStatement(ps);
                });
            } finally {
                StatementSetterUtils.cleanupParameters(paramArray);
            }
        }
    }

    @Override
    public Map<String, Object> multipleExecute(final String sql, final PreparedStatementSetter args) throws SQLException {
        PreparedStatementCreator psc = getPreparedStatementCreator(sql, args);
        return this.executeCreator(psc, (PreparedStatementCallback<Map<String, Object>>) ps -> {
            return new PreparedMultipleResultSetExtractor().doInPreparedStatement(ps);
        });
    }

    @Override
    public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws SQLException {
        return this.query(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, rse);
    }

    @Override
    public <T> T query(final String sql, final Object args, final ResultSetExtractor<T> rse) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.query(sql, (PreparedStatementSetter) args, rse);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            return this.executeCreator(this.getPreparedStatementCreator(parsedSql, argSource), rse);
        }
    }

    @Override
    public <T> T query(final String sql, final PreparedStatementSetter args, final ResultSetExtractor<T> rse) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        return this.executeCreator(psc, rse);
    }

    @Override
    public void query(final String sql, final RowCallbackHandler rch) throws SQLException {
        this.query(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, rch);
    }

    @Override
    public void query(final String sql, final Object args, final RowCallbackHandler rch) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            this.query(sql, (PreparedStatementSetter) args, rch);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            PreparedStatementCreator psc = getPreparedStatementCreator(parsedSql, argSource);
            this.executeCreator(psc, new RowCallbackHandlerResultSetExtractor(rch));
        }
    }

    @Override
    public void query(final String sql, final PreparedStatementSetter args, final RowCallbackHandler rch) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        this.executeCreator(psc, new RowCallbackHandlerResultSetExtractor(rch));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) throws SQLException {
        return this.queryForList(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, rowMapper);
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Object args, final RowMapper<T> rowMapper) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.queryForList(sql, (PreparedStatementSetter) args, rowMapper);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            PreparedStatementCreator psc = getPreparedStatementCreator(parsedSql, argSource);
            return this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper));
        }
    }

    @Override
    public <T> List<T> queryForList(final String sql, final PreparedStatementSetter args, final RowMapper<T> rowMapper) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        return this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Class<T> elementType) throws SQLException {
        return this.queryForList(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, elementType);
    }

    @Override
    public <T> List<T> queryForList(final String sql, final Object args, final Class<T> elementType) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.queryForList(sql, (PreparedStatementSetter) args, elementType);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            PreparedStatementCreator psc = getPreparedStatementCreator(parsedSql, argSource);
            return this.executeCreator(psc, this.createBeanResultSetExtractor(elementType));
        }
    }

    @Override
    public <T> List<T> queryForList(final String sql, final PreparedStatementSetter args, final Class<T> elementType) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        return this.executeCreator(psc, this.createBeanResultSetExtractor(elementType));
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql) throws SQLException {
        return this.queryForList(sql, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql, final Object args) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.queryForList(sql, (PreparedStatementSetter) args);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            PreparedStatementCreator psc = getPreparedStatementCreator(parsedSql, argSource);
            return this.executeCreator(psc, new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(final String sql, final PreparedStatementSetter args) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        return this.executeCreator(psc, new RowMapperResultSetExtractor<>(this.createMapRowMapper()));
    }

    @Override
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper) throws SQLException {
        return this.queryForObject(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, rowMapper);
    }

    @Override
    public <T> T queryForObject(final String sql, final Object args, final RowMapper<T> rowMapper) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.queryForObject(sql, (PreparedStatementSetter) args, rowMapper);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            PreparedStatementCreator psc = getPreparedStatementCreator(parsedSql, argSource);
            List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper, 1));
            return requiredSingleResult(result);
        }
    }

    @Override
    public <T> T queryForObject(final String sql, final PreparedStatementSetter args, final RowMapper<T> rowMapper) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        List<T> result = this.executeCreator(psc, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return requiredSingleResult(result);
    }

    @Override
    public <T> T queryForObject(final String sql, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, this.createBeanRowMapper(requiredType));
    }

    @Override
    public <T> T queryForObject(final String sql, final Object args, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, args, this.createBeanRowMapper(requiredType));
    }

    @Override
    public <T> T queryForObject(final String sql, final PreparedStatementSetter args, final Class<T> requiredType) throws SQLException {
        return this.queryForObject(sql, args, this.createBeanRowMapper(requiredType));
    }

    @Override
    public Map<String, Object> queryForMap(final String sql) throws SQLException {
        return this.queryForObject(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, this.createMapRowMapper());
    }

    @Override
    public Map<String, Object> queryForMap(final String sql, final Object args) throws SQLException {
        return this.queryForObject(sql, args, this.createMapRowMapper());
    }

    @Override
    public Map<String, Object> queryForMap(final String sql, final PreparedStatementSetter args) throws SQLException {
        return this.queryForObject(sql, args, this.createMapRowMapper());
    }

    @Override
    public long queryForLong(final String sql) throws SQLException {
        Number number = this.queryForObject(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public long queryForLong(final String sql, final Object args) throws SQLException {
        Number number = this.queryForObject(sql, args, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public long queryForLong(final String sql, final PreparedStatementSetter args) throws SQLException {
        Number number = this.queryForObject(sql, args, this.createSingleColumnRowMapper(long.class));
        return number != null ? number.longValue() : 0;
    }

    @Override
    public int queryForInt(final String sql) throws SQLException {
        Number number = this.queryForObject(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public int queryForInt(final String sql, final Object args) throws SQLException {
        Number number = this.queryForObject(sql, args, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public int queryForInt(final String sql, final PreparedStatementSetter args) throws SQLException {
        Number number = this.queryForObject(sql, args, this.createSingleColumnRowMapper(int.class));
        return number != null ? number.intValue() : 0;
    }

    @Override
    public String queryForString(final String sql) throws SQLException {
        return this.queryForObject(sql, ArrayUtils.EMPTY_OBJECT_ARRAY, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public String queryForString(final String sql, final Object args) throws SQLException {
        return this.queryForObject(sql, args, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public String queryForString(final String sql, final PreparedStatementSetter args) throws SQLException {
        return this.queryForObject(sql, args, this.createSingleColumnRowMapper(String.class));
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        return this.executeUpdate(sql, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public int executeUpdate(final String sql, final Object args) throws SQLException {
        if (args instanceof PreparedStatementSetter) {
            return this.executeUpdate(sql, (PreparedStatementSetter) args);
        } else {
            DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
            SqlArgSource argSource = toSqlArgSource(args);
            PreparedStatementCreator psc = getPreparedStatementCreator(parsedSql, argSource);
            return this.executeCreator(psc, (PreparedStatementCallback<Integer>) PreparedStatement::executeUpdate);
        }
    }

    @Override
    public int executeUpdate(final String sql, final PreparedStatementSetter args) throws SQLException {
        PreparedStatementCreator psc = this.getPreparedStatementCreator(sql, args);
        return this.executeCreator(psc, (PreparedStatementCallback<Integer>) PreparedStatement::executeUpdate);
    }

    /** Create a new RowMapper for reading columns as key-value pairs. */
    protected RowMapper<Map<String, Object>> createMapRowMapper() {
        return new ColumnMapRowMapper(this.isResultsCaseInsensitive(), this.getRegistry().getTypeRegistry()) {
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

        return new MappingRowMapper<>(requiredType, this.getRegistry().getTypeRegistry());
    }

    /** Create a new RowMapper for reading result objects from a single column. */
    protected <T> RowMapper<T> createSingleColumnRowMapper(final Class<T> requiredType) {
        Objects.requireNonNull(requiredType, "requiredType is null.");
        return new SingleColumnRowMapper<>(requiredType, this.getRegistry().getTypeRegistry());
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

        return new MappingResultSetExtractor<>(requiredType, this.getRegistry().getTypeRegistry());
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
    protected PreparedStatementCreator getPreparedStatementCreator(final DefaultSqlSegment segment, final SqlArgSource paramSource) {
        Objects.requireNonNull(segment, "SQL must not be null.");
        Objects.requireNonNull(paramSource, "SqlArgSource must not be null.");
        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL query [" + segment.getOriSqlString() + "].");
        }
        return new MapPreparedStatementCreator(segment, paramSource);
    }

    /** Build a PreparedStatementCreator based on the given SQL and named parameters. */
    protected CallableStatementCreator getCallableStatementCreator(final String sql, final CallableStatementSetter setter) {
        Objects.requireNonNull(sql, "SQL must not be null.");
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
    public int[] executeBatch(String sql, Object[] batchArgs) throws SQLException {
        if (batchArgs == null || batchArgs.length == 0) {
            return new int[0];
        }

        // check
        boolean usingPreparedStatement = false;
        boolean usingArgSource = false;
        for (int i = 0; i < batchArgs.length; i++) {
            if (batchArgs[i] instanceof PreparedStatementSetter) {
                usingPreparedStatement = true;
            } else {
                usingArgSource = true;
            }
        }
        if (usingPreparedStatement && usingArgSource) {
            throw new SQLException("executeBatch does not support mixing PreparedStatementSetter with other methods.");
        }

        // doBatch
        if (usingPreparedStatement) {
            return this.executeBatch(sql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ((PreparedStatementSetter) batchArgs[i]).setValues(ps);
                }

                @Override
                public int getBatchSize() {
                    return batchArgs.length;
                }
            });
        } else {

            String prepareSql = "";
            Object[][] prepareArgs = new Object[batchArgs.length][];
            for (int i = 0; i < batchArgs.length; i++) {
                DefaultSqlSegment parsedSql = DynamicParsed.getParsedSql(sql);
                SqlBuilder sqlBuilder = parsedSql.buildQuery(toSqlArgSource(batchArgs[i]), this.getRegistry());

                if (i == 0) {
                    prepareSql = sqlBuilder.getSqlString();
                    prepareArgs[i] = sqlBuilder.getArgs();
                } else {
                    if (!StringUtils.equals(prepareSql, sqlBuilder.getSqlString())) {
                        throw new SQLException("executeBatch, each set of parameters must be able to derive the same SQL.");
                    } else {
                        prepareArgs[i] = sqlBuilder.getArgs();
                    }
                }
            }

            TypeHandlerRegistry typeRegistry = getRegistry().getTypeRegistry();
            return this.executeBatch(prepareSql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Object[] args = prepareArgs[i];
                    for (int j = 0; j < args.length; j++) {
                        typeRegistry.setParameterValue(ps, j + 1, args[j]);
                    }
                }

                @Override
                public int getBatchSize() {
                    return prepareArgs.length;
                }
            });

        }
    }

    @Override
    public int[] executeBatch(final String sql, final BatchPreparedStatementSetter pss) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing SQL batch update [" + sql + "].");
        }

        return this.executeCreator(getPreparedStatementCreator(sql), (PreparedStatementCallback<int[]>) ps -> {
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
                if (pss instanceof SqlArgDisposer) {
                    ((SqlArgDisposer) pss).cleanupParameters();
                }
            }
        });
    }

    /** 创建用于保存结果集的数据Map。 */
    protected Map<String, Object> createResultsMap() {
        if (this.isResultsCaseInsensitive()) {
            return new LinkedCaseInsensitiveMap<>();
        } else {
            return new LinkedHashMap<>();
        }
    }

    /** 获取SQL文本 */
    private static String getSql(final Object sqlProvider) {
        if (sqlProvider instanceof SqlProvider) {
            return ((SqlProvider) sqlProvider).getSql();
        } else {
            return null;
        }
    }

    /** 至返回结果集中的一条数据。 */
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

    /** 接口 {@link PreparedStatementCreator} 的简单实现，目的是根据 SQL 语句创建 {@link PreparedStatement}对象。 */
    private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlArgDisposer, SqlProvider {
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
            if (this.setter instanceof SqlArgDisposer) {
                ((SqlArgDisposer) this.setter).cleanupParameters();
            }
        }
    }

    /** Simple adapter for CallableStatementCreator, allowing to use a plain SQL statement. */
    private static class SimpleCallableStatementCreator implements CallableStatementCreator, SqlArgDisposer, SqlProvider {
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
            if (this.setter instanceof SqlArgDisposer) {
                ((SqlArgDisposer) this.setter).cleanupParameters();
            }
        }
    }

    /** 接口 {@link PreparedStatementCreator} 的简单实现，目的是根据 SQL 语句创建 {@link PreparedStatement}对象。 */
    private class MapPreparedStatementCreator implements PreparedStatementCreator, SqlArgDisposer, SqlProvider {
        private final DefaultSqlSegment segment;
        private final SqlArgSource      paramSource;

        public MapPreparedStatementCreator(final DefaultSqlSegment segment, final SqlArgSource paramSource) {
            Objects.requireNonNull(segment, "SQL must not be null");
            this.segment = segment;
            this.paramSource = paramSource;
        }

        @Override
        public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
            SqlBuilder buildSql = this.segment.buildQuery(this.paramSource, getRegistry());

            String sqlToUse = buildSql.getSqlString();
            Object[] paramArray = buildSql.getArgs();

            PreparedStatement ps = con.prepareStatement(sqlToUse);
            if (paramArray.length > 0) {
                TypeHandlerRegistry typeRegistry = getRegistry().getTypeRegistry();
                for (int i = 0; i < paramArray.length; i++) {
                    typeRegistry.setParameterValue(ps, i + 1, paramArray[i]);
                }
                StatementSetterUtils.cleanupParameters(paramArray);
            }
            return ps;
        }

        @Override
        public String getSql() {
            return this.segment.getOriSqlString();
        }

        @Override
        public void cleanupParameters() {
            if (this.paramSource instanceof SqlArgDisposer) {
                ((SqlArgDisposer) this.paramSource).cleanupParameters();
            }
        }
    }

    /* Map of original SQL String to ParsedSql representation */
    private final Map<String, DefaultSqlSegment> parsedSqlCache = new HashMap<>();

    /* Obtain a parsed representation of the given SQL statement.*/
    protected DefaultSqlSegment getParsedSql(String originalSql) throws SQLException {
        try {
            synchronized (this.parsedSqlCache) {
                DefaultSqlSegment parsedSql = this.parsedSqlCache.get(originalSql);
                if (parsedSql == null) {
                    parsedSql = DynamicParsed.getParsedSql(originalSql);
                    this.parsedSqlCache.put(originalSql, parsedSql);
                }
                return parsedSql;
            }
        } catch (RuntimeSQLException e) {
            throw e.toSQLException();
        }
    }

    protected SqlArgSource toSqlArgSource(Object args) {
        if (args == null) {
            return new ArraySqlArgSource();
        } else if (args instanceof Map) {
            return new MapSqlArgSource((Map<String, ?>) args);
        } else if (args instanceof SqlArgSource) {
            return (SqlArgSource) args;
        } else if (args instanceof PreparedStatementSetter) {
            throw new UnsupportedOperationException();
        } else if (args instanceof SqlArg) {
            return new MapSqlArgSource(CollectionUtils.asMap("arg0", args));
        } else {
            Class<?> argType = args.getClass();
            if (argType.isArray()) {
                if (java.lang.reflect.Array.getLength(args) == 0) {
                    return new ArraySqlArgSource(ArrayUtils.EMPTY_OBJECT_ARRAY);
                } else {
                    return new ArraySqlArgSource(ArraySqlArgSource.toArgs(args));
                }
            } else if (this.getRegistry().getTypeRegistry().hasTypeHandler(argType)) {
                return new ArraySqlArgSource(new Object[] { args });
            } else {
                return new BeanSqlArgSource(args);
            }
        }
    }
}
