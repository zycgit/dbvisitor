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
package net.hasor.db.jdbc.lambda.query;
import net.hasor.db.JdbcUtils;
import net.hasor.db.jdbc.*;
import net.hasor.db.jdbc.lambda.LambdaOperations.BoundSql;
import net.hasor.db.jdbc.lambda.QueryExecute;
import net.hasor.db.jdbc.lambda.dialect.SqlDialect;
import net.hasor.db.jdbc.lambda.dialect.SqlDialectRegister;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Lambda SQL 执行器
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractQueryExecute<T> implements QueryExecute<T>, BoundSql {
    protected final String         dbType;
    protected final SqlDialect     dialect;
    private final   Class<T>       exampleType;
    private final   JdbcOperations jdbcOperations;

    public AbstractQueryExecute(Class<T> exampleType, JdbcOperations jdbcOperations) {
        this.exampleType = exampleType;
        this.jdbcOperations = jdbcOperations;
        String tmpDbType;
        try {
            tmpDbType = this.getJdbcOperations().execute((ConnectionCallback<String>) con -> {
                DatabaseMetaData metaData = con.getMetaData();
                return JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
            });
        } catch (Exception e) {
            tmpDbType = "";
        }
        //
        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        this.dbType = tmpDbType;
        this.dialect = (tempDialect == null) ? SqlDialect.DEFAULT : tempDialect;
    }

    AbstractQueryExecute(Class<T> exampleType, JdbcOperations jdbcOperations, String dbType, SqlDialect dialect) {
        this.exampleType = exampleType;
        this.jdbcOperations = jdbcOperations;
        this.dbType = dbType;
        this.dialect = (dialect == null) ? SqlDialect.DEFAULT : dialect;
    }

    public Class<T> exampleType() {
        return this.exampleType;
    }

    public JdbcOperations getJdbcOperations() {
        return this.jdbcOperations;
    }

    @Override
    public <V> QueryExecute<V> wrapperType(Class<V> wrapperType) {
        AbstractQueryExecute<T> self = this;
        return new AbstractQueryExecute<V>(wrapperType, this.jdbcOperations, this.dbType, this.dialect) {
            public String getSqlString() {
                return self.getSqlString();
            }

            public Map<String, Object> getArgs() {
                return self.getArgs();
            }
        };
    }

    @Override
    public T query(ResultSetExtractor<T> rse) throws SQLException {
        return this.jdbcOperations.query(getSqlString(), getArgs(), rse);
    }

    @Override
    public void query(RowCallbackHandler rch) throws SQLException {
        this.jdbcOperations.query(getSqlString(), getArgs(), rch);
    }

    @Override
    public List<T> query(RowMapper<T> rowMapper) throws SQLException {
        return this.jdbcOperations.query(getSqlString(), getArgs(), rowMapper);
    }

    @Override
    public List<T> queryForList() throws SQLException {
        return this.jdbcOperations.queryForList(getSqlString(), getArgs(), exampleType());
    }

    @Override
    public T queryForObject() throws SQLException {
        return this.jdbcOperations.queryForObject(getSqlString(), getArgs(), exampleType());
    }

    @Override
    public Map<String, Object> queryForMap() throws SQLException {
        return this.jdbcOperations.queryForMap(getSqlString(), getArgs());
    }

    @Override
    public List<Map<String, Object>> queryForMapList() throws SQLException {
        return this.jdbcOperations.queryForList(getSqlString(), getArgs());
    }

    @Override
    public long queryForLong() throws SQLException {
        return this.jdbcOperations.queryForLong(getSqlString(), getArgs());
    }

    @Override
    public int queryForInt() throws SQLException {
        return this.jdbcOperations.queryForInt(getSqlString(), getArgs());
    }
}