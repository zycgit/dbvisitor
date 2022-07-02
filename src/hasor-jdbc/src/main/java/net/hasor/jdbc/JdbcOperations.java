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
package net.hasor.jdbc;
import java.util.List;
import java.util.Map;
import net.hasor.jdbc.exceptions.DataAccessException;
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
    /**ִ�� JDBC���洢���̡����������ݷ��ʲ�����
     * <p>CallableStatementCreator �ӿڻ��� CallableStatementCallback �ӿ� ������Ҫ�Դ洢���̵Ĵ�������������á�*/
    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException;
    /**ִ�� JDBC���洢���̡����������ݷ��ʲ�����SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� CallableStatementCallback ִ�С�*/
    public <T> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException;
    /**ִ��һ�� JDBC ��������� JDBC ���ò�������ʹ�� PreparedStatement �ӿ�ִ�С�*/
    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementCallback ִ�С�*/
    public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException;
    /**ִ��һ�� JDBC ��������� JDBC ���ò�������ʹ�� PreparedStatement �ӿ�ִ�С�*/
    public <T> T execute(String sql, SqlParameterSource paramSource, PreparedStatementCallback<T> action) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementCallback ִ�С�*/
    public <T> T execute(String sql, Map<String, ?> paramMap, PreparedStatementCallback<T> action) throws DataAccessException;
    //
    //
    //
    /**ִ��һ�� SQL��䣬ͨ����һ�� DDL ���. */
    public void execute(String sql) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬��ѯ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementCallback ִ�С�
     * ���صĽ����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣��ͨ�� ResultSetExtractor ת���������*/
    public <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException;
    /**ִ��һ����̬��ѯ SQL ��䡣SQL ���ᱻ����� PreparedStatement ����ͨ���ص��ӿ� PreparedStatementSetter Ϊ��̬ SQL �������ԡ����صĽ����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ� SQL ��ѯ�����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ� SQL ��ѯ�����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, Object[] arg, ResultSetExtractor<T> rses) throws DataAccessException;
    /**ִ��һ����̬��ѯ SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�����SQL ��������ͨ�� argTypes �������������صĽ����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ� SQL ��ѯ�����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ� SQL ��ѯ�����ʹ�� ResultSetExtractor ת����*/
    public <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse) throws DataAccessException;
    //
    //
    //
    /**
     * Query using a prepared statement, reading the ResultSet on a per-row basis with a RowCallbackHandler.
     * <p>A PreparedStatementCreator can either be implemented directly or configured through a PreparedStatementCreatorFactory.
     * @param psc object that can create a PreparedStatement given a Connection
     * @param rch object that will extract results, one row at a time
     */
    public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䡣��ͨ�� RowCallbackHandler ����������*/
    public void query(String sql, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, RowCallbackHandler rch, Object... args) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ��������ҽ�����д���ʹ�� RowCallbackHandler �ӿڴ���*/
    public void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch) throws DataAccessException;
    //
    //
    //
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
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬��ʹ�� RowMapper ����������*/
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���������һ�� List��ÿһ�н�ͨ�� RowMapper ӳ�䡣*/
    public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬�������ӳ�䵽һ���б�(һ����ĿΪÿһ��)�Ķ����б���ÿһ����¼����<code>elementType</code>����ָ�������Ͷ���*/
    public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     */
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     */
    public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     */
    public <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� elementType ��������ʾ�����͡�
     * @throws DataAccessException if the query fails
     */
    public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬��ʹ�� RowMapper ����������
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ�ȡ��һ����¼��Ϊ�����
     * @return �������ڼ�¼ʱ����<code>null</code>��
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� RowMapper ӳ��ת�������ء�
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� RowMapper ӳ��ת�������ء�
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� RowMapper ӳ��ת�������ء�
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬��ѯ����ʹ�� SqlParameterSource ��װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬��ѯ����ʹ�� Map ��װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException;
    /**ִ��һ����̬ SQL ��䣬�������������ת����<code>requiredType</code>����ָ�������Ͷ���
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ�ȡ��һ����¼��Ϊ�����
     * @return �������ڼ�¼ʱ����<code>null</code>��
     */
    public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� requiredType ��������ʾ�����ͷ�װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� requiredType ��������ʾ�����ͷ�װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ͨ�� requiredType ��������ʾ�����ͷ�װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬��ѯ����ʹ�� SqlParameterSource ��װ��������ѯ���ʹ�� requiredType ������ʾ�����ͷ��ء�
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬��ѯ����ʹ�� Map ��װ��������ѯ���ʹ�� requiredType ������ʾ�����ͷ��ء�
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬�������������ת����<code>Map</code>��
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ�ȡ��һ����¼��Ϊ�����
     * @return �������ڼ�¼ʱ����<code>null</code>��
     */
    public Map<String, Object> queryForMap(String sql) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ʹ�� Map ��װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ʹ�� Map ��װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ʹ�� Map ��װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ʹ�� Map ��װ��
     * <p>Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݽ���ȡ�õ�һ��������Ϊ�����
     * @throws DataAccessException if the query fails
     */
    public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬��ȡ�� long �������ݡ�
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ��߶��н��������쳣��
     * @return the long value, or 0 in case of SQL NULL
     */
    public long queryForLong(String sql) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public long queryForLong(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public long queryForLong(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬sql ����ͨ�� SqlParameterSource ��װ����ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public long queryForLong(String sql, SqlParameterSource paramSource) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬sql ����ͨ�� Map ��װ����ѯ�����ת���� long ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public long queryForLong(String sql, Map<String, ?> paramMap) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬��ȡ�� int �������ݡ�
     * Ԥ�Ƹ÷���ֻ�ᴦ��һ�����ݣ������ѯ������ڶ������ݻ��߶��н��������쳣��
     * @return the int value, or 0 in case of SQL NULL
     */
    public int queryForInt(String sql) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� int ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public int queryForInt(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� int ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� int ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public int queryForInt(String sql, SqlParameterSource paramSource) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� int ���͡�
     * ������Ҫ��֤��ѯ�Ľ��ֻ��һ��һ�У�����ִ�л������쳣��
     * @throws DataAccessException if the query fails
     */
    public int queryForInt(String sql, Map<String, ?> paramMap) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬�������ӳ�䵽һ���б�(һ����ĿΪÿһ��)�Ķ���
     * �б���ÿһ����¼����<code>Map</code>���Ͷ���*/
    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ��¼����ʹ�� Map ���棬����װ�� List �С�*/
    public List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException;
    /**
     * ��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ�����ת���� Map ���͡�
     * @throws DataAccessException if the query fails
     * @see java.sql.Types*/
    public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ��¼����ʹ�� Map ���棬����װ�� List �С�*/
    public List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ��¼����ʹ�� Map ���棬����װ�� List �С�*/
    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) throws DataAccessException;
    //
    //
    //
    /**ִ��һ����̬ SQL ��䣬��ѯ���ʹ�� SqlRowSet �ӿڷ�װ��*/
    public SqlRowSet queryForRowSet(String sql) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���ʹ�� SqlRowSet �ӿڷ�װ��
     * @see java.sql.Types*/
    public SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���ʹ�� SqlRowSet �ӿڷ�װ��
     * @see java.sql.Types*/
    public SqlRowSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���ʹ�� SqlRowSet �ӿڷ�װ��
     * @see java.sql.Types*/
    public SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException;
    /**��ѯһ�� SQL ��䣬ʹ�������ѯ����ʹ�� PreparedStatement �ӿڲ�������ѯ���ʹ�� SqlRowSet �ӿڷ�װ��
     * @see java.sql.Types*/
    public SqlRowSet queryForRowSet(String sql, Map<String, ?> paramMap) throws DataAccessException;
    //
    //
    //
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(PreparedStatementCreator psc) throws DataAccessException;
    /**ִ��һ�� insert �� update��delete ��䣬����ֵ���ڱ�ʾ��Ӱ���������*/
    public int update(String sql) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, PreparedStatementSetter pss) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, Object... args) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, SqlParameterSource paramSource) throws DataAccessException;
    /**ִ��һ��������䣨insert��update��delete���������ѯ����ʹ�� PreparedStatement �ӿڲ�����*/
    public int update(String sql, Map<String, ?> paramMap) throws DataAccessException;
    //
    //
    //
    /**����ִ�� insert �� update��delete ��䣬����ֵ���ڱ�ʾ��Ӱ���������*/
    public int[] batchUpdate(String[] sql) throws DataAccessException;
    /**����ִ�� SQL ��䣬��һ�����е�SQL ����ʹ�� BatchPreparedStatementSetter �ӿ����á�*/
    public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;
    /**����ִ�� SQL ��䣬��һ�����е�SQL ����ʹ�� BatchPreparedStatementSetter �ӿ����á�*/
    public int[] batchUpdate(String sql, Map<String, ?>[] batchValues) throws DataAccessException;
    /**����ִ�� SQL ��䣬��һ�����е�SQL ����ʹ�� BatchPreparedStatementSetter �ӿ����á�*/
    public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) throws DataAccessException;
    //-------------------------------------------------------------------------
    // Methods dealing with prepared statements
    //-------------------------------------------------------------------------
    //    /** �������� ��������Ϊmap */
    //    public int[] insertBatchMap(String tableName, List<Map<String, Object>> list);
    //    /** �������� ��������Ϊmap   Map<String, Object>[] batch */
    //    public int[] insertBatchMap(String tableName, Map<String, Object>[] batch);
    //    /** ͨ��map��ʽ�������� */
    //    public int insertMap(String tableName, Map<String, Object> map);
}