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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.wrapper.support.entity.DeleteWrapperForEntity;
import net.hasor.dbvisitor.wrapper.support.entity.InsertWrapperForEntity;
import net.hasor.dbvisitor.wrapper.support.entity.SelectWrapperForEntity;
import net.hasor.dbvisitor.wrapper.support.entity.UpdateWrapperForEntity;
import net.hasor.dbvisitor.wrapper.support.freedom.DeleteWrapperForFreedom;
import net.hasor.dbvisitor.wrapper.support.freedom.InsertWrapperForFreedom;
import net.hasor.dbvisitor.wrapper.support.freedom.SelectWrapperForFreedom;
import net.hasor.dbvisitor.wrapper.support.freedom.UpdateWrapperForFreedom;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 继承自 WrapperAdapter 并提供 lambda 方式生成 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class WrapperAdapter implements WrapperOperations {
    protected final MappingRegistry registry;
    protected final JdbcTemplate    jdbc;
    protected final QueryContext    queryContext;

    /**
     * Construct a new LambdaTemplate for bean usage.
     */
    public WrapperAdapter() throws SQLException {
        this((DataSource) null, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new WrapperAdapter, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public WrapperAdapter(final DataSource dataSource) throws SQLException {
        this(dataSource, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new WrapperAdapter, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param options the options
     */
    public WrapperAdapter(final DataSource dataSource, Options options) throws SQLException {
        this(dataSource, new MappingRegistry(null, options), null);
    }

    /**
     * Construct a new WrapperAdapter, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param registry the mapping context
     * @param ctx the sql build context
     */
    public WrapperAdapter(final DataSource dataSource, MappingRegistry registry, QueryContext ctx) throws SQLException {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = (dataSource == null) ? null : new JdbcTemplate(dataSource, registry, ctx);
        this.queryContext = ctx;
    }

    /**
     * Construct a new WrapperAdapter, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public WrapperAdapter(final Connection conn) throws SQLException {
        this(conn, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new WrapperAdapter, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param options the options
     */
    public WrapperAdapter(final Connection conn, Options options) throws SQLException {
        this(conn, new MappingRegistry(null, options), null);
    }

    /**
     * Construct a new WrapperAdapter, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param registry the mapping context
     * @param ctx the sql build context
     */
    public WrapperAdapter(final Connection conn, MappingRegistry registry, QueryContext ctx) throws SQLException {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = (conn == null) ? null : new JdbcTemplate(conn, registry, ctx);
        this.queryContext = ctx;
    }

    /**
     * Construct a new WrapperAdapter, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     */
    public WrapperAdapter(final DynamicConnection dynamicConn) throws SQLException {
        this(dynamicConn, MappingRegistry.DEFAULT, null);
    }

    /**
     * Construct a new WrapperAdapter, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param options the options
     */
    public WrapperAdapter(final DynamicConnection dynamicConn, Options options) throws SQLException {
        this(dynamicConn, new MappingRegistry(null, options), null);
    }

    /**
     * Construct a new WrapperAdapter, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param registry the mapping context
     * @param ctx the sql build context
     */
    public WrapperAdapter(final DynamicConnection dynamicConn, MappingRegistry registry, QueryContext ctx) throws SQLException {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = (dynamicConn == null) ? null : new JdbcTemplate(dynamicConn, registry, ctx);
        this.queryContext = ctx;
    }

    /**
     * Construct a new WrapperAdapter, given a Connection to obtain WrapperAdapter from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param jdbc the JdbcTemplate
     */
    public WrapperAdapter(JdbcTemplate jdbc) throws SQLException {
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
    public <T> EntityInsertWrapper<T> insert(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new InsertWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityInsertWrapper<T> insert(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new InsertWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityUpdateWrapper<T> update(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new UpdateWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityUpdateWrapper<T> update(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new UpdateWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityDeleteWrapper<T> delete(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new DeleteWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityDeleteWrapper<T> delete(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new DeleteWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityQueryWrapper<T> query(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new SelectWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public <T> EntityQueryWrapper<T> query(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new SelectWrapperForEntity<>(tableMapping, this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapInsertWrapper insertFreedom(String catalog, String schema, String table) {
        return new InsertWrapperForFreedom(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapUpdateWrapper updateFreedom(String catalog, String schema, String table) {
        return new UpdateWrapperForFreedom(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapDeleteWrapper deleteFreedom(String catalog, String schema, String table) {
        return new DeleteWrapperForFreedom(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
    }

    @Override
    public MapQueryWrapper queryFreedom(String catalog, String schema, String table) {
        return new SelectWrapperForFreedom(this.freedomMapping(catalog, schema, table), this.registry, this.jdbc, this.queryContext);
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

    private static SqlDialect findSqlDialect(MappingRegistry registry, JdbcTemplate jdbc) throws SQLException {
        if (registry.getGlobalOptions().getDialect() != null) {
            return registry.getGlobalOptions().getDialect();
        } else {
            return (jdbc == null) ? DefaultSqlDialect.DEFAULT : jdbc.execute((ConnectionCallback<SqlDialect>) JdbcHelper::findDialect);
        }
    }
}