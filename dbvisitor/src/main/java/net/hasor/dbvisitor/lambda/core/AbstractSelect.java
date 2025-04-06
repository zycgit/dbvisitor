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
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.extractor.BeanMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.MapMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.MapMappingRowMapper;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.lambda.segment.SqlKeyword;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 提供 lambda query 基础能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
public abstract class AbstractSelect<R, T, P> extends BasicQueryCompare<R, T, P> implements QueryFunc<R, T, P> {
    protected final MergeSqlSegment customSelect = new MergeSqlSegment();
    protected final MergeSqlSegment groupByList  = new MergeSqlSegment();
    protected final MergeSqlSegment orderByList  = new MergeSqlSegment();
    private final   Page            pageInfo     = new PageObjectForFetchCount(0, this::queryForLargeCount);
    private         boolean         lockGroupBy  = false;
    private         boolean         lockOrderBy  = false;

    public AbstractSelect(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(exampleType, tableMapping, registry, jdbc, ctx);
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

    @SafeVarargs
    @Override
    public final R selectAdd(P first, P... other) {
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            List<P> list = new ArrayList<>();
            list.add(first);
            list.addAll(Arrays.asList(other));
            return this.selectApply(list, false);
        } else if (first == null) {
            return this.selectApply(Arrays.asList(other), false);
        } else {
            return this.selectApply(Collections.singletonList(first), false);
        }
    }

    @SafeVarargs
    @Override
    public final R select(P first, P... other) {
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            List<P> list = new ArrayList<>();
            list.add(first);
            list.addAll(Arrays.asList(other));
            return this.selectApply(list, true);
        } else if (first == null) {
            return this.selectApply(Arrays.asList(other), true);
        } else {
            return this.selectApply(Collections.singletonList(first), true);
        }
    }

    protected R selectApply(List<P> properties, boolean cleanSelect) {
        if (properties == null || properties.isEmpty()) {
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

    @SafeVarargs
    @Override
    public final R groupBy(P first, P... other) {
        if (this.lockGroupBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();

        //
        List<P> groupBy;
        if (first == null && other == null) {
            throw new IndexOutOfBoundsException("properties is empty.");
        } else if (first != null && other != null) {
            groupBy = new ArrayList<>();
            groupBy.add(first);
            groupBy.addAll(Arrays.asList(other));
        } else if (first == null) {
            groupBy = Arrays.asList(other);
        } else {
            groupBy = Collections.singletonList(first);
        }

        //
        if (!groupBy.isEmpty()) {
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

    protected R addOrderBy(OrderType orderType, List<P> orderBy, OrderNullsStrategy strategy) {
        if (this.lockOrderBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();
        lockGroupBy();

        if (orderBy != null && !orderBy.isEmpty()) {
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

        RowMapper<T> rowMapper;
        if (Map.class.isAssignableFrom(this.getTableMapping().entityType())) {
            boolean caseInsensitive = this.getTableMapping().isCaseInsensitive();
            rowMapper = (RowMapper<T>) new ColumnMapRowMapper(caseInsensitive, this.registry.getTypeRegistry());
        } else {
            rowMapper = new BeanMappingRowMapper<>(getTableMapping());
        }

        BoundSql boundSql = getBoundSql();
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
    protected BoundSql buildBoundSql(final SqlDialect dialect) throws SQLException {
        long pageSize = pageInfo().getPageSize();
        if (pageSize > 0) {
            BoundSql sqlWithoutPage = buildBoundSqlWithoutPage(dialect);
            long position = pageInfo().getFirstRecordPosition();
            return ((PageSqlDialect) dialect).pageSql(sqlWithoutPage, position, pageSize);
        } else {
            return buildBoundSqlWithoutPage(dialect);
        }
    }

    private BoundSql buildBoundSqlWithoutPage(SqlDialect dialect) throws SQLException {
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

    @Override
    public <D> Iterator<D> iteratorForLimit(long limit, int batchSize, Function<T, D> transform) {
        Page pageInfo = new PageObjectForFetchCount(batchSize, this::queryForLargeCount);
        pageInfo.setCurrentPage(0);
        pageInfo.setPageNumberOffset(0);
        return new StreamIterator<>(limit, pageInfo, this, transform, null);
    }

    @Override
    public <D> Iterator<D> iteratorByBatch(int batchSize, Function<T, D> transform) {
        Page pageInfo = new PageObjectForFetchCount(batchSize, this::queryForLargeCount);
        pageInfo.setCurrentPage(0);
        pageInfo.setPageNumberOffset(0);
        return new StreamIterator<>(-1, pageInfo, this, transform, null);
    }

    private static class StreamIterator<R, T, P, D> implements Iterator<D> {
        private final Page                    pageInfo;
        private final AbstractSelect<R, T, P> wrapper;
        private final RowMapper<T>            rowMapper;
        //
        private       Iterator<T>             currentIterator;
        private final Function<T, D>          transform;
        private final AtomicLong              limitCounter;
        private       boolean                 eof = false;

        public StreamIterator(long limit, Page pageInfo, AbstractSelect<R, T, P> wrapper, Function<T, D> transform, RowMapper<T> rowMapper) {
            this.limitCounter = limit < 0 ? null : new AtomicLong(limit);
            this.pageInfo = pageInfo;
            this.wrapper = wrapper;
            this.transform = transform;
            this.rowMapper = rowMapper;
        }

        private synchronized void fetchData() {
            try {
                this.wrapper.usePage(this.pageInfo);
                List<T> queryResult;
                if (this.rowMapper == null) {
                    queryResult = this.wrapper.queryForList();
                } else {
                    queryResult = this.wrapper.queryForList(this.rowMapper);
                }
                if (queryResult == null || queryResult.isEmpty()) {
                    this.eof = true;
                    this.currentIterator = Collections.emptyIterator();
                } else {
                    this.currentIterator = queryResult.iterator();
                }
            } catch (SQLException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        @Override
        public synchronized boolean hasNext() {
            if (this.limitCounter != null && this.limitCounter.get() <= 0) {
                return false;
            }

            if (this.currentIterator == null) {
                this.fetchData();
            }

            if (this.currentIterator.hasNext()) {
                return true;
            } else if (!this.eof) {
                this.pageInfo.nextPage();
                this.fetchData();
                return this.currentIterator.hasNext();
            } else {
                return false;
            }
        }

        @Override
        public synchronized D next() {
            if (this.hasNext()) {
                if (this.limitCounter != null) {
                    this.limitCounter.decrementAndGet();
                }
                return this.transform.apply(this.currentIterator.next());
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
