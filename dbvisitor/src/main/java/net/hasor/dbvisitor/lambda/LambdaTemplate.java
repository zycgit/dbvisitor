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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.support.entity.DeleteLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.InsertLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.SelectLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.UpdateLambdaForEntity;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Objects;

/**
 * 继承自 JdbcTemplate 并提供 lambda 方式生成 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class LambdaTemplate implements LambdaOperations {
    protected RegistryManager registry;
    protected JdbcTemplate    jdbc;

    /**
     * Construct a new LambdaTemplate for bean usage.
     */
    public LambdaTemplate() {
        this((DataSource) null, RegistryManager.DEFAULT);
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public LambdaTemplate(final DataSource dataSource) {
        this(dataSource, RegistryManager.DEFAULT);
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param registry the DynamicContext
     */
    public LambdaTemplate(final DataSource dataSource, RegistryManager registry) {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = new JdbcTemplate(dataSource, registry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public LambdaTemplate(final Connection conn) {
        this(conn, RegistryManager.DEFAULT);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param registry the DynamicContext
     */
    public LambdaTemplate(final Connection conn, RegistryManager registry) {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = new JdbcTemplate(conn, registry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     */
    public LambdaTemplate(final DynamicConnection dynamicConn) {
        this(dynamicConn, RegistryManager.DEFAULT);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param registry the DynamicContext
     */
    public LambdaTemplate(final DynamicConnection dynamicConn, RegistryManager registry) {
        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = new JdbcTemplate(dynamicConn, registry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain JdbcTemplate from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param jdbc the JdbcTemplate
     */
    public LambdaTemplate(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc is null.");
        this.registry = jdbc.getRegistry();
    }

    public JdbcTemplate getJdbc() {
        return this.jdbc;
    }

    public RegistryManager getRegistry() {
        return this.registry;
    }

    @Override
    public <T> InsertOperation<T> insertBySpace(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new InsertLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> InsertOperation<T> insertByTable(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new InsertLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> EntityUpdateOperation<T> updateBySpace(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new UpdateLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> EntityUpdateOperation<T> updateByTable(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new UpdateLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> EntityDeleteOperation<T> deleteBySpace(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new DeleteLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> EntityDeleteOperation<T> deleteByTable(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new DeleteLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> EntityQueryOperation<T> queryBySpace(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.findTableMapping(entityType, space);
        return new SelectLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    @Override
    public <T> EntityQueryOperation<T> queryByTable(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.findTableMapping(catalog, schema, table, specifyName);
        return new SelectLambdaForEntity<>(tableMapping, this.registry, this.jdbc);
    }

    protected <T> TableMapping<T> findTableMapping(Class<T> entityType, String space) {
        TableMapping<T> tableMapping = this.registry.getMappingRegistry().findBySpace(space, entityType);
        if (tableMapping == null) {
            throw new RuntimeSQLException("tableMapping not found.");
        } else {
            return tableMapping;
        }
    }

    protected <T> TableMapping<T> findTableMapping(String catalog, String schema, String table, String specifyName) {
        TableMapping<T> tableMapping = this.registry.getMappingRegistry().findByTable(catalog, schema, table, specifyName);
        if (tableMapping == null) {
            throw new RuntimeSQLException("tableMapping not found.");
        } else {
            return tableMapping;
        }
    }
}