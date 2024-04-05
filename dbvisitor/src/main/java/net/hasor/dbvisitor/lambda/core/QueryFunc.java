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
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.page.Page;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Query 复杂操作构造器。
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface QueryFunc<R, T, P> extends BoundSqlBuilder {
    /**
     * 查询所有属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R selectAll();

    /**
     * 查询部分属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P property1);

    /**
     * 查询部分属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P property1, P property2);

    /**
     * 查询部分属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P property1, P property2, P property3);

    /**
     * 查询部分属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P property1, P property2, P property3, P property4);

    /**
     * 查询部分属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P property1, P property2, P property3, P property4, P property5);

    /**
     * 查询部分属性
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(P[] properties);

    /**
     * 拼接 sql 方式来自定义 select 和 form 之间的语句，一旦使用自定义那么 selectAll 和 select 将会失效。
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     * <p>!! 会有 sql 注入风险 !!</p>
     * <p>例1: applySelect("count(*)")</p>
     * <p>例2: applySelect("date_format(dateColumn,'%Y-%m-%d') as date")</p>
     * <p>例3: applySelect("max(columnA) , min(columnB)")</p>
     */
    R applySelect(String select);

    /** 分组条件，类似：group by xxx */
    R groupBy(P property1);

    /** 分组条件，类似：group by xxx */
    R groupBy(P property1, P property2);

    /** 分组条件，类似：group by xxx */
    R groupBy(P property1, P property2, P property3);

    /** 分组条件，类似：group by xxx */
    R groupBy(P property1, P property2, P property3, P property4);

    /** 分组条件，类似：group by xxx */
    R groupBy(P property1, P property2, P property3, P property4, P property5);

    /** 分组条件，类似：group by xxx */
    R groupBy(P[] properties);

    /** 排序条件，类似：order by xxx */
    R orderBy(P property1);

    /** 排序条件，类似：order by xxx */
    R orderBy(P property1, P property2);

    /** 排序条件，类似：order by xxx */
    R orderBy(P property1, P property2, P property3);

    /** 排序条件，类似：order by xxx */
    R orderBy(P property1, P property2, P property3, P property4);

    /** 排序条件，类似：order by xxx */
    R orderBy(P property1, P property2, P property3, P property4, P property5);

    /** 排序条件，类似：order by xxx */
    R orderBy(P[] properties);

    /** 排序(升序)，类似：order by xxx desc */
    R asc(P property1);

    /** 排序(升序)，类似：order by xxx desc */
    R asc(P property1, P property2);

    /** 排序(升序)，类似：order by xxx desc */
    R asc(P property1, P property2, P property3);

    /** 排序(升序)，类似：order by xxx desc */
    R asc(P property1, P property2, P property3, P property4);

    /** 排序(升序)，类似：order by xxx desc */
    R asc(P property1, P property2, P property3, P property4, P property5);

    /** 排序(升序)，类似：order by xxx desc */
    R asc(P[] properties);

    /** 排序(降序)，类似：order by xxx desc */
    R desc(P property1);

    /** 排序(降序)，类似：order by xxx desc */
    R desc(P property1, P property2);

    /** 排序(降序)，类似：order by xxx desc */
    R desc(P property1, P property2, P property3);

    /** 排序(降序)，类似：order by xxx desc */
    R desc(P property1, P property2, P property3, P property4);

    /** 排序(降序)，类似：order by xxx desc */
    R desc(P property1, P property2, P property3, P property4, P property5);

    /** 排序(降序)，类似：order by xxx desc */
    R desc(P[] properties);

    /** 设置分页信息 */
    R usePage(Page pageInfo);

    /** 获取对应的分页对象 */
    Page pageInfo();

    /** 生成分页对象 */
    R initPage(int pageSize, int pageNumber);

    /** 执行查询，并通过 RowCallbackHandler 处理结果集。*/
    void query(RowCallbackHandler rch) throws SQLException;

    /** 执行查询，并通过 ResultSetExtractor 转换结果集。*/
    <V> V query(ResultSetExtractor<V> rse) throws SQLException;

    /** 执行查询，并使用 RowMapper 处理结果集。*/
    <V> List<V> query(RowMapper<V> rowMapper) throws SQLException;

    /** 执行查询，并结果将被映射到一个列表(一个条目为每一行)的对象，列表中每一条记录都是<code>elementType</code>参数指定的类型对象。*/
    List<T> queryForList() throws SQLException;

    List<Map<String, Object>> queryForMapList() throws SQLException;

    /** 执行查询，并返回一个结果。*/
    T queryForObject() throws SQLException;

    /** 执行查询，并返回一个Map结果。*/
    Map<String, Object> queryForMap() throws SQLException;

    /** 生成 select count() 查询语句并查询总数。*/
    int queryForCount() throws SQLException;

    /** 生成 select count() 查询语句并查询总数。*/
    long queryForLargeCount() throws SQLException;

    /** 迭代器方式获取 limit 条(-1 表示所有)，每批 200条。 */
    default Iterator<T> queryForIterator(long limit) throws SQLException {
        return this.queryForIterator(limit, 200, r -> r);
    }

    /** 迭代器方式获取 limit 条(-1 表示所有)，batchSize 表示每批条数。 */
    default Iterator<T> queryForIterator(long limit, int batchSize) throws SQLException {
        return this.queryForIterator(limit, batchSize, r -> r);
    }

    /** 分页方式 获取每一条数据,并通过 transform 对变换 */
    <D> Iterator<D> queryForIterator(long limit, int batchSize, Function<T, D> transform) throws SQLException;
}
