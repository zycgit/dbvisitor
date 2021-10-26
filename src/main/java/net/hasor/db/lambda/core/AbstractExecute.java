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
import net.hasor.db.JdbcUtils;
import net.hasor.db.dialect.DefaultSqlDialect;
import net.hasor.db.dialect.SqlDialect;
import net.hasor.db.dialect.SqlDialectRegister;
import net.hasor.db.jdbc.ConnectionCallback;
import net.hasor.db.lambda.segment.Segment;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.DatabaseMetaData;
import java.util.Objects;

/**
 * 所有 SQL 执行器必要的公共属性
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractExecute<T> {
    protected final String         dbType;
    private         SqlDialect     dialect;
    private final   TableReader<T> tableReader;
    private final   LambdaTemplate jdbcTemplate;
    private         boolean        qualifier;

    public AbstractExecute(TableReader<T> tableReader, LambdaTemplate jdbcTemplate) {
        this.tableReader = Objects.requireNonNull(tableReader, "tableReader is null.");
        this.jdbcTemplate = jdbcTemplate;

        String tmpDbType = "";
        try {
            tmpDbType = jdbcTemplate.execute((ConnectionCallback<String>) con -> {
                DatabaseMetaData metaData = con.getMetaData();
                return JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
            });
        } catch (Exception e) {
            tmpDbType = "";
        }

        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        this.dbType = tmpDbType;
        this.dialect = (tempDialect == null) ? DefaultSqlDialect.DEFAULT : tempDialect;
    }

    AbstractExecute(TableReader<T> tableReader, LambdaTemplate jdbcTemplate, String dbType, SqlDialect dialect) {
        this.tableReader = Objects.requireNonNull(tableReader, "tableReader is null.");
        this.jdbcTemplate = jdbcTemplate;
        this.dbType = dbType;
        this.dialect = (dialect == null) ? DefaultSqlDialect.DEFAULT : dialect;
    }

    public final Class<T> exampleType() {
        return this.tableReader.getTableMapping().entityType();
    }

    public final LambdaTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    protected final TableMapping<T> getTableMapping() {
        return this.tableReader.getTableMapping();
    }

    protected final TableReader<T> getTableReader() {
        return this.tableReader;
    }

    protected final SqlDialect dialect() {
        return this.dialect;
    }

    protected final void setDialect(SqlDialect sqlDialect) {
        this.dialect = sqlDialect;
    }

    protected void enableQualifier() {
        this.qualifier = true;
    }

    protected boolean isQualifier() {
        return this.qualifier;
    }

    protected Segment buildTabName(SqlDialect dialect) {
        TableMapping<T> tableMapping = this.getTableMapping();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        return () -> dialect.tableName(isQualifier(), schemaName, tableName);
    }
}
