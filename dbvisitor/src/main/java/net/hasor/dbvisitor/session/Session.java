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
package net.hasor.dbvisitor.session;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 基础数据库操作接口
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
public class Session extends JdbcAccessor implements Closeable {
    private final Configuration    configuration;
    private final SessionPrototype prototype;
    private final LambdaTemplate   lambda;
    private final JdbcTemplate     jdbc;

    public Session(Connection conn, SessionPrototype prototype) throws SQLException {
        this.setConnection(Objects.requireNonNull(conn, "connection is null."));
        this.prototype = Objects.requireNonNull(prototype, "prototype is null.");
        this.configuration = Objects.requireNonNull(prototype.getConfiguration(), "configuration is null.");
        this.lambda = this.configuration.newLambda(conn);
        this.jdbc = this.lambda.jdbc();
    }

    public Session(DynamicConnection dc, SessionPrototype prototype) throws SQLException {
        this.setDynamic(Objects.requireNonNull(dc, "dynamicConnection is null."));
        this.prototype = Objects.requireNonNull(prototype, "prototype is null.");
        this.configuration = Objects.requireNonNull(prototype.getConfiguration(), "configuration is null.");
        this.lambda = this.configuration.newLambda(dc);
        this.jdbc = this.lambda.jdbc();
    }

    public Session(DataSource ds, SessionPrototype prototype) throws SQLException {
        this.setDataSource(Objects.requireNonNull(ds, "dataSource is null."));
        this.prototype = Objects.requireNonNull(prototype, "prototype is null.");
        this.configuration = Objects.requireNonNull(prototype.getConfiguration(), "configuration is null.");
        this.lambda = this.configuration.newLambda(ds);
        this.jdbc = this.lambda.jdbc();
    }

    @Override
    public void close() throws IOException {
        if (this.getConnection() != null) {
            IOUtils.closeQuietly(this.getConnection());
        }
        this.setDataSource(null);
        this.setConnection(null);
        this.setDynamic(null);
    }

    /** {@link LambdaTemplate} */
    public LambdaTemplate lambda() {
        return this.lambda;
    }

    /** {@link JdbcTemplate} */
    public JdbcTemplate jdbc() {
        return this.jdbc;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public Object executeStatement(String stId, Object parameter) throws SQLException {
        return this.jdbc.execute((ConnectionCallback<Object>) con -> {
            return prototype.executeStatement(con, stId, parameter);
        });
    }

    public <E> List<E> queryStatement(String stId, Object parameter) throws SQLException {
        return this.jdbc.execute((ConnectionCallback<List<E>>) con -> {
            return prototype.queryStatement(con, stId, parameter, null);
        });
    }

    public <E> List<E> queryStatement(String stId, Object parameter, Page page) throws SQLException {
        return this.jdbc.execute((ConnectionCallback<List<E>>) con -> {
            return prototype.queryStatement(con, stId, parameter, page);
        });
    }

    public <E> PageResult<E> pageStatement(String stId, Object parameter, Page page) throws SQLException {
        return this.jdbc.execute((ConnectionCallback<PageResult<E>>) con -> {
            return prototype.pageStatement(con, stId, parameter, page);
        });
    }

    public <T> BaseMapper<T> createBaseMapper(Class<T> entityType) {
        TableMapping<?> entity = this.configuration.findByEntity(entityType);
        if (entity == null) {
            entity = this.configuration.loadEntityToSpace(entityType);
        }
        return (BaseMapper<T>) new DefaultBaseMapper((TableMapping<Object>) entity, this);
    }

    public <T> BaseMapper<T> createBaseMapper(Class<T> entityType, String namespace) {
        TableMapping<?> entity = configuration.findBySpace(namespace, entityType);
        if (entity == null) {
            entity = this.configuration.loadEntityToSpace(entityType, namespace);
        }
        return (BaseMapper<T>) new DefaultBaseMapper((TableMapping<Object>) entity, this);
    }

    public <T> T createMapper(Class<T> mapperType) throws Exception {
        this.configuration.loadMapper(mapperType);

        String space = mapperType.getName();
        DefaultBaseMapper baseMapper = null;

        if (BaseMapper.class.isAssignableFrom(mapperType)) {
            ResolvableType type = ResolvableType.forClass(mapperType).as(BaseMapper.class);
            Class<?>[] generics = type.resolveGenerics(Object.class);
            Class<?> entityType = generics[0];

            TableMapping<?> entity = this.configuration.findBySpace(space, entityType);
            if (entity == null) {
                entity = this.configuration.findByEntity(entityType);
                if (entity == null) {
                    entity = this.configuration.loadEntityToSpace(entityType, space);
                }
            }

            baseMapper = new DefaultBaseMapper((TableMapping<Object>) entity, this);
        }

        ClassLoader classLoader = this.configuration.getClassLoader();
        InvocationHandler handler = new ExecuteInvocationHandler(space, mapperType, this, this.configuration, baseMapper);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { mapperType }, handler);
    }
}