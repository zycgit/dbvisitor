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
package net.hasor.dbvisitor.wrapper.core;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.extractor.BeanMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.MapMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.MapMappingRowMapper;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.wrapper.segment.MergeSqlSegment;
import net.hasor.dbvisitor.wrapper.segment.Segment;
import net.hasor.dbvisitor.wrapper.segment.SqlKeyword;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 提供 lambda query 基础能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public abstract class AbstractSelectWrapper<R, T, P> extends BasicQueryCompare<R, T, P> implements QueryFunc<R, T, P> {
    protected final MergeSqlSegment customSelect = new MergeSqlSegment();
    protected final MergeSqlSegment groupByList  = new MergeSqlSegment();
    protected final MergeSqlSegment orderByList  = new MergeSqlSegment();
    private final   Page            pageInfo     = new PageObjectForWrapper(0, this::queryForLargeCount);
    private         boolean         lockGroupBy  = false;
    private         boolean         lockOrderBy  = false;

    public AbstractSelectWrapper(Class<?> exampleType, TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(exampleType, tableMapping, registry, jdbc);
    }

    @Override
    public R reset() {
        super.reset();
        this.customSelect.cleanSegment();
        this.groupByList.cleanSegment();
        this.orderByList.cleanSegment();
        this.initPage(-1, 0);
        this.lockGroupBy = false;
        this.lockOrderBy = false;
        return this.getSelf();
    }

    @Override
    public R selectAll() {
        this.customSelect.cleanSegment();
        return this.getSelf();
    }

    @Override
    public R selectAdd(P[] properties) {
        return this.selectApply(properties, false);
    }

    @Override
    public final R select(P[] properties) {
        return this.selectApply(properties, true);
    }

    protected R selectApply(P[] properties, boolean cleanSelect) {
        if (properties == null || properties.length == 0) {
            throw new IndexOutOfBoundsException("properties is empty.");
        }

        if (cleanSelect) {
            this.customSelect.cleanSegment();
        }

        for (P property : properties) {
            if (!this.customSelect.isEmpty()) {
                this.customSelect.addSegment(d -> ",");
            }
            this.customSelect.addSegment(buildSelectByProperty(getPropertyName(property)));
        }
        return this.getSelf();
    }

    @Override
    public R applySelect(String select) {
        this.customSelect.cleanSegment();
        this.customSelect.addSegment(d -> select);
        return this.getSelf();
    }

    @Override
    public R applySelectAdd(String select) {
        if (!this.customSelect.isEmpty()) {
            this.customSelect.addSegment(d -> ",");
        }
        this.customSelect.addSegment(d -> select);
        return this.getSelf();
    }

    protected void lockGroupBy() {
        this.lockGroupBy = true;
    }

    @Override
    public final R groupBy(P[] groupBy) {
        if (this.lockGroupBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();

        if (groupBy != null && groupBy.length > 0) {
            if (this.groupByList.isEmpty()) {
                this.queryTemplate.addSegment(SqlKeyword.GROUP_BY);
                this.queryTemplate.addSegment(this.groupByList);
            }

            for (P property : groupBy) {
                if (!this.groupByList.isEmpty()) {
                    this.groupByList.addSegment(d -> ",");
                }
                this.groupByList.addSegment(buildGroupByProperty(getPropertyName(property)));

            }
        }
        return this.getSelf();
    }

    protected void lockOrderBy() {
        this.lockGroupBy = true;
    }

    protected R addOrderBy(OrderType orderType, P[] orderBy, OrderNullsStrategy strategy) {
        if (this.lockOrderBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();
        lockGroupBy();

        if (orderBy != null && orderBy.length > 0) {
            if (this.orderByList.isEmpty()) {
                this.queryTemplate.addSegment(SqlKeyword.ORDER_BY);
                this.queryTemplate.addSegment(this.orderByList);
            }
            for (P property : orderBy) {
                if (!this.orderByList.isEmpty()) {
                    this.orderByList.addSegment(d -> ",");
                }

                this.orderByList.addSegment(buildOrderByProperty(getPropertyName(property), orderType, strategy));
            }
        }
        return this.getSelf();
    }

    @Override
    public R usePage(Page pageInfo) {
        Page page = this.pageInfo();
        page.setPageSize(pageInfo.getPageSize());
        page.setTotalCount(pageInfo.getTotalCount());
        page.setCurrentPage(pageInfo.getCurrentPage());
        page.setPageNumberOffset(pageInfo.getPageNumberOffset());
        return this.getSelf();
    }

    @Override
    public Page pageInfo() {
        return this.pageInfo;
    }

    @Override
    public R initPage(int pageSize, int pageNumber) {
        Page pageInfo = pageInfo();
        pageInfo.setPageNumberOffset(0);
        pageInfo.setPageSize(pageSize);
        pageInfo.setCurrentPage(pageNumber);
        return this.getSelf();
    }

    @Override
    public void query(RowCallbackHandler rch) throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        this.jdbc.query(boundSql.getSqlString(), boundSql.getArgs(), rch);
    }

    @Override
    public <V> V query(ResultSetExtractor<V> rse) throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        return this.jdbc.query(boundSql.getSqlString(), boundSql.getArgs(), rse);
    }

    @Override
    public List<T> queryForList() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");
        BoundSql boundSql = getBoundSql();

        if (Map.class == this.exampleType() || isFreedom()) {
            return (List<T>) this.queryForMapList();
        } else {
            ResultSetExtractor<List<T>> extractor = new BeanMappingResultSetExtractor<>(this.getTableMapping());
            return this.jdbc.query(boundSql.getSqlString(), boundSql.getArgs(), extractor);
        }
    }

    @Override
    public <V> List<V> queryForList(Class<V> asType) throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        return this.jdbc.queryForList(boundSql.getSqlString(), boundSql.getArgs(), asType);
    }

    @Override
    public <V> List<V> queryForList(RowMapper<V> rowMapper) throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        return this.jdbc.queryForList(boundSql.getSqlString(), boundSql.getArgs(), rowMapper);
    }

    @Override
    public List<Map<String, Object>> queryForMapList() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        ResultSetExtractor<List<Map<String, Object>>> extractor = new MapMappingResultSetExtractor(this.getTableMapping());
        return this.jdbc.query(boundSql.getSqlString(), boundSql.getArgs(), extractor);
    }

    @Override
    public Map<String, Object> queryForMap() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        RowMapper<Map<String, Object>> rowMapper = new MapMappingRowMapper(getTableMapping());
        return this.jdbc.queryForObject(boundSql.getSqlString(), boundSql.getArgs(), rowMapper);
    }

    @Override
    public T queryForObject() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        RowMapper<T> rowMapper = new BeanMappingRowMapper<>(getTableMapping());
        return this.jdbc.queryForObject(boundSql.getSqlString(), boundSql.getArgs(), rowMapper);
    }

    @Override
    public <V> V queryForObject(Class<V> asType) throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql boundSql = getBoundSql();
        return this.jdbc.queryForObject(boundSql.getSqlString(), boundSql.getArgs(), asType);
    }

    @Override
    public <V> V queryForObject(RowMapper<V> rowMapper) throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");
        Objects.requireNonNull(rowMapper, "rowMapper is required.");

        BoundSql boundSql = getBoundSql();
        return this.jdbc.queryForObject(boundSql.getSqlString(), boundSql.getArgs(), rowMapper);
    }

    @Override
    public int queryForCount() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql oriBoundSql = this.buildBoundSqlWithoutPage(dialect());
        BoundSql countSql = ((PageSqlDialect) this.dialect()).countSql(oriBoundSql);
        return this.jdbc.queryForInt(countSql.getSqlString(), countSql.getArgs());
    }

    @Override
    public long queryForLargeCount() throws SQLException {
        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");

        BoundSql oriBoundSql = this.buildBoundSqlWithoutPage(dialect());
        BoundSql countSql = ((PageSqlDialect) this.dialect()).countSql(oriBoundSql);
        return this.jdbc.queryForLong(countSql.getSqlString(), countSql.getArgs());
    }

    @Override
    protected BoundSql buildBoundSql(final SqlDialect dialect) {
        long pageSize = pageInfo().getPageSize();
        if (pageSize > 0) {
            BoundSql sqlWithoutPage = buildBoundSqlWithoutPage(dialect);
            long position = pageInfo().getFirstRecordPosition();
            return ((PageSqlDialect) dialect).pageSql(sqlWithoutPage, position, pageSize);
        } else {
            return buildBoundSqlWithoutPage(dialect);
        }
    }

    private BoundSql buildBoundSqlWithoutPage(SqlDialect dialect) {
        MergeSqlSegment sqlSegment = new MergeSqlSegment();
        sqlSegment.addSegment(SqlKeyword.SELECT);
        if (this.customSelect.isEmpty()) {
            if (this.groupByList.isEmpty()) {
                sqlSegment.addSegment(d -> "*");
            } else {
                sqlSegment.addSegment(this.groupByList);
            }
        } else {
            sqlSegment.addSegment(this.customSelect);
        }
        sqlSegment.addSegment(SqlKeyword.FROM);
        sqlSegment.addSegment(d -> {
            TableMapping<?> tableMapping = this.getTableMapping();
            String catalogName = tableMapping.getCatalog();
            String schemaName = tableMapping.getSchema();
            String tableName = tableMapping.getTable();
            return d.tableName(isQualifier(), catalogName, schemaName, tableName);
        });

        if (!this.queryTemplate.isEmpty()) {
            Segment firstSqlSegment = this.queryTemplate.firstSqlSegment();
            if (firstSqlSegment == SqlKeyword.GROUP_BY || firstSqlSegment == SqlKeyword.HAVING || firstSqlSegment == SqlKeyword.ORDER_BY) {
                sqlSegment.addSegment(this.queryTemplate);
            } else {
                sqlSegment.addSegment(SqlKeyword.WHERE);
                if (this.queryTemplate.firstSqlSegment() == SqlKeyword.NOT) {
                    sqlSegment.addSegment(this.queryTemplate);
                } else {
                    sqlSegment.addSegment(this.queryTemplate.sub(1));
                }
            }
        }

        // if have any group by condition, then orderBy must be in groupBy
        String sqlQuery = sqlSegment.getSqlSegment(dialect);
        Object[] args = this.queryParam.toArray().clone();
        return new BoundSql.BoundSqlObj(sqlQuery, args);
    }

    //    @Override
    //    public <D> Iterator<D> iteratorForLimit(long limit, int batchSize, Function<T, D> transform) {
    //        Page pageInfo = new PageObjectForLambda(batchSize, this::queryForLargeCount);
    //        pageInfo.setCurrentPage(0);
    //        pageInfo.setPageNumberOffset(0);
    //        return new StreamIterator<>(limit, pageInfo, this, transform, null);
    //    }
    //
    //    @Override
    //    public <D> Iterator<D> iteratorByBatch(int batchSize, Function<T, D> transform) {
    //        Page pageInfo = new PageObjectForLambda(batchSize, this::queryForLargeCount);
    //        pageInfo.setCurrentPage(0);
    //        pageInfo.setPageNumberOffset(0);
    //        return new StreamIterator<>(-1, pageInfo, this, transform, null);
    //    }
    //
    //    private static class StreamIterator<R, T, P, D> implements Iterator<D> {
    //        private final Page                          pageInfo;
    //        private final AbstractSelectLambda<R, T, P> lambda;
    //        private final RowMapper<T>                  rowMapper;
    //        //
    //        private       Iterator<T>                   currentIterator;
    //        private final Function<T, D>                transform;
    //        private final AtomicLong                    limitCounter;
    //        private       boolean                       eof = false;
    //
    //        public StreamIterator(long limit, Page pageInfo, AbstractSelectLambda<R, T, P> lambda, Function<T, D> transform, RowMapper<T> rowMapper) {
    //            this.limitCounter = limit < 0 ? null : new AtomicLong(limit);
    //            this.pageInfo = pageInfo;
    //            this.lambda = lambda;
    //            this.transform = transform;
    //            this.rowMapper = rowMapper;
    //        }
    //
    //        private synchronized void fetchData() {
    //            try {
    //                this.lambda.usePage(this.pageInfo);
    //                List<T> queryResult;
    //                if (this.rowMapper == null) {
    //                    queryResult = this.lambda.queryForList();
    //                } else {
    //                    queryResult = this.lambda.query(this.rowMapper);
    //                }
    //                if (queryResult == null || queryResult.isEmpty()) {
    //                    this.eof = true;
    //                    this.currentIterator = Collections.emptyIterator();
    //                } else {
    //                    this.currentIterator = queryResult.iterator();
    //                }
    //            } catch (SQLException e) {
    //                throw ExceptionUtils.toRuntime(e);
    //            }
    //        }
    //
    //        @Override
    //        public synchronized boolean hasNext() {
    //            if (this.limitCounter != null && this.limitCounter.get() <= 0) {
    //                return false;
    //            }
    //
    //            if (this.currentIterator == null) {
    //                this.fetchData();
    //            }
    //
    //            if (this.currentIterator.hasNext()) {
    //                return true;
    //            } else if (!this.eof) {
    //                this.pageInfo.nextPage();
    //                this.fetchData();
    //                return this.currentIterator.hasNext();
    //            } else {
    //                return false;
    //            }
    //        }
    //
    //        @Override
    //        public synchronized D next() {
    //            if (this.hasNext()) {
    //                if (this.limitCounter != null) {
    //                    this.limitCounter.decrementAndGet();
    //                }
    //                return this.transform.apply(this.currentIterator.next());
    //            } else {
    //                throw new NoSuchElementException();
    //            }
    //        }
    //    }
}
