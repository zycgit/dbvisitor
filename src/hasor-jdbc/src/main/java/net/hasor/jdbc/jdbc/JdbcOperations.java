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
package net.hasor.jdbc.jdbc;
import java.util.List;
import java.util.Map;
import net.hasor.jdbc.dao.DataAccessException;
import net.hasor.jdbc.dao.IncorrectResultSizeDataAccessException;
import net.hasor.jdbc.jdbc.parameter.SqlParameter;
import net.hasor.jdbc.jdbc.rowset.SqlRowSet;
/**
 * �ýӿ�������һЩ JDBC ����������
 * @version : 2013-10-9
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author ������(zyc@hasor.net)
 */
public interface JdbcOperations {
    /**ͨ���ص�����ִ��һ��JDBC���ݷ��ʲ����� */
    public <T> T execute(ConnectionCallback<T> action) throws DataAccessException;
    /**ͨ���ص�����ִ��һ��JDBC���ݷ��ʲ����� */
    public <T> T execute(StatementCallback<T> action) throws DataAccessException;
    /**ִ��һ�� SQL��䣬ͨ����һ�� DDL ���. */
    public void execute(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣��ͨ�� ResultSetExtractor ת���������*/
    public <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣��ͨ�� RowCallbackHandler ����������*/
    public void query(String sql, RowCallbackHandler rch) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ʹ�� RowMapper ����������*/
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ʹ�� RowMapper ����������
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ����� {@link IncorrectResultSizeDataAccessException} �쳣��
     * @return �������ڼ�¼ʱ����<code>null</code>��
     * @throws IncorrectResultSizeDataAccessException 
     *    if the query does not return exactly one row.
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������������ת����<code>requiredType</code>����ָ�������Ͷ���
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ����� {@link IncorrectResultSizeDataAccessException} �쳣��
     * @return �������ڼ�¼ʱ����<code>null</code>��
     * @throws IncorrectResultSizeDataAccessException 
     *    if the query does not return exactly one row.
     */
    public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������������ת����<code>Map</code>��
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ����� {@link IncorrectResultSizeDataAccessException} �쳣��
     * @return �������ڼ�¼ʱ����<code>null</code>��
     * @throws IncorrectResultSizeDataAccessException 
     *    if the query does not return exactly one row.
     */
    public Map<String, Object> queryForMap(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ȡ�� long �������ݡ�
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ��ߴ��ڶ������ݻ����� {@link IncorrectResultSizeDataAccessException} �쳣��
     * @return the long value, or 0 in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException 
     *    if the query does not return exactly one row, or does not return exactly one column in that row.
     */
    public long queryForLong(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ȡ�� int �������ݡ�
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ��ߴ��ڶ������ݻ����� {@link IncorrectResultSizeDataAccessException} �쳣��
     * @return the int value, or 0 in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException 
     *    if the query does not return exactly one row, or does not return exactly one column in that row.
     */
    public int queryForInt(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������ӳ�䵽һ���б�(һ����ĿΪÿһ��)�Ķ���
     * �б���ÿһ����¼����<code>elementType</code>����ָ�������Ͷ���*/
    public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������ӳ�䵽һ���б�(һ����ĿΪÿһ��)�Ķ���
     * �б���ÿһ����¼����<code>Map</code>���Ͷ���*/
    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException;
    /**
     * Execute a query for a SqlRowSet, given static SQL.
     * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to
     * execute a static query with a PreparedStatement, use the overloaded
     * <code>queryForRowSet</code> method with <code>null</code> as argument array.
     * <p>The results will be mapped to an SqlRowSet which holds the data in a
     * disconnected fashion. This wrapper will translate any SQLExceptions thrown.
     * <p>Note that that, for the default implementation, JDBC RowSet support needs to
     * be available at runtime: by default, Sun's <code>com.sun.rowset.CachedRowSetImpl</code>
     * class is used, which is part of JDK 1.5+ and also available separately as part of
     * Sun's JDBC RowSet Implementations download (rowset.jar).
     * @param sql SQL query to execute
     * @return a SqlRowSet representation (possibly a wrapper around a
     * <code>javax.sql.rowset.CachedRowSet</code>)
     * @throws DataAccessException if there is any problem executing the query
     * @see #queryForRowSet(String, Object[])
     * @see SqlRowSetResultSetExtractor
     * @see javax.sql.rowset.CachedRowSet
     */
    public SqlRowSet queryForRowSet(String sql) throws DataAccessException;
    /**
     * Issue a single SQL update operation (such as an insert, update or delete statement).
     * @param sql static SQL to execute
     * @return the number of rows affected
     * @throws DataAccessException if there is any problem.
     */
    public int update(String sql) throws DataAccessException;
    /**
     * Issue multiple SQL updates on a single JDBC Statement using batching.
     * <p>Will fall back to separate updates on a single Statement if the JDBC
     * driver does not support batch updates.
     * @param sql defining an array of SQL statements that will be executed.
     * @return an array of the number of rows affected by each statement
     * @throws DataAccessException if there is any problem executing the batch
     */
    public int[] batchUpdate(String[] sql) throws DataAccessException;
    //-------------------------------------------------------------------------
    // Methods dealing with prepared statements
    //-------------------------------------------------------------------------
    
    
    
    
    
    
    
    
    
    /**
     * Execute a JDBC data access operation, implemented as callback action
     * working on a JDBC PreparedStatement. This allows for implementing arbitrary
     * data access operations on a single Statement, within Spring's managed
     * JDBC environment: that is, participating in Spring-managed transactions
     * and converting JDBC SQLExceptions into Spring's DataAccessException hierarchy.
     * <p>The callback action can return a result object, for example a
     * domain object or a collection of domain objects.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param action callback object that specifies the action
     * @return a result object returned by the action, or <code>null</code>
     * @throws DataAccessException if there is any problem
     */
    <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException;
    /**
     * Execute a JDBC data access operation, implemented as callback action
     * working on a JDBC PreparedStatement. This allows for implementing arbitrary
     * data access operations on a single Statement, within Spring's managed
     * JDBC environment: that is, participating in Spring-managed transactions
     * and converting JDBC SQLExceptions into Spring's DataAccessException hierarchy.
     * <p>The callback action can return a result object, for example a
     * domain object or a collection of domain objects.
     * @param sql SQL to execute
     * @param action callback object that specifies the action
     * @return a result object returned by the action, or <code>null</code>
     * @throws DataAccessException if there is any problem
     */
    <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException;
    /**
     * Query using a prepared statement, reading the ResultSet with a
     * ResultSetExtractor.
     * <p>A PreparedStatementCreator can either be implemented directly or
     * configured through a PreparedStatementCreatorFactory.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param rse object that will extract results
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws DataAccessException if there is any problem
     * @see PreparedStatementCreatorFactory
     */
    <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException;
    /**
     * Query using a prepared statement, reading the ResultSet with a
     * ResultSetExtractor.
     * @param sql SQL query to execute
     * @param pss object that knows how to set values on the prepared statement.
     * If this is <code>null</code>, the SQL will be assumed to contain no bind parameters.
     * Even if there are no bind parameters, this object may be used to
     * set fetch size and other performance options.
     * @param rse object that will extract results
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws DataAccessException if there is any problem
     */
    <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, reading the ResultSet with a
     * ResultSetExtractor.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @param rse object that will extract results
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws DataAccessException if the query fails
     * @see java.sql.Types
     */
    <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, reading the ResultSet with a
     * ResultSetExtractor.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @param rse object that will extract results
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws DataAccessException if the query fails
     */
    <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, reading the ResultSet with a
     * ResultSetExtractor.
     * @param sql SQL query to execute
     * @param rse object that will extract results
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws DataAccessException if the query fails
     */
    <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException;
    /**
     * Query using a prepared statement, reading the ResultSet on a per-row
     * basis with a RowCallbackHandler.
     * <p>A PreparedStatementCreator can either be implemented directly or
     * configured through a PreparedStatementCreatorFactory.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param rch object that will extract results, one row at a time
     * @throws DataAccessException if there is any problem
     * @see PreparedStatementCreatorFactory
     */
    void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * PreparedStatementSetter implementation that knows how to bind values
     * to the query, reading the ResultSet on a per-row basis with a
     * RowCallbackHandler.
     * @param sql SQL query to execute
     * @param pss object that knows how to set values on the prepared statement.
     * If this is <code>null</code>, the SQL will be assumed to contain no bind parameters.
     * Even if there are no bind parameters, this object may be used to
     * set fetch size and other performance options.
     * @param rch object that will extract results, one row at a time
     * @throws DataAccessException if the query fails
     */
    void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list of
     * arguments to bind to the query, reading the ResultSet on a per-row basis
     * with a RowCallbackHandler.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @param rch object that will extract results, one row at a time
     * @throws DataAccessException if the query fails
     * @see java.sql.Types
     */
    void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list of
     * arguments to bind to the query, reading the ResultSet on a per-row basis
     * with a RowCallbackHandler.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @param rch object that will extract results, one row at a time
     * @throws DataAccessException if the query fails
     */
    void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list of
     * arguments to bind to the query, reading the ResultSet on a per-row basis
     * with a RowCallbackHandler.
     * @param sql SQL query to execute
     * @param rch object that will extract results, one row at a time
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @throws DataAccessException if the query fails
     */
    void query(String sql, RowCallbackHandler rch, Object... args) throws DataAccessException;
    /**
     * Query using a prepared statement, mapping each row to a Java object
     * via a RowMapper.
     * <p>A PreparedStatementCreator can either be implemented directly or
     * configured through a PreparedStatementCreatorFactory.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param rowMapper object that will map one object per row
     * @return the result List, containing mapped objects
     * @throws DataAccessException if there is any problem
     * @see PreparedStatementCreatorFactory
     */
    <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * PreparedStatementSetter implementation that knows how to bind values
     * to the query, mapping each row to a Java object via a RowMapper.
     * @param sql SQL query to execute
     * @param pss object that knows how to set values on the prepared statement.
     * If this is <code>null</code>, the SQL will be assumed to contain no bind parameters.
     * Even if there are no bind parameters, this object may be used to
     * set fetch size and other performance options.
     * @param rowMapper object that will map one object per row
     * @return the result List, containing mapped objects
     * @throws DataAccessException if the query fails
     */
    <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping each row to a Java object
     * via a RowMapper.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @param rowMapper object that will map one object per row
     * @return the result List, containing mapped objects
     * @throws DataAccessException if the query fails
     * @see java.sql.Types
     */
    <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping each row to a Java object
     * via a RowMapper.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @param rowMapper object that will map one object per row
     * @return the result List, containing mapped objects
     * @throws DataAccessException if the query fails
     */
    <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping each row to a Java object
     * via a RowMapper.
     * @param sql SQL query to execute
     * @param rowMapper object that will map one object per row
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the result List, containing mapped objects
     * @throws DataAccessException if the query fails
     */
    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping a single result row to a
     * Java object via a RowMapper.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type)
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @param rowMapper object that will map one object per row
     * @return the single mapped object
     * @throws IncorrectResultSizeDataAccessException if the query does not
     * return exactly one row
     * @throws DataAccessException if the query fails
     */
    <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping a single result row to a
     * Java object via a RowMapper.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @param rowMapper object that will map one object per row
     * @return the single mapped object
     * @throws IncorrectResultSizeDataAccessException if the query does not
     * return exactly one row
     * @throws DataAccessException if the query fails
     */
    <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping a single result row to a
     * Java object via a RowMapper.
     * @param sql SQL query to execute
     * @param rowMapper object that will map one object per row
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the single mapped object
     * @throws IncorrectResultSizeDataAccessException if the query does not
     * return exactly one row
     * @throws DataAccessException if the query fails
     */
    <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result object.
     * <p>The query is expected to be a single row/single column query; the returned
     * result will be directly mapped to the corresponding object type.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @param requiredType the type that the result object is expected to match
     * @return the result object of the required type, or <code>null</code> in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForObject(String, Class)
     * @see java.sql.Types
     */
    <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result object.
     * <p>The query is expected to be a single row/single column query; the returned
     * result will be directly mapped to the corresponding object type.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @param requiredType the type that the result object is expected to match
     * @return the result object of the required type, or <code>null</code> in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForObject(String, Class)
     */
    <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result object.
     * <p>The query is expected to be a single row/single column query; the returned
     * result will be directly mapped to the corresponding object type.
     * @param sql SQL query to execute
     * @param requiredType the type that the result object is expected to match
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the result object of the required type, or <code>null</code> in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForObject(String, Class)
     */
    <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result Map.
     * <p>The query is expected to be a single row query; the result row will be
     * mapped to a Map (one entry for each column, using the column name as the key).
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @return the result Map (one entry for each column, using the
     * column name as the key)
     * @throws IncorrectResultSizeDataAccessException if the query does not
     * return exactly one row
     * @throws DataAccessException if the query fails
     * @see #queryForMap(String)
     * @see ColumnMapRowMapper
     * @see java.sql.Types
     */
    Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result Map.
     * The queryForMap() methods defined by this interface are appropriate
     * when you don't have a domain model. Otherwise, consider using
     * one of the queryForObject() methods.
     * <p>The query is expected to be a single row query; the result row will be
     * mapped to a Map (one entry for each column, using the column name as the key).
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the result Map (one entry for each column, using the
     * column name as the key)
     * @throws IncorrectResultSizeDataAccessException if the query does not
     * return exactly one row
     * @throws DataAccessException if the query fails
     * @see #queryForMap(String)
     * @see ColumnMapRowMapper
     */
    Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, resulting in a long value.
     * <p>The query is expected to be a single row/single column query that
     * results in a long value.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @return the long value, or 0 in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForLong(String)
     * @see java.sql.Types
     */
    long queryForLong(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, resulting in a long value.
     * <p>The query is expected to be a single row/single column query that
     * results in a long value.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the long value, or 0 in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForLong(String)
     */
    long queryForLong(String sql, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, resulting in an int value.
     * <p>The query is expected to be a single row/single column query that
     * results in an int value.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @return the int value, or 0 in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForInt(String)
     * @see java.sql.Types
     */
    int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, resulting in an int value.
     * <p>The query is expected to be a single row/single column query that
     * results in an int value.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the int value, or 0 in case of SQL NULL
     * @throws IncorrectResultSizeDataAccessException if the query does not return
     * exactly one row, or does not return exactly one column in that row
     * @throws DataAccessException if the query fails
     * @see #queryForInt(String)
     */
    int queryForInt(String sql, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * result objects, each of them matching the specified element type.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @param elementType the required type of element in the result list
     * (for example, <code>Integer.class</code>)
     * @return a List of objects that match the specified element type
     * @throws DataAccessException if the query fails
     * @see #queryForList(String, Class)
     * @see SingleColumnRowMapper
     */
    <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * result objects, each of them matching the specified element type.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @param elementType the required type of element in the result list
     * (for example, <code>Integer.class</code>)
     * @return a List of objects that match the specified element type
     * @throws DataAccessException if the query fails
     * @see #queryForList(String, Class)
     * @see SingleColumnRowMapper
     */
    <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * result objects, each of them matching the specified element type.
     * @param sql SQL query to execute
     * @param elementType the required type of element in the result list
     * (for example, <code>Integer.class</code>)
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return a List of objects that match the specified element type
     * @throws DataAccessException if the query fails
     * @see #queryForList(String, Class)
     * @see SingleColumnRowMapper
     */
    <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * Maps (one entry for each column, using the column name as the key).
     * Thus  Each element in the list will be of the form returned by this interface's
     * queryForMap() methods.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @return a List that contains a Map per row
     * @throws DataAccessException if the query fails
     * @see #queryForList(String)
     * @see java.sql.Types
     */
    List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * Maps (one entry for each column, using the column name as the key).
     * Each element in the list will be of the form returned by this interface's
     * queryForMap() methods.
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return a List that contains a Map per row
     * @throws DataAccessException if the query fails
     * @see #queryForList(String)
     */
    List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a SqlRowSet.
     * <p>The results will be mapped to an SqlRowSet which holds the data in a
     * disconnected fashion. This wrapper will translate any SQLExceptions thrown.
     * <p>Note that that, for the default implementation, JDBC RowSet support needs to
     * be available at runtime: by default, Sun's <code>com.sun.rowset.CachedRowSetImpl</code>
     * class is used, which is part of JDK 1.5+ and also available separately as part of
     * Sun's JDBC RowSet Implementations download (rowset.jar).
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @return a SqlRowSet representation (possibly a wrapper around a
     * <code>javax.sql.rowset.CachedRowSet</code>)
     * @throws DataAccessException if there is any problem executing the query
     * @see #queryForRowSet(String)
     * @see SqlRowSetResultSetExtractor
     * @see javax.sql.rowset.CachedRowSet
     * @see java.sql.Types
     */
    SqlRowSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a SqlRowSet.
     * <p>The results will be mapped to an SqlRowSet which holds the data in a
     * disconnected fashion. This wrapper will translate any SQLExceptions thrown.
     * <p>Note that that, for the default implementation, JDBC RowSet support needs to
     * be available at runtime: by default, Sun's <code>com.sun.rowset.CachedRowSetImpl</code>
     * class is used, which is part of JDK 1.5+ and also available separately as part of
     * Sun's JDBC RowSet Implementations download (rowset.jar).
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return a SqlRowSet representation (possibly a wrapper around a
     * <code>javax.sql.rowset.CachedRowSet</code>)
     * @throws DataAccessException if there is any problem executing the query
     * @see #queryForRowSet(String)
     * @see SqlRowSetResultSetExtractor
     * @see javax.sql.rowset.CachedRowSet
     */
    SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException;
    /**
     * Issue a single SQL update operation (such as an insert, update or delete statement)
     * using a PreparedStatementCreator to provide SQL and any required parameters.
     * <p>A PreparedStatementCreator can either be implemented directly or
     * configured through a PreparedStatementCreatorFactory.
     * @param psc object that provides SQL and any necessary parameters
     * @return the number of rows affected
     * @throws DataAccessException if there is any problem issuing the update
     * @see PreparedStatementCreatorFactory
     */
    int update(PreparedStatementCreator psc) throws DataAccessException;
    /**
     * Issue an update statement using a PreparedStatementSetter to set bind parameters,
     * with given SQL. Simpler than using a PreparedStatementCreator as this method
     * will create the PreparedStatement: The PreparedStatementSetter just needs to
     * set parameters.
     * @param sql SQL containing bind parameters
     * @param pss helper that sets bind parameters. If this is <code>null</code>
     * we run an update with static SQL.
     * @return the number of rows affected
     * @throws DataAccessException if there is any problem issuing the update
     */
    int update(String sql, PreparedStatementSetter pss) throws DataAccessException;
    /**
     * Issue a single SQL update operation (such as an insert, update or delete statement)
     * via a prepared statement, binding the given arguments.
     * @param sql SQL containing bind parameters
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments
     * (constants from <code>java.sql.Types</code>)
     * @return the number of rows affected
     * @throws DataAccessException if there is any problem issuing the update
     * @see java.sql.Types
     */
    int update(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * Issue a single SQL update operation (such as an insert, update or delete statement)
     * via a prepared statement, binding the given arguments.
     * @param sql SQL containing bind parameters
     * @param args arguments to bind to the query
     * (leaving it to the PreparedStatement to guess the corresponding SQL type);
     * may also contain {@link SqlParameterValue} objects which indicate not
     * only the argument value but also the SQL type and optionally the scale
     * @return the number of rows affected
     * @throws DataAccessException if there is any problem issuing the update
     */
    int update(String sql, Object... args) throws DataAccessException;
    /**
     * Issue multiple update statements on a single PreparedStatement,
     * using batch updates and a BatchPreparedStatementSetter to set values.
     * <p>Will fall back to separate updates on a single PreparedStatement
     * if the JDBC driver does not support batch updates.
     * @param sql defining PreparedStatement that will be reused.
     * All statements in the batch will use the same SQL.
     * @param pss object to set parameters on the PreparedStatement
     * created by this method
     * @return an array of the number of rows affected by each statement
     * @throws DataAccessException if there is any problem issuing the update
     */
    int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;
    //-------------------------------------------------------------------------
    // Methods dealing with callable statements
    //-------------------------------------------------------------------------
    /**
     * Execute a JDBC data access operation, implemented as callback action
     * working on a JDBC CallableStatement. This allows for implementing arbitrary
     * data access operations on a single Statement, within Spring's managed
     * JDBC environment: that is, participating in Spring-managed transactions
     * and converting JDBC SQLExceptions into Spring's DataAccessException hierarchy.
     * <p>The callback action can return a result object, for example a
     * domain object or a collection of domain objects.
     * @param csc object that can create a CallableStatement given a Connection
     * @param action callback object that specifies the action
     * @return a result object returned by the action, or <code>null</code>
     * @throws DataAccessException if there is any problem
     */
    <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException;
    /**
     * Execute a JDBC data access operation, implemented as callback action
     * working on a JDBC CallableStatement. This allows for implementing arbitrary
     * data access operations on a single Statement, within Spring's managed
     * JDBC environment: that is, participating in Spring-managed transactions
     * and converting JDBC SQLExceptions into Spring's DataAccessException hierarchy.
     * <p>The callback action can return a result object, for example a
     * domain object or a collection of domain objects.
     * @param callString the SQL call string to execute
     * @param action callback object that specifies the action
     * @return a result object returned by the action, or <code>null</code>
     * @throws DataAccessException if there is any problem
     */
    <T> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException;
    /**
     * Execute a SQL call using a CallableStatementCreator to provide SQL and any
     * required parameters.
     * @param csc object that provides SQL and any necessary parameters
     * @param declaredParameters list of declared SqlParameter objects
     * @return Map of extracted out parameters
     * @throws DataAccessException if there is any problem issuing the update
     */
    Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws DataAccessException;
}
