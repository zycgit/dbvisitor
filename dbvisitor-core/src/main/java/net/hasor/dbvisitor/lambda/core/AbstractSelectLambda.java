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
package net.hasor.dbvisitor.lambda.core;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.mapper.MappingResultSetExtractor;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.lambda.segment.OrderByKeyword;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.hasor.dbvisitor.lambda.segment.OrderByKeyword.*;
import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda query 基础能力。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractSelectLambda<R, T, P> extends BasicQueryCompare<R, T, P> implements QueryFunc<R, T, P> {
    protected final MergeSqlSegment customSelect = new MergeSqlSegment();
    protected final MergeSqlSegment groupByList  = new MergeSqlSegment();
    protected final MergeSqlSegment orderByList  = new MergeSqlSegment();
    private final   Page            pageInfo     = new PageObject(0, this::queryForCount);
    private         boolean         lockGroupBy  = false;
    private         boolean         lockOrderBy  = false;

    public AbstractSelectLambda(Class<?> exampleType, TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
    }

    @Override
    public R selectAll() {
        this.customSelect.cleanSegment();
        return this.getSelf();
    }

    @Override
    public final R select(P... properties) {
        if (properties == null || properties.length == 0) {
            throw new IndexOutOfBoundsException("properties is empty.");
        }

        this.customSelect.cleanSegment();

        for (P property : properties) {
            if (!this.customSelect.isEmpty()) {
                this.customSelect.addSegment(() -> ",");
            }
            this.customSelect.addSegment(buildColumnByLambda(property));
        }
        return this.getSelf();
    }

    @Override
    public R applySelect(String select) {
        this.customSelect.cleanSegment();
        this.customSelect.addSegment(() -> select);
        return this.getSelf();
    }

    protected void lockGroupBy() {
        this.lockGroupBy = true;
    }

    @Override
    public final R groupBy(P... groupBy) {
        if (this.lockGroupBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();

        if (groupBy != null && groupBy.length > 0) {
            if (this.groupByList.isEmpty()) {
                this.queryTemplate.addSegment(GROUP_BY);
                this.queryTemplate.addSegment(this.groupByList);
            }

            for (P property : groupBy) {
                if (!this.groupByList.isEmpty()) {
                    this.groupByList.addSegment(() -> ",");
                }
                this.groupByList.addSegment(buildColumnByLambda(property));

            }
        }
        return this.getSelf();
    }

    @Override
    public R orderBy(P... orderBy) {
        return this.addOrderBy(ORDER_DEFAULT, orderBy);
    }

    @Override
    public R asc(P... orderBy) {
        return this.addOrderBy(ASC, orderBy);
    }

    @Override
    public R desc(P... orderBy) {
        return this.addOrderBy(DESC, orderBy);
    }

    protected void lockOrderBy() {
        this.lockGroupBy = true;
    }

    private R addOrderBy(OrderByKeyword keyword, P... orderBy) {
        if (this.lockOrderBy) {
            throw new IllegalStateException("must before order by invoke it.");
        }

        lockCondition();
        lockGroupBy();

        if (orderBy != null && orderBy.length > 0) {
            if (this.orderByList.isEmpty()) {
                this.queryTemplate.addSegment(ORDER_BY);
                this.queryTemplate.addSegment(this.orderByList);
            }
            for (P property : orderBy) {
                if (!this.orderByList.isEmpty()) {
                    this.orderByList.addSegment(() -> ",");
                }
                this.orderByList.addSegment(buildColumnByLambda(property), keyword);
            }
        }
        return this.getSelf();
    }

    @Override
    public R usePage(Page pageInfo) {
        Page page = this.pageInfo();
        page.setPageSize(pageInfo.getPageSize());
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
        BoundSql boundSql = getBoundSql();
        this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rch);
    }

    @Override
    public <V> V query(ResultSetExtractor<V> rse) throws SQLException {
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rse);
    }

    @Override
    public <V> List<V> query(RowMapper<V> rowMapper) throws SQLException {
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rowMapper);
    }

    @Override
    public List<T> queryForList() throws SQLException {
        BoundSql boundSql = getBoundSql();
        ResultSetExtractor<List<T>> extractor = new MappingResultSetExtractor<>(getTableReader());
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), extractor);
    }

    @Override
    public List<Map<String, Object>> queryForMapList() throws SQLException {
        BoundSql boundSql = getBoundSql();
        ResultSetExtractor<List<Map<String, Object>>> extractor = new MappingResultSetExtractor<>(getTableMapping().toMapReader());
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), extractor);
    }

    @Override
    public Map<String, Object> queryForMap() throws SQLException {
        BoundSql boundSql = getBoundSql();
        TableReader<Map<String, Object>> mapReader = getTableMapping().toMapReader();
        return this.getJdbcTemplate().queryForObject(boundSql.getSqlString(), boundSql.getArgs(), (rs, rowNum) -> {
            ResultSetMetaData rsmd = rs.getMetaData();
            int nrOfColumns = rsmd.getColumnCount();
            List<String> columnList = new ArrayList<>();
            for (int i = 1; i <= nrOfColumns; i++) {
                String name = rsmd.getColumnLabel(i);
                if (name == null || name.length() < 1) {
                    name = rsmd.getColumnName(i);
                }
                columnList.add(name);
            }

            return mapReader.extractRow(columnList, rs, rowNum);
        });
    }

    protected abstract TableReader<T> getTableReader();

    @Override
    public T queryForObject() throws SQLException {
        TableReader<T> tableReader = getTableReader();
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().queryForObject(boundSql.getSqlString(), boundSql.getArgs(), (rs, rowNum) -> {
            ResultSetMetaData rsmd = rs.getMetaData();
            int nrOfColumns = rsmd.getColumnCount();
            List<String> columnList = new ArrayList<>();
            for (int i = 1; i <= nrOfColumns; i++) {
                String colName = rsmd.getColumnLabel(i);
                if (colName == null || colName.length() < 1) {
                    colName = rsmd.getColumnName(i);
                }
                columnList.add(colName);
            }

            return tableReader.extractRow(columnList, rs, rowNum);
        });
    }

    @Override
    public int queryForCount() throws SQLException {
        BoundSql oriBoundSql = this.buildBoundSqlWithoutPage(dialect());
        BoundSql countSql = ((PageSqlDialect) this.dialect()).countSql(oriBoundSql);
        return this.getJdbcTemplate().queryForInt(countSql.getSqlString(), countSql.getArgs());
    }

    @Override
    public long queryForLargeCount() throws SQLException {
        BoundSql oriBoundSql = this.buildBoundSqlWithoutPage(dialect());
        BoundSql countSql = ((PageSqlDialect) this.dialect()).countSql(oriBoundSql);
        return this.getJdbcTemplate().queryForLong(countSql.getSqlString(), countSql.getArgs());
    }

    @Override
    protected BoundSql buildBoundSql(final SqlDialect dialect) {
        int pageSize = pageInfo().getPageSize();
        if (pageSize > 0) {
            BoundSql sqlWithoutPage = buildBoundSqlWithoutPage(dialect);
            int position = pageInfo().getFirstRecordPosition();
            return ((PageSqlDialect) dialect).pageSql(sqlWithoutPage, position, pageSize);
        } else {
            return buildBoundSqlWithoutPage(dialect);
        }
    }

    private BoundSql buildBoundSqlWithoutPage(final SqlDialect dialect) {
        MergeSqlSegment sqlSegment = new MergeSqlSegment();
        sqlSegment.addSegment(SELECT);
        if (this.customSelect.isEmpty()) {
            if (this.groupByList.isEmpty()) {
                sqlSegment.addSegment(() -> "*");
            } else {
                sqlSegment.addSegment(this.groupByList);
            }
        } else {
            sqlSegment.addSegment(this.customSelect);
        }
        sqlSegment.addSegment(FROM);
        sqlSegment.addSegment(() -> {
            TableMapping<?> tableMapping = this.getTableMapping();
            String schemaName = tableMapping.getSchema();
            String tableName = tableMapping.getTable();
            return dialect.tableName(isQualifier(), schemaName, tableName);
        });

        if (!this.queryTemplate.isEmpty()) {
            Segment firstSqlSegment = this.queryTemplate.firstSqlSegment();
            if (firstSqlSegment == GROUP_BY || firstSqlSegment == HAVING || firstSqlSegment == ORDER_BY) {
                sqlSegment.addSegment(this.queryTemplate);
            } else {
                sqlSegment.addSegment(WHERE);
                sqlSegment.addSegment(this.queryTemplate.sub(1));
            }
        }

        // if have any group by condition, then orderBy must be in groupBy

        String sqlQuery = sqlSegment.getSqlSegment();
        Object[] args = this.queryParam.toArray().clone();
        return new BoundSql.BoundSqlObj(sqlQuery, args);
    }
}
