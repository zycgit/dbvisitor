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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.DalMapper;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.template.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.template.jdbc.DynamicConnection;
import net.hasor.dbvisitor.template.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * 基础数据库操作接口
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-19
 */
public class Session extends JdbcAccessor implements Closeable {
    private static final Logger           log = LoggerFactory.getLogger(Session.class);
    private final        Configuration    configuration;
    private final        SessionPrototype prototype;
    private final        WrapperAdapter   adapter;
    private final        JdbcTemplate     jdbc;

    public Session(Connection conn, SessionPrototype prototype, Configuration configuration) {
        this.setConnection(Objects.requireNonNull(conn, "connection is null."));
        this.prototype = Objects.requireNonNull(prototype, "prototype is null.");
        this.configuration = Objects.requireNonNull(configuration, "configuration is null.");
        this.adapter = configuration.newWrapper(conn);
        this.jdbc = this.adapter.getJdbc();
    }

    public Session(DynamicConnection dc, SessionPrototype prototype, Configuration configuration) {
        this.setDynamic(Objects.requireNonNull(dc, "DynamicConnection is null."));
        this.prototype = Objects.requireNonNull(prototype, "prototype is null.");
        this.configuration = Objects.requireNonNull(configuration, "configuration is null.");
        this.adapter = configuration.newWrapper(dc);
        this.jdbc = this.adapter.getJdbc();
    }

    public Session(DataSource ds, SessionPrototype prototype, Configuration configuration) {
        this.setDataSource(Objects.requireNonNull(ds, "dataSource is null."));
        this.prototype = Objects.requireNonNull(prototype, "prototype is null.");
        this.configuration = Objects.requireNonNull(configuration, "configuration is null.");
        this.adapter = configuration.newWrapper(ds);
        this.jdbc = this.adapter.getJdbc();
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

    /** {@link WrapperAdapter} */
    public WrapperAdapter wrapper() {
        return this.adapter;
    }

    /** {@link JdbcTemplate} */
    public JdbcTemplate jdbc() {
        return this.jdbc;
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

    public <T> T createMapper(Class<T> mapperType) {
        if (!mapperType.isInterface()) {
            throw new UnsupportedOperationException("mapperType " + mapperType.getName() + " is not interface.");
        }

        boolean isMapper = false;
        String resource = null;
        Annotation[] annotations = mapperType.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof RefMapper) {
                resource = ((RefMapper) annotation).value();
            }
            if (annotation instanceof DalMapper || annotation.annotationType().getAnnotation(DalMapper.class) != null) {
                isMapper = true;
                break;
            }
        }
        //        if (!isMapper && !this.registry.hasScope(mapperType)) {
        //            throw new UnsupportedOperationException("type '" + mapperType.getName() + "' need @RefMapper or @SimpleMapper or @DalMapper");
        //        }

        if (StringUtils.isNotBlank(resource) /*&& !this.dalRegistry.hasLoaded(resource)*/) {
            try {
                //  this.registry.loadMapper(resource);
            } catch (Exception e) {
                log.error("loadMapper '" + resource + "' failed, " + e.getMessage(), e);
            }
        }

        DefaultBaseMapper mapperHandler = null;
        if (BaseMapper.class.isAssignableFrom(mapperType)) {
            ResolvableType type = ResolvableType.forClass(mapperType).as(BaseMapper.class);
            Class<?>[] generics = type.resolveGenerics(Object.class);
            Class<?> entityType = generics[0];

            //            if (this.registry.findByEntity(entityType) == null) {
            //                this.registry.loadEntityToSpace(entityType);
            //            }

            mapperHandler = null;// new DefaultBaseMapper(mapperType.getName(), entityType, this);
        }

        ClassLoader classLoader = this.configuration.getClassLoader();
        InvocationHandler handler = new ExecuteInvocationHandler(this, mapperType, this.configuration, mapperHandler);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { mapperType }, handler);
    }
}