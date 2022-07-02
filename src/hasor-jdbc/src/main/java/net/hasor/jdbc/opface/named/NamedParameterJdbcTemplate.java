/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.jdbc.opface.named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import net.hasor.Hasor;
import net.hasor.jdbc.DataAccessException;
import net.hasor.jdbc.InvalidDataAccessException;
import net.hasor.jdbc.opface.JdbcOperations;
import net.hasor.jdbc.opface.PreparedStatementCallback;
import net.hasor.jdbc.opface.PreparedStatementCreator;
import net.hasor.jdbc.opface.ResultSetExtractor;
import net.hasor.jdbc.opface.RowCallbackHandler;
import net.hasor.jdbc.opface.RowMapper;
import net.hasor.jdbc.opface.SqlRowSet;
import net.hasor.jdbc.opface.core.ColumnMapRowMapper;
import net.hasor.jdbc.opface.core.JdbcTemplate;
import net.hasor.jdbc.opface.core.SingleColumnRowMapper;
import net.hasor.jdbc.opface.core.SqlRowSetResultSetExtractor;
import net.hasor.jdbc.opface.named.source.MapSqlParameterSource;
/**
 * Template class with a basic set of JDBC operations, allowing the use
 * of named parameters rather than traditional '?' placeholders.
 *
 * <p>This class delegates to a wrapped {@link #getJdbcOperations() JdbcTemplate}
 * once the substitution from named parameters to JDBC style '?' placeholders is
 * done at execution time. It also allows for expanding a {@link java.util.List}
 * of values to the appropriate number of placeholders.
 *
 * <p>The underlying {@link org.noe.platform.modules.db.jdbcorm.jdbc.core.JdbcTemplate} is
 * exposed to allow for convenient access to the traditional
 * {@link org.noe.platform.modules.db.jdbcorm.jdbc.core.JdbcTemplate} methods.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcOperations
 * @see org.noe.platform.modules.db.jdbcorm.jdbc.core.JdbcTemplate
 */
