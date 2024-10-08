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
package net.hasor.dbvisitor.jdbc;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 该接口声明了一些 JDBC 基本操作。
 * @version : 2013-10-9
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 */
public interface JdbcOperations {
    /**通过回调函数执行一个JDBC数据访问操作 */
    <T> T execute(ConnectionCallback<T> action) throws SQLException;

    /**执行一个动态 SQL 语句。SQL 语句会被编译成 StatementCallback 类型通过回调接口 StatementCallback 执行 */
    <T> T execute(StatementCallback<T> action) throws SQLException;

    /**执行一个 SQL语句，通常是一个 DDL 语句 */
    void execute(String sql) throws SQLException;

    // ------------------------------------------------------------------------ executeCallback(PreparedStatementCreator)

    /**执行一个动态查询 SQL 语句，返回的结果集使用 ResultSetExtractor 处理 */
    <T> T executeCreator(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws SQLException;

    /**执行一个动态查询 SQL 语句，返回的结果集使用 RowCallbackHandler 处理 */
    void executeCreator(PreparedStatementCreator psc, RowCallbackHandler rch) throws SQLException;

    /**执行一个动态查询 SQL 语句，返回的结果集使用 RowMapper 处理 */
    <T> List<T> executeCreator(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws SQLException;

    // ------------------------------------------------------------------------ executeCallback(CallableStatementCreator)

    Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws SQLException;

    <T> T call(CallableStatementCreator csc, CallableStatementCallback<T> action) throws SQLException;

    <T> T call(String callString, CallableStatementSetter setter, CallableStatementCallback<T> action) throws SQLException;

    Map<String, Object> call(String callString, List<SqlParameter> declaredParameters) throws SQLException;

    // ------------------------------------------------------------------------ multipleExecute(ResultSetExtractor)

    /** 执行一个 SQL语句块，语句块可能返回多个结果.（需要数据库驱动支持，例如mysql 要设置 allowMultiQueries=true 参数） */
    List<Object> multipleExecute(String sql) throws SQLException;

    /** 执行一个 SQL语句块，语句块可能返回多个结果.（需要数据库驱动支持，例如mysql 要设置 allowMultiQueries=true 参数） */
    List<Object> multipleExecute(String sql, Object[] args) throws SQLException;

    /** 执行一个 SQL语句块，语句块可能返回多个结果.（需要数据库驱动支持，例如mysql 要设置 allowMultiQueries=true 参数） */
    List<Object> multipleExecute(String sql, SqlParameterSource parameterSource) throws SQLException;

    /** 执行一个 SQL语句块，语句块可能返回多个结果.（需要数据库驱动支持，例如mysql 要设置 allowMultiQueries=true 参数） */
    List<Object> multipleExecute(String sql, Map<String, ?> paramMap) throws SQLException;

    /** 执行一个 SQL语句块，语句块可能返回多个结果.（需要数据库驱动支持，例如mysql 要设置 allowMultiQueries=true 参数） */
    List<Object> multipleExecute(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ query(ResultSetExtractor)

    /**执行一个静态 SQL 语句。并通过 ResultSetExtractor 转换结果集 */
    <T> T query(String sql, ResultSetExtractor<T> rse) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且将 SQL 查询结果集使用 ResultSetExtractor 转换 */
    <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且将 SQL 查询结果集使用 ResultSetExtractor 转换 */
    <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且将 SQL 查询结果集使用 ResultSetExtractor 转换 */
    <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse) throws SQLException;

    /**执行一个动态查询 SQL 语句。SQL 语句会被编译成 PreparedStatement 类型通过回调接口 PreparedStatementSetter 为动态 SQL 设置属性。返回的结果集使用 ResultSetExtractor 转换 */
    <T> T query(String sql, PreparedStatementSetter setter, ResultSetExtractor<T> rse) throws SQLException;

    // ------------------------------------------------------------------------ query(RowCallbackHandler)

    /**执行一个静态 SQL 语句。并通过 RowCallbackHandler 处理结果集 */
    void query(String sql, RowCallbackHandler rch) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且结果集行处理使用 RowCallbackHandler 接口处理 */
    void query(String sql, Object[] args, RowCallbackHandler rch) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且结果集行处理使用 RowCallbackHandler 接口处理 */
    void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且结果集行处理使用 RowCallbackHandler 接口处理 */
    void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作，并且结果集行处理使用 RowCallbackHandler 接口处理 */
    void query(String sql, PreparedStatementSetter setter, RowCallbackHandler rch) throws SQLException;

    // ------------------------------------------------------------------------ queryForList(RowMapper)

    /**执行一个静态 SQL 语句，并使用 RowMapper 处理结果集 */
    <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将返回一个 List，每一行将通过 RowMapper 映射 */
    <T> List<T> queryForList(String sql, Object[] args, RowMapper<T> rowMapper) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将返回一个 List，每一行将通过 RowMapper 映射 */
    <T> List<T> queryForList(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将返回一个 List，每一行将通过 RowMapper 映射 */
    <T> List<T> queryForList(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将返回一个 List，每一行将通过 RowMapper 映射 */
    <T> List<T> queryForList(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) throws SQLException;

    // ------------------------------------------------------------------------ queryForList(elementType)

    /**执行一个静态 SQL 语句，结果将被映射到一个列表(一个条目为每一行)的对象，列表中每一条记录都是<code>elementType</code>参数指定的类型对象 */
    <T> List<T> queryForList(String sql, Class<T> elementType) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 elementType 参数所表示的类型。
     * @throws SQLException if the query fails
     */
    <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 elementType 参数所表示的类型。
     * @throws SQLException if the query fails
     */
    <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 elementType 参数所表示的类型。
     * @throws SQLException if the query fails
     */
    <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 elementType 参数所表示的类型。
     * @throws SQLException if the query fails
     */
    <T> List<T> queryForList(String sql, PreparedStatementSetter setter, Class<T> elementType) throws SQLException;

    // ------------------------------------------------------------------------ queryForList(List/Map)

    /**执行一个静态 SQL 语句，结果将被映射到一个列表(一个条目为每一行)的对象，
     * 列表中每一条记录都是<code>Map</code>类型对象 */
    List<Map<String, Object>> queryForList(String sql) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询记录将会使用 Map 保存，并封装到 List 中 */
    List<Map<String, Object>> queryForList(String sql, Object[] args) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询记录将会使用 Map 保存，并封装到 List 中 */
    List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询记录将会使用 Map 保存，并封装到 List 中 */
    List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) throws SQLException;

    /**查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询记录将会使用 Map 保存，并封装到 List 中 */
    List<Map<String, Object>> queryForList(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ queryForObject(RowMapper)

    /**执行一个静态 SQL 语句，并使用 RowMapper 处理结果集。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据将取第一条记录作为结果。
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将通过 RowMapper 映射转换并返回。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 查询一个 SQL 语句，查询参数使用 SqlParameterSource 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 查询一个 SQL 语句，查询参数使用 Map 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 查询一个 SQL 语句，查询参数使用 PreparedStatementSetter 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) throws SQLException;

    // ------------------------------------------------------------------------ queryForObject(requiredType)

    /**执行一个静态 SQL 语句，并将结果集数据转换成<code>requiredType</code>参数指定的类型对象。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据将取第一条记录作为结果。
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, Class<T> requiredType) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将通过 requiredType 参数所表示的类型封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws SQLException;

    /**
     * 查询一个 SQL 语句，查询参数使用 SqlParameterSource 封装，并将查询结果使用 requiredType 参数表示的类型返回。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType) throws SQLException;

    /**
     * 查询一个 SQL 语句，查询参数使用 Map 封装，并将查询结果使用 requiredType 参数表示的类型返回。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws SQLException;

    /**
     * 查询一个 SQL 语句，查询参数使用 PreparedStatementSetter 封装，并将查询结果使用 requiredType 参数表示的类型返回。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    <T> T queryForObject(String sql, PreparedStatementSetter setter, Class<T> requiredType) throws SQLException;

    // ------------------------------------------------------------------------ queryForLong

    /**执行一个静态 SQL 语句，并将结果集数据转换成<code>Map</code>。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据将取第一条记录作为结果。
     * @return 当不存在记录时返回<code>null</code>。
     */
    Map<String, Object> queryForMap(String sql) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将使用 Map 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    Map<String, Object> queryForMap(String sql, Object[] args) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将使用 Map 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将使用 Map 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将使用 Map 封装。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。
     * @throws SQLException if the query fails
     */
    Map<String, Object> queryForMap(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ queryForLong

    /**执行一个静态 SQL 语句，并取得 long 类型数据。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。
     * @return the long value, or 0 in case of SQL NULL
     */
    long queryForLong(String sql) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 long 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    long queryForLong(String sql, Object[] args) throws SQLException;

    /**
     * 查询一个 SQL 语句，sql 参数通过 SqlParameterSource 封装。查询结果将转换成 long 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    long queryForLong(String sql, SqlParameterSource paramSource) throws SQLException;

    /**
     * 查询一个 SQL 语句，sql 参数通过 Map 封装。查询结果将转换成 long 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    long queryForLong(String sql, Map<String, ?> paramMap) throws SQLException;

    /**
     * 查询一个 SQL 语句，sql 参数通过 PreparedStatementSetter 封装。查询结果将转换成 long 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    long queryForLong(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ queryForInt

    /**执行一个静态 SQL 语句，并取得 int 类型数据。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。
     * @return the int value, or 0 in case of SQL NULL
     */
    int queryForInt(String sql) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 int 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    int queryForInt(String sql, Object[] args) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 int 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    int queryForInt(String sql, SqlParameterSource paramSource) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 int 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    int queryForInt(String sql, Map<String, ?> paramMap) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 int 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    int queryForInt(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ queryForString

    /**执行一个静态 SQL 语句，并取得 String 类型数据。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。
     * @return the String value.
     */
    String queryForString(String sql) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 String 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    String queryForString(String sql, Object[] args) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 String 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    String queryForString(String sql, SqlParameterSource paramSource) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 String 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    String queryForString(String sql, Map<String, ?> paramMap) throws SQLException;

    /**
     * 查询一个 SQL 语句，使用这个查询将会使用 PreparedStatement 接口操作。查询结果将转换成 String 类型。
     * 所以需要保证查询的结果只有一行一列，否则执行会引发异常。
     * @throws SQLException if the query fails
     */
    String queryForString(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ executeUpdate

    /**执行一条 insert 或 update、delete 语句，返回值用于表示受影响的行数 */
    int executeUpdate(String sql) throws SQLException;

    /**执行一个更新语句（insert、update、delete），这个查询将会使用 PreparedStatement 接口操作 */
    int executeUpdate(String sql, Object[] args) throws SQLException;

    /**执行一个更新语句（insert、update、delete），这个查询将会使用 PreparedStatement 接口操作 */
    int executeUpdate(String sql, SqlParameterSource paramSource) throws SQLException;

    /**执行一个更新语句（insert、update、delete），这个查询将会使用 PreparedStatement 接口操作 */
    int executeUpdate(String sql, Map<String, ?> paramMap) throws SQLException;

    /**执行一个更新语句（insert、update、delete），这个查询将会使用 PreparedStatement 接口操作 */
    int executeUpdate(String sql, PreparedStatementSetter setter) throws SQLException;

    // ------------------------------------------------------------------------ executeBatch

    /**批量执行 insert 或 update、delete 语句，返回值用于表示受影响的行数 */
    int[] executeBatch(String[] sql) throws SQLException;

    /**批量执行 insert 或 update、delete 语句，这一批次中的SQL 参数使用 BatchPreparedStatementSetter 接口设置 */
    int[] executeBatch(String sql, Object[][] batchValues) throws SQLException;

    /**批量执行 insert 或 update、delete 语句，这一批次中的SQL 参数使用 BatchPreparedStatementSetter 接口设置 */
    int[] executeBatch(String sql, SqlParameterSource[] batchArgs) throws SQLException;

    /**批量执行 insert 或 update、delete 语句，这一批次中的SQL 参数使用 BatchPreparedStatementSetter 接口设置 */
    int[] executeBatch(String sql, Map<String, ?>[] batchValues) throws SQLException;

    /**批量执行 insert 或 update、delete 语句，这一批次中的SQL 参数使用 BatchPreparedStatementSetter 接口设置 */
    int[] executeBatch(String sql, BatchPreparedStatementSetter setter) throws SQLException;
}
