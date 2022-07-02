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
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ������쳣��
     * @return �������ڼ�¼ʱ����<code>null</code>��
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������������ת����<code>requiredType</code>����ָ�������Ͷ���
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ������쳣��
     * @return �������ڼ�¼ʱ����<code>null</code>��
     */
    public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������������ת����<code>Map</code>��
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ������쳣��
     * @return �������ڼ�¼ʱ����<code>null</code>��
     */
    public Map<String, Object> queryForMap(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ȡ�� long �������ݡ�
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ��ߴ��ڶ������ݻ������쳣��
     * @return the long value, or 0 in case of SQL NULL
     */
    public long queryForLong(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ȡ�� int �������ݡ�
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ��ߴ��ڶ������ݻ������쳣��
     * @return the int value, or 0 in case of SQL NULL
     */
    public int queryForInt(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������ӳ�䵽һ���б�(һ����ĿΪÿһ��)�Ķ���
     * �б���ÿһ����¼����<code>elementType</code>����ָ�������Ͷ���*/
    public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������ӳ�䵽һ���б�(һ����ĿΪÿһ��)�Ķ���
     * �б���ÿһ����¼����<code>Map</code>���Ͷ���*/
    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ѯ���ʹ�� SqlRowSet �ӿڷ�װ��*/
    public SqlRowSet queryForRowSet(String sql) throws DataAccessException;
    /**ִ��һ�� insert �� update��delete ��䣬����ֵ���ڱ�ʾ��Ӱ���������*/
    public int update(String sql) throws DataAccessException;
    /**����ִ�� insert �� update��delete ��䣬����ֵ���ڱ�ʾ��Ӱ���������*/
    public int[] batchUpdate(String[] sql) throws DataAccessException;
    //-------------------------------------------------------------------------
    // Methods dealing with prepared statements
    //-------------------------------------------------------------------------
    /**ִ��һ�� JDBC ��������� JDBC ���ò�������ʹ�� PreparedStatement �ӿ�ִ�С�*/
    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementCallback ִ�С�*/
    public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException;
    /**ִ��һ����̬��ѯ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementCallback ִ�С�
     * ���صĽ����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException;
    /**ִ��һ����̬��ѯ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementSetter Ϊ��̬ SQL �������ԡ�
     * ���صĽ����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException;
    /**ִ��һ����̬��ѯ SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�����
     * SQL ��������ͨ�� argTypes �������������صĽ����ʹ�� ResultSetExtractor ת����
     * @param sql SQL query to execute
     * @param args arguments to bind to the query
     * @param argTypes SQL types of the arguments (constants from <code>java.sql.Types</code>)
     * @param rse object that will extract results
     * @see java.sql.Types
     */
    public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ� SQL ��ѯ�����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ� SQL ��ѯ�����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException;
    /**
     * Query using a prepared statement, reading the ResultSet on a per-row basis with a RowCallbackHandler.
     * <p>A PreparedStatementCreator can either be implemented directly or configured through a PreparedStatementCreatorFactory.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param rch object that will extract results, one row at a time
     */
    public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���
     * @see java.sql.Types*/
    public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, RowCallbackHandler rch, Object... args) throws DataAccessException;
    /**
     * Query using a prepared statement, mapping each row to a Java object via a RowMapper.
     * <p>A PreparedStatementCreator can either be implemented directly or
     * configured through a PreparedStatementCreatorFactory.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param rowMapper object that will map one object per row
     * @return the result List, containing mapped objects
     */
    public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� RowMapper ӳ��ת�������ء�
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� RowMapper ӳ��ת�������ء�
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� RowMapper ӳ��ת�������ء�
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� requiredType ��������ʾ�����ͷ�װ��
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� requiredType ��������ʾ�����ͷ�װ��
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� requiredType ��������ʾ�����ͷ�װ��
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ʹ�� Map ��װ��
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ʹ�� Map ��װ��
     * <p>��ȷ����ѯ���ֻ��һ����¼������������쳣��
     * @throws DataAccessException if the query fails
     */
    public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public long queryForLong(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public long queryForLong(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� int ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public int queryForInt(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     */
    public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     */
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� Map ���͡�
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ��¼����ʹ�� Map ���棬����װ�� List �С�*/
    public List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���ʹ�� SqlRowSet �ӿڷ�װ��
     * @see java.sql.Types*/
    public SqlRowSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���ʹ�� SqlRowSet �ӿڷ�װ��*/
    public SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(PreparedStatementCreator psc) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, PreparedStatementSetter pss) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����
     * @see java.sql.Types*/
    public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, Object... args) throws DataAccessException;
    /**����ִ�� SQL ��䣬��һ�����е�SQL ����ʹ�� BatchPreparedStatementSetter �ӿ����á�*/
    public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;
    //-------------------------------------------------------------------------
    // Methods dealing with callable statements
    //-------------------------------------------------------------------------
    /**ִ�� JDBC���洢���̡����������ݷ��ʲ�����*/
    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException;
    /**ִ�� JDBC���洢���̡����������ݷ��ʲ�����*/
    public <T> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException;
    /**ִ�� JDBC���洢���̡����������ݷ��ʲ�����*/
    public Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws DataAccessException;
}