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
import net.hasor.cobble.StringUtils;
import net.hasor.db.JdbcUtils;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.dialect.DefaultSqlDialect;
import net.hasor.db.dialect.SqlDialect;
import net.hasor.db.dialect.SqlDialectRegister;
import net.hasor.db.jdbc.ConnectionCallback;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.lambda.segment.Segment;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.DatabaseMetaData;
import java.util.Map;
import java.util.Objects;

/**
 * 所有 SQL 执行器必要的公共属性
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class BasicLambda<R, T, P> {
    protected final String          dbType;
    private         SqlDialect      dialect;
    private final   Class<?>        exampleType;
    private final   boolean         exampleIsMap;
    private final   TableMapping<?> tableMapping;
    private final   LambdaTemplate  jdbcTemplate;
    private         boolean         qualifier;

    public BasicLambda(Class<?> exampleType, TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        this.exampleType = Objects.requireNonNull(exampleType, "exampleType is null.");
        this.exampleIsMap = Map.class == exampleType || Map.class.isAssignableFrom(this.exampleType());
        this.tableMapping = Objects.requireNonNull(tableMapping, "tableMapping is null.");
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
        this.qualifier = tableMapping.useDelimited();
    }

    BasicLambda(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate, String dbType, SqlDialect dialect) {
        this.exampleType = Objects.requireNonNull(exampleType, "exampleType is null.");
        this.exampleIsMap = Map.class.isAssignableFrom(this.exampleType());
        this.tableMapping = Objects.requireNonNull(tableMapping, "tableMapping is null.");
        this.jdbcTemplate = jdbcTemplate;
        this.dbType = dbType;
        this.dialect = (dialect == null) ? DefaultSqlDialect.DEFAULT : dialect;
        this.qualifier = tableMapping.useDelimited();
    }

    public final Class<?> exampleType() {
        return this.exampleType;
    }

    public R useQualifier() {
        this.qualifier = true;
        return this.getSelf();
    }

    public final LambdaTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    protected final TableMapping<?> getTableMapping() {
        return this.tableMapping;
    }

    protected final SqlDialect dialect() {
        return this.dialect;
    }

    public final void setDialect(SqlDialect sqlDialect) {
        this.dialect = sqlDialect;
    }

    protected boolean isQualifier() {
        return this.qualifier;
    }

    protected boolean exampleIsMap() {
        return this.exampleIsMap;
    }

    protected abstract String getPropertyName(P property);

    protected Segment buildColumnByLambda(P property) {
        String propertyName = getPropertyName(property);
        return buildColumnByProperty(propertyName);
    }

    protected Segment buildColumnByProperty(String propertyName) {
        TableMapping<?> tableMapping = this.getTableMapping();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        ColumnMapping propertyInfo = tableMapping.getPropertyByName(propertyName);

        if (propertyInfo == null) {
            String tab = StringUtils.isBlank(schemaName) ? ("'" + tableName + "'") : ("'" + schemaName + "'.'" + tableName + "'");
            throw new NullPointerException("tableMapping '" + tab + "', property '" + propertyName + "' is not exist.");
        }

        String columnName = propertyInfo.getColumn();
        return () -> dialect().columnName(isQualifier(), schemaName, tableName, columnName);
    }

    public final BoundSql getBoundSql() {
        return getBoundSql(dialect());
    }

    public final BoundSql getBoundSql(SqlDialect dialect) {
        if (dialect == null) {
            throw new IllegalStateException("dialect is null.");
        } else {
            SqlDialect oriDialect = dialect();
            try {
                this.dialect = dialect;
                return buildBoundSql(dialect);
            } finally {
                this.dialect = oriDialect;
            }

        }
    }

    protected abstract BoundSql buildBoundSql(SqlDialect dialect);

    protected abstract R getSelf();

}