public class NamedParameterJdbcTemplate implements NamedParameterJdbcOperations {
    /** Default maximum number of entries for this template's SQL cache: 256 */
    public static final int        DEFAULT_CACHE_LIMIT = 256;
    /** The JdbcTemplate we are wrapping */
    private JdbcOperations         classicJdbcTemplate;
    private volatile int           cacheLimit          = DEFAULT_CACHE_LIMIT;
    /** Cache of original SQL String to ParsedSql representation */
    private Map<String, ParsedSql> parsedSqlCache;
    //
    //
    private NamedParameterJdbcTemplate() {
        this.parsedSqlCache = new LinkedHashMap<String, ParsedSql>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, ParsedSql> eldest) {
                return size() > getCacheLimit();
            }
        };
    }
    /**
     * Create a new NamedParameterJdbcTemplate for the given {@link DataSource}.
     * <p>Creates a classic Spring {@link org.noe.platform.modules.db.jdbcorm.jdbc.core.JdbcTemplate} and wraps it.
     * @param dataSource the JDBC DataSource to access
     */
    public NamedParameterJdbcTemplate(DataSource dataSource) {
        this();
        Hasor.assertIsNotNull(dataSource, "DataSource must not be null");
        this.classicJdbcTemplate = new JdbcTemplate(dataSource);
    }
    /**
     * Create a new NamedParameterJdbcTemplate for the given classic
     * Spring {@link org.noe.platform.modules.db.jdbcorm.jdbc.core.JdbcTemplate}.
     * @param classicJdbcTemplate the classic Spring JdbcTemplate to wrap
     */
    public NamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
        this();
        Hasor.assertIsNotNull(classicJdbcTemplate, "JdbcTemplate must not be null");
        this.classicJdbcTemplate = classicJdbcTemplate;
    }
    /**Expose the classic Spring JdbcTemplate to allow invocation of less commonly used methods.*/
    public JdbcOperations getJdbcOperations() {
        return this.classicJdbcTemplate;
    }
    /**Specify the maximum number of entries for this template's SQL cache. Default is 256.*/
    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }
    /**Return the maximum number of entries for this template's SQL cache.*/
    public int getCacheLimit() {
        return this.cacheLimit;
    }
    public <T> T execute(String sql, SqlParameterSource paramSource, PreparedStatementCallback<T> action) throws DataAccessException {
        return getJdbcOperations().execute(getPreparedStatementCreator(sql, paramSource), action);
    }
    public <T> T execute(String sql, Map<String, ?> paramMap, PreparedStatementCallback<T> action) throws DataAccessException {
        return execute(sql, new MapSqlParameterSource(paramMap), action);
    }
    public <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse) throws DataAccessException {
        return getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rse);
    }
    public <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(sql, new MapSqlParameterSource(paramMap), rse);
    }
    public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch) throws DataAccessException {
        getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rch);
    }
    public void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch) throws DataAccessException {
        query(sql, new MapSqlParameterSource(paramMap), rch);
    }
    public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws DataAccessException {
        return getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rowMapper);
    }
    public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new MapSqlParameterSource(paramMap), rowMapper);
    }
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws DataAccessException {
        List<T> results = getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), rowMapper);
        return requiredSingleResult(results);
    }
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
        return queryForObject(sql, new MapSqlParameterSource(paramMap), rowMapper);
    }
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, paramSource, new SingleColumnRowMapper<T>(requiredType));
    }
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, paramMap, new SingleColumnRowMapper<T>(requiredType));
    }
    public Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return queryForObject(sql, paramSource, new ColumnMapRowMapper());
    }
    public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForObject(sql, paramMap, new ColumnMapRowMapper());
    }
    public long queryForLong(String sql, SqlParameterSource paramSource) throws DataAccessException {
        Number number = queryForObject(sql, paramSource, Number.class);
        return (number != null ? number.longValue() : 0);
    }
    public long queryForLong(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForLong(sql, new MapSqlParameterSource(paramMap));
    }
    public int queryForInt(String sql, SqlParameterSource paramSource) throws DataAccessException {
        Number number = queryForObject(sql, paramSource, Number.class);
        return (number != null ? number.intValue() : 0);
    }
    public int queryForInt(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForInt(sql, new MapSqlParameterSource(paramMap));
    }
    public <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType) throws DataAccessException {
        return query(sql, paramSource, new SingleColumnRowMapper<T>(elementType));
    }
    public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType) throws DataAccessException {
        return queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
    }
    public List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return query(sql, paramSource, new ColumnMapRowMapper());
    }
    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForList(sql, new MapSqlParameterSource(paramMap));
    }
    public SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return getJdbcOperations().query(getPreparedStatementCreator(sql, paramSource), new SqlRowSetResultSetExtractor());
    }
    public SqlRowSet queryForRowSet(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForRowSet(sql, new MapSqlParameterSource(paramMap));
    }
    public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return getJdbcOperations().update(getPreparedStatementCreator(sql, paramSource));
    }
    public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return update(sql, new MapSqlParameterSource(paramMap));
    }
    public int[] batchUpdate(String sql, Map<String, ?>[] batchValues) {
        SqlParameterSource[] batchArgs = new SqlParameterSource[batchValues.length];
        int i = 0;
        for (Map<String, ?> values : batchValues) {
            batchArgs[i] = new MapSqlParameterSource(values);
            i++;
        }
        return batchUpdate(sql, batchArgs);
    }
    public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {
        ParsedSql parsedSql = this.getParsedSql(sql);
        return NamedParameterBatchUpdateUtils.executeBatchUpdateWithNamedParameters(parsedSql, batchArgs, getJdbcOperations());
    }
    /**
     * Build a PreparedStatementCreator based on the given SQL and named parameters.
     * <p>Note: Not used for the <code>update</code> variant with generated key handling.
     * @param sql SQL to execute
     * @param paramSource container of arguments to bind
     * @return the corresponding PreparedStatementCreator
     */
    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        ParsedSql parsedSql = getParsedSql(sql);
        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
        int[] paramTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, paramTypes);
        return pscf.newPreparedStatementCreator(params);
    }
    /**
     * Obtain a parsed representation of the given SQL statement.
     * <p>The default implementation uses an LRU cache with an upper limit
     * of 256 entries.
     * @param sql the original SQL
     * @return a representation of the parsed SQL statement
     */
    protected ParsedSql getParsedSql(String sql) {
        if (getCacheLimit() <= 0) {
            return NamedParameterUtils.parseSqlStatement(sql);
        }
        synchronized (this.parsedSqlCache) {
            ParsedSql parsedSql = this.parsedSqlCache.get(sql);
            if (parsedSql == null) {
                parsedSql = NamedParameterUtils.parseSqlStatement(sql);
                this.parsedSqlCache.put(sql, parsedSql);
            }
            return parsedSql;
        }
    }
    /**�����ؽ�����е�һ�����ݡ�*/
    private static <T> T requiredSingleResult(Collection<T> results) throws InvalidDataAccessException {
        int size = (results != null ? results.size() : 0);
        if (size == 0)
            throw new InvalidDataAccessException("Empty Result");
        if (results.size() > 1)
            throw new InvalidDataAccessException("Incorrect column count: expected " + 1 + ", actual " + size);
        return results.iterator().next();
    }
}