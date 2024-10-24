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
package net.hasor.dbvisitor.lambda;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.core.BasicLambda;
import net.hasor.dbvisitor.lambda.support.entity.DeleteLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.InsertLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.SelectLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.UpdateLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.map.DeleteLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.InsertLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.SelectLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.UpdateLambdaForMap;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 继承自 JdbcTemplate 并提供 lambda 方式生成 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class LambdaTemplate extends JdbcTemplate implements LambdaOperations {
    protected SqlDialect  dialect     = null;
    protected ClassLoader classLoader = null;
    protected boolean     useQualifier;

    /**
     * Construct a new JdbcTemplate for bean usage.
     * <p>Note: The DataSource has to be set before using the instance.
     * @see #setDataSource
     */
    public LambdaTemplate() {
        super();
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public LambdaTemplate(final DataSource dataSource) {
        super(dataSource);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param registry the DynamicContext
     */
    public LambdaTemplate(final DataSource dataSource, DynamicContext registry) {
        super(dataSource, registry);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public LambdaTemplate(final Connection conn) {
        super(conn);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param registry the DynamicContext
     */
    public LambdaTemplate(final Connection conn, DynamicContext registry) {
        super(conn, registry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     */
    public LambdaTemplate(final DynamicConnection dynamicConn) {
        super(dynamicConn);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param registry the DynamicContext
     */
    public LambdaTemplate(final DynamicConnection dynamicConn, DynamicContext registry) {
        super(dynamicConn, registry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain JdbcTemplate from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param jdbcTemplate the JdbcTemplate
     */
    public LambdaTemplate(JdbcTemplate jdbcTemplate) {
        super();
        this.setDataSource(jdbcTemplate.getDataSource());
        this.setConnection(jdbcTemplate.getConnection());
        this.setDynamic(jdbcTemplate.getDynamic());

        this.setResultsCaseInsensitive(jdbcTemplate.isResultsCaseInsensitive());
        this.setRegistry(jdbcTemplate.getRegistry());
        this.setFetchSize(jdbcTemplate.getFetchSize());
        this.setMaxRows(jdbcTemplate.getMaxRows());
        this.setQueryTimeout(jdbcTemplate.getQueryTimeout());
        this.setIgnoreWarnings(jdbcTemplate.isIgnoreWarnings());
        this.setPrintStmtError(jdbcTemplate.isPrintStmtError());
    }

    protected void init() {
        this.dialect = fetchDialect();
    }

    protected SqlDialect fetchDialect() {
        if (getConnection() == null && getDynamic() == null && getDataSource() == null) {
            return DefaultSqlDialect.DEFAULT;
        }

        String tmpDbType = "";
        try {
            tmpDbType = this.execute((ConnectionCallback<String>) con -> {
                DatabaseMetaData metaData = con.getMetaData();
                return JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
            });
        } catch (Exception e) {
            tmpDbType = "";
        }

        return SqlDialectRegister.findOrCreate(tmpDbType);
    }

    public SqlDialect getDialect() {
        return this.dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public boolean isUseQualifier() {
        return this.useQualifier;
    }

    public void setUseQualifier(boolean useQualifier) {
        this.useQualifier = useQualifier;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public <T> TableMapping<T> getTableMapping(Class<T> exampleType, MappingOptions opt) {
        if (exampleType == null) {
            throw new NullPointerException("exampleType is null.");
        }
        if (exampleType == Map.class) {
            throw new UnsupportedOperationException("Map cannot be used as lambda exampleType.");
        }

        MappingRegistry registry = getRegistry().getMappingRegistry();
        TableMapping<T> def = registry.findUsingSpace(exampleType);
        if (def != null) {
            return def;
        } else {
            MappingOptions copyOpt = MappingOptions.buildNew(opt);
            copyOpt.setUseDelimited(Boolean.TRUE.equals(copyOpt.getUseDelimited()));
            copyOpt.setCaseInsensitive(this.isResultsCaseInsensitive());
            copyOpt.setMapUnderscoreToCamelCase(Boolean.TRUE.equals(copyOpt.getMapUnderscoreToCamelCase()));
            copyOpt.setDefaultDialect(this.getDialect());
            return registry.loadEntityToSpace(exampleType);
        }
    }

    public TableMapping<?> getTableMapping(final String catalog, final String schema, final String table, MappingOptions opt) throws SQLException {
        if (StringUtils.isBlank(table)) {
            throw new NullPointerException("table is blank.");
        }
        MappingRegistry registry = getRegistry().getMappingRegistry();

        TableMapping<?> usingTable = registry.findUsingTable(catalog, schema, table);
        if (usingTable != null) {
            return usingTable;
        } else {
            MappingOptions copyOpt = MappingOptions.buildNew(opt);
            copyOpt.setUseDelimited(Boolean.TRUE.equals(copyOpt.getUseDelimited()));
            copyOpt.setCaseInsensitive(this.isResultsCaseInsensitive());
            copyOpt.setMapUnderscoreToCamelCase(Boolean.TRUE.equals(copyOpt.getMapUnderscoreToCamelCase()));
            copyOpt.setDefaultDialect(this.getDialect());

            TableMapping<?> def = registry.loadEntityToTable(LinkedHashMap.class, catalog, schema, table);
            return def;
        }
    }

    protected <E extends BasicLambda<R, T, P>, R, T, P> E configLambda(E execute) {
        if (this.useQualifier) {
            execute.useQualifier();
        }
        return execute;
    }

    @Override
    public <T> InsertOperation<T> lambdaInsert(Class<T> exampleType, MappingOptions options) {
        return configLambda(new InsertLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), options, this));
    }

    @Override
    public InsertOperation<Map<String, Object>> lambdaInsert(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new InsertLambdaForMap(getTableMapping(catalog, schema, table, options), options, this));
    }

    @Override
    public <T> EntityUpdateOperation<T> lambdaUpdate(Class<T> exampleType, MappingOptions options) {
        return configLambda(new UpdateLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), options, this));
    }

    @Override
    public MapUpdateOperation lambdaUpdate(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new UpdateLambdaForMap(getTableMapping(catalog, schema, table, options), options, this));
    }

    @Override
    public <T> EntityDeleteOperation<T> lambdaDelete(Class<T> exampleType, MappingOptions options) {
        return configLambda(new DeleteLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), options, this));
    }

    @Override
    public MapDeleteOperation lambdaDelete(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new DeleteLambdaForMap(getTableMapping(catalog, schema, table, options), options, this));
    }

    @Override
    public <T> EntityQueryOperation<T> lambdaQuery(Class<T> exampleType, MappingOptions options) {
        return configLambda(new SelectLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), options, this));
    }

    @Override
    public MapQueryOperation lambdaQuery(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new SelectLambdaForMap(getTableMapping(catalog, schema, table, options), options, this));
    }
}