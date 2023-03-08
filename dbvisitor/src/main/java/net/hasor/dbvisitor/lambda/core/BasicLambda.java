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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.PreparedStatementCreator;
import net.hasor.dbvisitor.jdbc.SqlProvider;
import net.hasor.dbvisitor.jdbc.core.ParameterDisposer;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 所有 SQL 执行器必要的公共属性
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class BasicLambda<R, T, P> {
    protected static final Logger          logger = LoggerFactory.getLogger(BasicLambda.class);
    private                SqlDialect      dialect;
    private final          Class<?>        exampleType;
    private final          boolean         exampleIsMap;
    private final          TableMapping<?> tableMapping;
    private final          LambdaTemplate  jdbcTemplate;
    private                boolean         qualifier;

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
        this.dialect = (tempDialect == null) ? DefaultSqlDialect.DEFAULT : tempDialect;
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

    protected Segment buildSelectByProperty(String propertyName) {
        return buildGroupOrderByProperty(false, true, propertyName);
    }

    protected Segment buildConditionByProperty(String propertyName) {
        return buildGroupOrderByProperty(true, false, propertyName);
    }

    protected Segment buildGroupOrderByProperty(String propertyName) {
        return buildGroupOrderByProperty(false, false, propertyName);
    }

    private Segment buildGroupOrderByProperty(boolean isWhere, boolean isSelect, String propertyName) {
        TableMapping<?> tableMapping = this.getTableMapping();
        String catalogName = tableMapping.getCatalog();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        ColumnMapping propertyInfo = tableMapping.getPropertyByName(propertyName);

        if (propertyInfo == null) {
            String tab = this.dialect.tableName(isQualifier(), catalogName, schemaName, tableName);
            throw new NullPointerException("tableMapping '" + tab + "', property '" + propertyName + "' is not exist.");
        }

        if (!isSelect && isWhere) {
            String specialWhereCol = propertyInfo.getWhereColTemplate();
            if (StringUtils.isNotBlank(specialWhereCol)) {
                return () -> specialWhereCol;
            }
        } else if (isSelect && !isWhere) {
            String specialSelectCol = propertyInfo.getWhereColTemplate();
            if (StringUtils.isNotBlank(specialSelectCol)) {
                return () -> specialSelectCol;
            }
        }

        String columnName = propertyInfo.getColumn();
        return () -> dialect().fmtName(isQualifier(), columnName);
    }

    protected Map<String, String> extractKeysMap(Map entity) {
        Map<String, String> propertySet = getTableMapping().isCaseInsensitive() ? new LinkedCaseInsensitiveMap<>() : new HashMap<>();
        for (Object key : entity.keySet()) {
            String keyStr = key.toString();
            propertySet.put(keyStr, keyStr);
        }
        return propertySet;
    }

    public final BoundSql getBoundSql() {
        return getBoundSql(dialect());
    }

    public final BoundSql getBoundSql(SqlDialect dialect) {
        if (dialect == null) {
            throw new IllegalStateException("dialect is null.");
        } else if (dialect == this.dialect) {
            return buildBoundSql(dialect);
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

    /** 接口 {@link PreparedStatementCreator} 的包装用于实现 SqlProvider 接口，方便打印错误语句 */
    protected static class PreparedStatementCreatorWrap implements PreparedStatementCreator, ParameterDisposer, SqlProvider {
        private final String                   sql;
        private final PreparedStatementCreator creator;

        public PreparedStatementCreatorWrap(String sql, PreparedStatementCreator creator) {
            this.sql = Objects.requireNonNull(sql, "SQL must not be null");
            this.creator = creator;
        }

        @Override
        public String getSql() {
            return sql;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            return this.creator.createPreparedStatement(con);
        }

        @Override
        public void cleanupParameters() {
            if (this.creator instanceof ParameterDisposer) {
                ((ParameterDisposer) this.creator).cleanupParameters();
            }
        }
    }
}