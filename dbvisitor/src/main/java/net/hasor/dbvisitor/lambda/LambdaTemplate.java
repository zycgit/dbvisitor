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
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.support.entity.EntityDeleteImpl;
import net.hasor.dbvisitor.lambda.support.entity.EntityInsertImpl;
import net.hasor.dbvisitor.lambda.support.entity.EntityQueryImpl;
import net.hasor.dbvisitor.lambda.support.entity.EntityUpdateImpl;
import net.hasor.dbvisitor.lambda.support.freedom.FreedomDelete;
import net.hasor.dbvisitor.lambda.support.freedom.FreedomInsert;
import net.hasor.dbvisitor.lambda.support.freedom.FreedomQuery;
import net.hasor.dbvisitor.lambda.support.freedom.FreedomUpdate;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 继承自 LambdaTemplate 并提供 lambda 方式生成 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class LambdaTemplate implements LambdaOperations {
    protected final MappingRegistry registry;
    protected final JdbcTemplate    jdbc;
    protected final QueryContext    queryContext;

    /**
     * Construct a new LambdaTemplate for bean usage.
     */
    public LambdaTemplate() throws SQLException {
        this((DataSource) null, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new LambdaTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public LambdaTemplate(final DataSource dataSource) throws SQLException {
        this(dataSource, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new LambdaTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param options the options
     */
    public LambdaTemplate(final DataSource dataSource, Options options) throws SQLException {
        this(dataSource, new MappingRegistry(null, options), null);
    }

    /**
     * Construct a new LambdaTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param registry the mapping context
     * @param ctx the sql build context
     */
    public LambdaTemplate(final DataSource dataSource, MappingRegistry registry, QueryContext ctx) throws SQLException {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = (dataSource == null) ? null : new JdbcTemplate(dataSource, registry, ctx);
        this.queryContext = ctx;
    }

    /**
     * Construct a new LambdaTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public LambdaTemplate(final Connection conn) throws SQLException {
        this(conn, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new LambdaTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param options the options
     */
    public LambdaTemplate(final Connection conn, Options options) throws SQLException {
        this(conn, new MappingRegistry(null, options), null);
    }

    /**
     * Construct a new LambdaTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param registry the mapping context
     * @param ctx the sql build context
     */
    public LambdaTemplate(final Connection conn, MappingRegistry registry, QueryContext ctx) throws SQLException {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = (conn == null) ? null : new JdbcTemplate(conn, registry, ctx);
        this.queryContext = ctx;
    }

    /**
     * Construct a new LambdaTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     */
    public LambdaTemplate(final DynamicConnection dynamicConn) throws SQLException {
        this(dynamicConn, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new LambdaTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param options the options
     */
    public LambdaTemplate(final DynamicConnection dynamicConn, Options options) throws SQLException {
        this(dynamicConn, new MappingRegistry(null, options), null);
    }

    /**
     * Construct a new LambdaTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param registry the mapping context
     * @param ctx the sql build context
     */
    public LambdaTemplate(final DynamicConnection dynamicConn, MappingRegistry registry, QueryContext ctx) throws SQLException {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = (dynamicConn == null) ? null : new JdbcTemplate(dynamicConn, registry, ctx);
        this.queryContext = ctx;
    }

    /**
     * Construct a new LambdaTemplate, given a Connection to obtain LambdaTemplate from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param jdbc the JdbcTemplate
     */
    public LambdaTemplate(JdbcTemplate jdbc) throws SQLException {
        this.registry = jdbc.getRegistry();
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc is null.");
        this.queryContext = jdbc.getQueryContext();
    }

    public JdbcTemplate jdbc() {
        return this.jdbc;
    }

    public MappingRegistry getRegistry() {
        return this.registry;
    }

    @Override
    public <T> EntityInsert<T> insert(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new EntityInsertImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityInsert<T> insert(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new EntityInsertImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityUpdate<T> update(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new EntityUpdateImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityUpdate<T> update(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new EntityUpdateImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityDelete<T> delete(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new EntityDeleteImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityDelete<T> delete(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new EntityDeleteImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityQuery<T> query(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new EntityQueryImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityQuery<T> query(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new EntityQueryImpl<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapInsert insertFreedom(String catalog, String schema, String table) {
        return new FreedomInsert(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapUpdate updateFreedom(String catalog, String schema, String table) {
        return new FreedomUpdate(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapDelete deleteFreedom(String catalog, String schema, String table) {
        return new FreedomDelete(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapQuery queryFreedom(String catalog, String schema, String table) {
        return new FreedomQuery(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    protected TableMapping<Map<String, String>> freedomMapping(String catalog, String schema, String table) {
        Options usingOpt = this.registry.getGlobalOptions();

        boolean usingAutoProperty = usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping();
        boolean usingUseDelimited = Boolean.TRUE.equals(usingOpt.getUseDelimited());
        boolean usingMapUnderscoreToCamelCase = Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase());
        boolean usingCaseInsensitive = MappingHelper.caseInsensitive(usingOpt);

        TableDef<?> def = new TableDef<>(catalog, schema, table, LinkedHashMap.class, //
                usingAutoProperty, usingUseDelimited, usingCaseInsensitive, usingMapUnderscoreToCamelCase);
        return (TableMapping<Map<String, String>>) def;
    }

    protected <T> TableMapping<T> findTableMapping(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.registry.findBySpace(space, entityType);
        if (tableMapping == null) {
            tableMapping = this.registry.loadEntityToSpace(entityType, space, entityType.getName());
            if (tableMapping == null) {
                throw new RuntimeSQLException("tableMapping not found.");
            }
        }
        return tableMapping;
    }

    protected <T> TableMapping<T> findTableMapping(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.registry.findByTable(catalog, schema, table, specifyName);
        if (tableMapping == null) {
            throw new RuntimeSQLException("tableMapping not found.");
        } else {
            return tableMapping;
        }
    }
}