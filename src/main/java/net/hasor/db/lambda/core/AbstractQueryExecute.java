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
package net.hasor.db.lambda.core;
import net.hasor.db.dialect.*;
import net.hasor.db.jdbc.ResultSetExtractor;
import net.hasor.db.jdbc.RowCallbackHandler;
import net.hasor.db.jdbc.RowMapper;
import net.hasor.db.lambda.QueryExecute;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.mapping.resolve.MappingOptions;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 带有查询能力的 SQL 执行器基类，实现了 QueryExecute 接口
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractQueryExecute<T> extends AbstractExecute<T> implements QueryExecute<T> {
    private final Page pageInfo = new PageObject(0, this::queryForCount);

    public AbstractQueryExecute(TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(tableMapping, jdbcTemplate);
    }

    AbstractQueryExecute(TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate, String dbType, SqlDialect dialect) {
        super(tableMapping, jdbcTemplate, dbType, dialect);
    }

    /**
     * 由于 pageInfo 是在高层 QueryExecute 实现中提供的能力，因此提供一个开关。让子类来决定是否有能力支持这个特性。
     * 这样在 getBoundSql(SqlDialect) 方法中可以有依据的决定是否进行 sql 改写。
     * 例如：update 和 delete 类别的实现中就不应该支持 page 的改写。 */
    protected abstract boolean supportPage();

    public final BoundSql getBoundSql(SqlDialect dialect) {
        SqlDialect currentSqlDialect = this.dialect();
        try {
            setDialect(dialect);
            int pageSize = this.pageInfo.getPageSize();
            if (supportPage() && pageSize > 0) {
                int recordPosition = this.pageInfo.getFirstRecordPosition();
                return ((PageSqlDialect) dialect).pageSql(getOriginalBoundSql(), recordPosition, pageSize);
            } else {
                return this.getOriginalBoundSql();
            }
        } finally {
            if (currentSqlDialect != dialect) {
                setDialect(currentSqlDialect);
            }
        }
    }

    public final BoundSql getBoundSql() {
        return this.getBoundSql(this.dialect());
    }

    protected abstract BoundSql getOriginalBoundSql();

    public Page pageInfo() {
        return this.pageInfo;
    }

    @Override
    public <V> QueryExecute<V> wrapperType(Class<V> wrapperType, MappingOptions options) {
        TableMapping<V> tableMapping = this.getJdbcTemplate().getTableMapping(wrapperType, options);
        AbstractQueryExecute<T> self = this;
        return new AbstractQueryExecute<V>(tableMapping, this.getJdbcTemplate(), this.dbType, this.dialect()) {
            @Override
            protected boolean supportPage() {
                return AbstractQueryExecute.this.supportPage();
            }

            @Override
            protected BoundSql getOriginalBoundSql() {
                return self.getOriginalBoundSql();
            }
        };
    }

    @Override
    public <V> V query(ResultSetExtractor<V> rse) throws SQLException {
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rse);
    }

    @Override
    public void query(RowCallbackHandler rch) throws SQLException {
        BoundSql boundSql = getBoundSql();
        this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rch);
    }

    @Override
    public <V> List<V> query(RowMapper<V> rowMapper) throws SQLException {
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rowMapper);
    }

    private List<String> fetchColumns(ResultSetMetaData rsmd) throws SQLException {
        int nrOfColumns = rsmd.getColumnCount();
        List<String> columnList = new ArrayList<>();
        for (int i = 1; i <= nrOfColumns; i++) {
            String colName = rsmd.getColumnLabel(i);
            if (colName == null || colName.length() < 1) {
                colName = rsmd.getColumnName(i);
            }
            columnList.add(colName);
        }
        return columnList;
    }

    @Override
    public List<T> queryForList() throws SQLException {
        BoundSql boundSql = getBoundSql();
        TableReader<T> tableReader = getTableMapping().toReader();
        return this.getJdbcTemplate().query(boundSql.getSqlString(), boundSql.getArgs(), rs -> {
            List<String> columns = fetchColumns(rs.getMetaData());
            return tableReader.extractData(columns, rs);
        });
    }

    @Override
    public T queryForObject() throws SQLException {
        BoundSql boundSql = getBoundSql();
        TableReader<T> tableReader = getTableMapping().toReader();
        return this.getJdbcTemplate().queryForObject(boundSql.getSqlString(), boundSql.getArgs(), (rs, rowNum) -> {
            List<String> columns = fetchColumns(rs.getMetaData());
            return tableReader.extractRow(columns, rs, rowNum);
        });
    }

    @Override
    public Map<String, Object> queryForMap() throws SQLException {
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().queryForMap(boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public List<Map<String, Object>> queryForMapList() throws SQLException {
        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().queryForList(boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public int queryForCount() throws SQLException {
        BoundSql countSql = ((PageSqlDialect) this.dialect()).countSql(this.getOriginalBoundSql());
        return this.getJdbcTemplate().queryForInt(countSql.getSqlString(), countSql.getArgs());
    }

    @Override
    public long queryForLargeCount() throws SQLException {
        BoundSql countSql = ((PageSqlDialect) this.dialect()).countSql(this.getOriginalBoundSql());
        return this.getJdbcTemplate().queryForLong(countSql.getSqlString(), countSql.getArgs());
    }
}
