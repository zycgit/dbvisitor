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
package net.hasor.dbvisitor.lambda.core;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;

/**
 * Query 复杂操作构造器。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface QueryFunc<R, T, P> extends BasicFunc<R>, BoundSqlBuilder {
    /**
     * 查询所有属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R selectAll();

    /**
     * 查询一个字段
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    default R select(P property) {
        return this.select(property, (P[]) null);
    }

    /**
     * 查询部分字段
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    default R select(P[] properties) {
        return this.select(null, properties);
    }

    /**
     * 查询部分字段
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P first, P... other);

    /**
     * 拼接 sql 方式来自定义 select 和 form 之间的语句，一旦使用自定义那么 selectAll 和 select 将会失效。
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     * <p>!! 会有 sql 注入风险 !!</p>
     * <p>例1: applySelect("count(*)")</p>
     * <p>例2: applySelect("date_format(dateColumn,'%Y-%m-%d') as date")</p>
     * <p>例3: applySelect("max(columnA) , min(columnB)")</p>
     */
    R applySelect(String select);

    /**
     * 追加查询一个字段，不同于 {@link #select(Object)} 的是不会清空已有选择条件。
     */
    default R selectAdd(P property) {
        return this.selectAdd(property, (P[]) null);
    }

    /**
     * 追加查询部分字段，不同于 {@link #select(Object[])} 的是不会清空已有选择条件。
     */
    default R selectAdd(P[] properties) {
        return this.selectAdd(null, properties);
    }

    /**
     * 追加查询部分字段，不同于 {@link #select(Object[])} 的是不会清空已有选择条件。
     */
    R selectAdd(P first, P... other);

    /**
     * 追加拼接 sql 方式来自定义 select 和 form 之间的语句，一旦使用自定义那么 selectAll 和 select 将会失效。
     * 不同于 {@link #applySelect(String)} 的是不会清空已有选择条件。
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     * <p>!! 会有 sql 注入风险 !!</p>
     * <p>例1: applySelectAdd("count(*)")</p>
     * <p>例2: applySelectAdd("date_format(dateColumn,'%Y-%m-%d') as date")</p>
     * <p>例3: applySelectAdd("max(columnA) , min(columnB)")</p>
     */
    R applySelectAdd(String select);

    /** 分组条件，类似：group by xxx */
    default R groupBy(P property) {
        return this.groupBy(property, (P[]) null);
    }

    /** 分组条件，类似：group by xxx */
    default R groupBy(P[] properties) {
        return this.groupBy(null, properties);
    }

    /** 分组条件，类似：group by xxx */
    R groupBy(P first, P... other);

    /** 排序条件，类似：order by xxx */
    default R orderBy(P property) {
        return this.orderBy(OrderType.DEFAULT, null, property, (P[]) null);
    }

    /** 排序条件，类似：order by xxx */
    default R orderBy(P[] properties) {
        return this.orderBy(OrderType.DEFAULT, null, null, properties);
    }

    /** 分组条件，类似：group by xxx */
    default R orderBy(P first, P... other) {
        return this.orderBy(OrderType.DEFAULT, null, first, other);
    }

    /** 排序条件，类似：order by xxx */
    default R orderBy(OrderType orderType, OrderNullsStrategy strategy, P property) {
        return this.orderBy(orderType, strategy, property, (P[]) null);
    }

    /** 排序条件，类似：order by xxx */
    default R orderBy(OrderType orderType, OrderNullsStrategy strategy, P[] properties) {
        return this.orderBy(orderType, strategy, null, properties);
    }

    /** 排序条件，类似：order by xxx */
    R orderBy(OrderType orderType, OrderNullsStrategy strategy, P first, P... other);

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(P property) {
        return this.orderBy(OrderType.ASC, null, property, (P[]) null);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(P[] properties) {
        return this.orderBy(OrderType.ASC, null, null, properties);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(P first, P... other) {
        return this.orderBy(OrderType.ASC, null, first, other);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(OrderNullsStrategy strategy, P property) {
        return this.orderBy(OrderType.ASC, strategy, property, (P[]) null);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(OrderNullsStrategy strategy, P[] properties) {
        return this.orderBy(OrderType.ASC, strategy, null, properties);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(OrderNullsStrategy strategy, P first, P... other) {
        return this.orderBy(OrderType.ASC, strategy, first, other);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(P property) {
        return this.orderBy(OrderType.DESC, null, property, (P[]) null);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(P[] properties) {
        return this.orderBy(OrderType.DESC, null, null, properties);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(OrderNullsStrategy strategy, P property) {
        return this.orderBy(OrderType.DESC, strategy, property, (P[]) null);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(OrderNullsStrategy strategy, P[] properties) {
        return this.orderBy(OrderType.DESC, strategy, null, properties);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(OrderNullsStrategy strategy, P first, P... other) {
        return this.orderBy(OrderType.DESC, strategy, first, other);
    }

    /** 设置分页信息 */
    R usePage(Page pageInfo);

    /** 获取对应的分页对象 */
    Page pageInfo();

    /** 生成分页对象 */
    R initPage(int pageSize, int pageNumber);

    /** 执行查询，并通过 RowCallbackHandler 处理结果集。 */
    void query(RowCallbackHandler rch) throws SQLException;

    /** 执行查询，并通过 ResultSetExtractor 转换结果集。 */
    <V> V query(ResultSetExtractor<V> rse) throws SQLException;

    /** 执行查询，并结果将被映射到一个列表(一个条目为每一行)的对象，列表中每一条记录都是<code>elementType</code>参数指定的类型对象。 */
    List<T> queryForList() throws SQLException;

    /** 执行查询，并结果将结果映射到对象。 */
    <V> List<V> queryForList(Class<V> asType) throws SQLException;

    /** 执行查询，并使用 RowMapper 处理结果集。 */
    <V> List<V> queryForList(RowMapper<V> rowMapper) throws SQLException;

    List<Map<String, Object>> queryForMapList() throws SQLException;

    /** 执行查询，并返回一个结果。 */
    T queryForObject() throws SQLException;

    /** 执行查询，并返回一个结果。 */
    <V> V queryForObject(Class<V> asType) throws SQLException;

    /** 执行查询，并返回一个结果。 */
    <V> V queryForObject(RowMapper<V> rowMapper) throws SQLException;

    /** 执行查询，并返回一个Map结果。 */
    Map<String, Object> queryForMap() throws SQLException;

    /** 生成 select count() 查询语句并查询总数。 */
    int queryForCount() throws SQLException;

    /** 生成 select count() 查询语句并查询总数。 */
    long queryForLargeCount() throws SQLException;

    /** 迭代器方式获取 limit 条(-1 表示所有)，每批 200条。 */
    default Iterator<T> iteratorForLimit(long limit) {
        return this.iteratorForLimit(limit, 200, r -> r);
    }

    /** 迭代器方式获取 limit 条(-1 表示所有)，batchSize 表示每批条数。 */
    default Iterator<T> iteratorForLimit(long limit, int batchSize) {
        return this.iteratorForLimit(limit, batchSize, r -> r);
    }

    /** 分页方式 获取每一条数据,并通过 transform 对变换 */
    <D> Iterator<D> iteratorForLimit(long limit, int batchSize, Function<T, D> transform);

    /** 迭代器方式获取 limit 条(-1 表示所有)，每批 200条。 */
    default Iterator<T> iteratorByBatch(int batchSize) {
        return this.iteratorByBatch(batchSize, r -> r);
    }

    /** 分页方式 获取每一条数据,并通过 transform 对变换 */
    <D> Iterator<D> iteratorByBatch(int batchSize, Function<T, D> transform);
}
