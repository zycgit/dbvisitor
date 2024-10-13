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
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

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
    /** 通过回调函数，执行一个JDBC数据访问操作 */
    <T> T execute(ConnectionCallback<T> action) throws SQLException;

    /** 通过回调函数，执行一个JDBC数据访问操作 */
    <T> T execute(StatementCallback<T> action) throws SQLException;

    /**执行一个 静态 SQL 语句，通常是一个 DDL 语句 */
    void execute(String sql) throws SQLException;

    // ------------------------------------------------------------------------ executeCallback(PreparedStatementCreator)

    /** 通过回调函数，执行一个JDBC数据访问操作，返回的结果集使用 {@link ResultSetExtractor} 处理 */
    <T> T executeCreator(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws SQLException;

    /** 通过回调函数，执行一个JDBC数据访问操作，返回的结果集使用 {@link RowCallbackHandler} 处理 */
    void executeCreator(PreparedStatementCreator psc, RowCallbackHandler rch) throws SQLException;

    /** 通过回调函数，执行一个JDBC数据访问操作，返回的结果集使用 {@link RowMapper} 处理 */
    <T> List<T> executeCreator(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws SQLException;

    // ------------------------------------------------------------------------ executeCallback(CallableStatementCreator)

    Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws SQLException;

    <T> T call(CallableStatementCreator csc, CallableStatementCallback<T> action) throws SQLException;

    <T> T call(String callString, CallableStatementSetter setter, CallableStatementCallback<T> action) throws SQLException;

    Map<String, Object> call(String callString, List<SqlParameter> declaredParameters) throws SQLException;

    // ------------------------------------------------------------------------ multipleExecute(ResultSetExtractor)

    /** 执行静态 SQL，期待语句可能返回多个结果。 */
    List<Object> multipleExecute(String sql) throws SQLException;

    /**
     * 执行动态 SQL，期待语句可能返回多个结果。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return 返回的多值结果
     */
    List<Object> multipleExecute(String sql, Object args) throws SQLException;

    /**
     * 执行带参的 SQL，期待语句可能返回多个结果。
     */
    List<Object> multipleExecute(String sql, PreparedStatementSetter args) throws SQLException;

    // ------------------------------------------------------------------------ query(ResultSetExtractor)

    /** 执行静态 SQL，并通过 {@link ResultSetExtractor} 转换结果集 */
    <T> T query(String sql, ResultSetExtractor<T> rse) throws SQLException;

    /**
     * 执行动态 SQL，并且将 SQL 查询结果集使用 {@link ResultSetExtractor} 接口处理。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return 返回的结果
     */
    <T> T query(String sql, Object args, ResultSetExtractor<T> rse) throws SQLException;

    /**
     * 执行带参的 SQL，并且将 SQL 查询结果集使用 {@link ResultSetExtractor} 接口处理。
     */
    <T> T query(String sql, PreparedStatementSetter args, ResultSetExtractor<T> rse) throws SQLException;

    // ------------------------------------------------------------------------ query(RowCallbackHandler)

    /** 执行静态 SQL，并通过 {@link RowCallbackHandler} 接口处理返回的行数据 */
    void query(String sql, RowCallbackHandler rch) throws SQLException;

    /**
     * 执行动态 SQL, 并通过 {@link RowCallbackHandler} 处理行数据。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     */
    void query(String sql, Object args, RowCallbackHandler rch) throws SQLException;

    /**
     * 执行带参的 SQL，并通过 {@link RowCallbackHandler} 处理行数据。
     */
    void query(String sql, PreparedStatementSetter args, RowCallbackHandler rch) throws SQLException;

    // ------------------------------------------------------------------------ queryForList(RowMapper)

    /** 执行静态 SQL，并通过 {@link RowMapper} 接口映射记录 */
    <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 执行动态 SQL, 并通过 {@link RowMapper} 接口映射记录。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     */
    <T> List<T> queryForList(String sql, Object args, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 执行带参的 SQL，并通过 {@link RowMapper} 接口映射记录。
     */
    <T> List<T> queryForList(String sql, PreparedStatementSetter args, RowMapper<T> rowMapper) throws SQLException;

    // ------------------------------------------------------------------------ queryForList(elementType)

    /** 执行静态 SQL，并通过 <code>elementType</code> 类型将数据记录映射到对象上。 */
    <T> List<T> queryForList(String sql, Class<T> elementType) throws SQLException;

    /**
     * 执行动态 SQL, 并通过 <code>elementType</code> 类型将数据记录映射到对象上。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     */
    <T> List<T> queryForList(String sql, Object args, Class<T> elementType) throws SQLException;

    /**
     * 执行带参的 SQL，并通过 <code>elementType</code> 类型将数据记录映射到对象上。
     */
    <T> List<T> queryForList(String sql, PreparedStatementSetter args, Class<T> elementType) throws SQLException;

    // ------------------------------------------------------------------------ queryForList(List/Map)

    /** 执行静态 SQL，结果以每行通过 Map 结构进行封装 */
    List<Map<String, Object>> queryForList(String sql) throws SQLException;

    /**
     * 执行动态 SQL，结果以每行通过 Map 结构进行封装。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     */
    List<Map<String, Object>> queryForList(String sql, Object args) throws SQLException;

    /**
     * 执行带参的 SQL，结果以每行通过 Map 结构进行封装。
     */
    List<Map<String, Object>> queryForList(String sql, PreparedStatementSetter args) throws SQLException;

    // ------------------------------------------------------------------------ queryForObject(RowMapper)

    /**
     * 执行静态 SQL，并使用 {@link RowMapper} 处理结果集。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 执行动态 SQL，并使用 {@link RowMapper} 处理结果集。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, Object args, RowMapper<T> rowMapper) throws SQLException;

    /**
     * 执行带参的 SQL，并使用 {@link RowMapper} 处理结果集。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, PreparedStatementSetter args, RowMapper<T> rowMapper) throws SQLException;

    // ------------------------------------------------------------------------ queryForObject(requiredType)

    /**
     * 执行静态 SQL，并将结果集数据转换成<code>requiredType</code>参数指定的类型对象。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, Class<T> requiredType) throws SQLException;

    /**
     * 执行动态 SQL，并将结果集数据转换成<code>requiredType</code>参数指定的类型对象。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, Object args, Class<T> requiredType) throws SQLException;

    /**
     * 执行带参的 SQL，并将结果集数据转换成<code>requiredType</code>参数指定的类型对象。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @return 当不存在记录时返回<code>null</code>。
     */
    <T> T queryForObject(String sql, PreparedStatementSetter args, Class<T> requiredType) throws SQLException;

    // ------------------------------------------------------------------------ queryForLong

    /**
     * 执行静态 SQL，并将结果集数据转换成<code>Map</code>。
     * 预计该方法只会处理一条数据，如果查询结果存在多条数据将取第一条记录作为结果。
     * @return 当不存在记录时返回<code>null</code>。
     */
    Map<String, Object> queryForMap(String sql) throws SQLException;

    /**
     * 执行动态 SQL，并将结果集数据转换成<code>Map</code>。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return 当不存在记录时返回<code>null</code>。
     */
    Map<String, Object> queryForMap(String sql, Object args) throws SQLException;

    /**
     * 执行带参的 SQL，并将结果集数据转换成<code>Map</code>。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据将会取得第一条数据作为结果。</p>
     * @return 当不存在记录时返回<code>null</code>。
     */
    Map<String, Object> queryForMap(String sql, PreparedStatementSetter args) throws SQLException;

    // ------------------------------------------------------------------------ queryForLong

    /**
     * 执行静态 SQL，并以 long 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @return the long value, or 0 in case of SQL NULL
     */
    long queryForLong(String sql) throws SQLException;

    /**
     * 执行动态 SQL，并以 long 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return the long value, or 0 in case of SQL NULL
     */
    long queryForLong(String sql, Object args) throws SQLException;

    /**
     * 执行带参的 SQL，并以 long 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @return the long value, or 0 in case of SQL NULL
     */
    long queryForLong(String sql, PreparedStatementSetter args) throws SQLException;
    // ------------------------------------------------------------------------ queryForInt

    /**
     * 执行静态 SQL，并以 int 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @return the int value, or 0 in case of SQL NULL
     */
    int queryForInt(String sql) throws SQLException;

    /**
     * 执行动态 SQL，并以 int 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return the int value, or 0 in case of SQL NULL
     */
    int queryForInt(String sql, Object args) throws SQLException;

    /**
     * 执行带参的 SQL，并以 int 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @return the int value, or 0 in case of SQL NULL
     */
    int queryForInt(String sql, PreparedStatementSetter args) throws SQLException;

    // ------------------------------------------------------------------------ queryForString

    /**
     * 执行静态 SQL，并以 string 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @return the string value.
     */
    String queryForString(String sql) throws SQLException;

    /**
     * 执行动态 SQL，并以 string 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return the string value.
     */
    String queryForString(String sql, Object args) throws SQLException;

    /**
     * 执行带参的 SQL，并以 string 类型返回数据。
     * <p>预计该方法只会处理一条数据，如果查询结果存在多条数据或者多列将会引发异常。</p>
     * @return the string value.
     */
    String queryForString(String sql, PreparedStatementSetter args) throws SQLException;

    // ------------------------------------------------------------------------ executeUpdate

    /** 执行静态 SQL，通常用于 insert、update、delete DML 操作，返回值用于表示受影响的行数。 */
    int executeUpdate(String sql) throws SQLException;

    /**
     * 执行动态 SQL，通常用于 insert、update、delete DML 操作，返回值用于表示受影响的行数。
     * @param sql 动态 SQL 语句块
     * @param args 语句参数，可支持的参数种类有：
     * <ul>
     *  <li>Array</li>
     *  <li>Map&lt;String, ?&gt;</li>
     *  <li>Pojo Bean</li>
     *  <li>{@link TypeHandlerRegistry} 中注册的类型</li>
     *  <li>{@link SqlArgSource}</li>
     *  <li>{@link PreparedStatementSetter}，使用这种类型设置参数时 SQL 语句中只能通过 ? 来定义传参</li>
     * </ul>
     * @return the string value.
     */
    int executeUpdate(String sql, Object args) throws SQLException;

    /** 执行带参的 SQL，通常用于 insert、update、delete DML 操作，返回值用于表示受影响的行数。*/
    int executeUpdate(String sql, PreparedStatementSetter args) throws SQLException;

    // ------------------------------------------------------------------------ executeBatch

    /** 批量执行 insert 或 update、delete 语句，返回值用于表示受影响的行数 */
    int[] executeBatch(String[] sql) throws SQLException;

    /** 批量执行 insert 或 update、delete 语句，这一批次中的SQL 参数使用 BatchPreparedStatementSetter 接口设置 */
    int[] executeBatch(String sql, Object[] batchArgs) throws SQLException;

    /** 批量执行 insert 或 update、delete 语句，这一批次中的SQL 参数使用 BatchPreparedStatementSetter 接口设置 */
    int[] executeBatch(String sql, BatchPreparedStatementSetter setter) throws SQLException;
}
