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
package net.hasor.dbvisitor.dal.session;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterBean;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.JdbcHelper;
import net.hasor.dbvisitor.dal.execute.ExecuteProxy;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalMapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.page.Page;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 基础数据库操作接口
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-19
 */
public class DalSession extends JdbcAccessor {
    private static final Logger                           log          = LoggerFactory.getLogger(DalSession.class);
    private final        DalRegistry                      dalRegistry;
    private final        PageSqlDialect                   dialect;
    private final        LambdaTemplate                   defaultTemplate;
    private final        Function<String, LambdaTemplate> spaceTemplateFactory;
    private final        Map<String, ExecuteProxy>        executeCache = new ConcurrentHashMap<>();

    public DalSession(Connection connection) throws SQLException {
        this(connection, DalRegistry.DEFAULT);
    }

    public DalSession(DataSource dataSource) throws SQLException {
        this(dataSource, DalRegistry.DEFAULT);
    }

    public DalSession(DynamicConnection dynamicConnection) throws SQLException {
        this(dynamicConnection, DalRegistry.DEFAULT);
    }

    public DalSession(Connection connection, DalRegistry dalRegistry) throws SQLException {
        this.setConnection(Objects.requireNonNull(connection, "connection is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.defaultTemplate = new DalLambdaTemplate(connection);
        this.spaceTemplateFactory = space -> this.defaultTemplate;

        SqlDialect defaultDialect = this.dalRegistry.getGlobalOptions() != null ? this.dalRegistry.getGlobalOptions().getDefaultDialect() : null;
        if (defaultDialect instanceof PageSqlDialect) {
            this.dialect = (PageSqlDialect) defaultDialect;
        } else {
            this.dialect = this.lambdaTemplate().getJdbc().execute(this::findPageDialect);
        }
    }

    public DalSession(DynamicConnection dynamicConn, DalRegistry dalRegistry) throws SQLException {
        this.setDynamic(Objects.requireNonNull(dynamicConn, "dynamicConnection is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.defaultTemplate = new DalLambdaTemplate(dynamicConn);
        this.spaceTemplateFactory = space -> this.defaultTemplate;

        SqlDialect defaultDialect = this.dalRegistry.getGlobalOptions() != null ? this.dalRegistry.getGlobalOptions().getDefaultDialect() : null;
        if (defaultDialect instanceof PageSqlDialect) {
            this.dialect = (PageSqlDialect) defaultDialect;
        } else {
            this.dialect = this.lambdaTemplate().getJdbc().execute(this::findPageDialect);
        }
    }

    public DalSession(DataSource dataSource, DalRegistry dalRegistry) throws SQLException {
        this.setDataSource(Objects.requireNonNull(dataSource, "dataSource is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.defaultTemplate = new DalLambdaTemplate(dataSource);
        this.spaceTemplateFactory = space -> this.defaultTemplate;

        SqlDialect defaultDialect = this.dalRegistry.getGlobalOptions() != null ? this.dalRegistry.getGlobalOptions().getDefaultDialect() : null;
        if (defaultDialect instanceof PageSqlDialect) {
            this.dialect = (PageSqlDialect) defaultDialect;
        } else {
            this.dialect = this.lambdaTemplate().getJdbc().execute(this::findPageDialect);
        }
    }

    public DalRegistry getDalRegistry() {
        return this.dalRegistry;
    }

    public PageSqlDialect getDialect() {
        return this.dialect;
    }

    public LambdaTemplate lambdaTemplate() {
        return this.defaultTemplate;
    }

    protected LambdaTemplate newTemplate(String space) {
        return this.spaceTemplateFactory.apply(space);
    }

    public <T> BaseMapper<T> createBaseMapper(Class<T> entityType) {
        if (this.dalRegistry.findByEntity(entityType) == null) {
            this.dalRegistry.loadEntityToSpace(entityType);
        }

        BaseMapperHandler mapperHandler = new BaseMapperHandler(null, entityType, this);
        ClassLoader classLoader = this.dalRegistry.getClassLoader();
        InvocationHandler handler = new ExecuteInvocationHandler(this, Mapper.class, this.dalRegistry, mapperHandler);
        return (BaseMapper<T>) Proxy.newProxyInstance(classLoader, new Class[] { Mapper.class, BaseMapper.class }, handler);
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
        if (!isMapper && !this.dalRegistry.hasScope(mapperType)) {
            throw new UnsupportedOperationException("type '" + mapperType.getName() + "' need @RefMapper or @SimpleMapper or @DalMapper");
        }

        if (StringUtils.isNotBlank(resource) /*&& !this.dalRegistry.hasLoaded(resource)*/) {
            try {
                this.dalRegistry.loadMapper(resource);
            } catch (Exception e) {
                log.error("loadMapper '" + resource + "' failed, " + e.getMessage(), e);
            }
        }

        BaseMapperHandler mapperHandler = null;
        if (BaseMapper.class.isAssignableFrom(mapperType)) {
            ResolvableType type = ResolvableType.forClass(mapperType).as(BaseMapper.class);
            Class<?>[] generics = type.resolveGenerics(Object.class);
            Class<?> entityType = generics[0];

            if (this.dalRegistry.findByEntity(entityType) == null) {
                this.dalRegistry.loadEntityToSpace(entityType);
            }

            mapperHandler = new BaseMapperHandler(mapperType.getName(), entityType, this);
        }

        ClassLoader classLoader = this.dalRegistry.getClassLoader();
        InvocationHandler handler = new ExecuteInvocationHandler(this, mapperType, this.dalRegistry, mapperHandler);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { mapperType }, handler);
    }

    public int executeStatement(String stId, Object parameter) throws SQLException {
        return (int) executeStatement(stId, parameter, null);
    }

    public <E> List<E> queryStatement(String stId, Object parameter) throws SQLException {
        return this.queryStatement(stId, parameter, null);
    }

    public <E> List<E> queryStatement(String stId, Object parameter, Page page) throws SQLException {
        Object result = executeStatement(stId, parameter, page);
        if (result instanceof List) {
            return (List<E>) result;
        } else {
            List<E> list = new ArrayList<>();
            list.add((E) result);
            return list;
        }
    }

    private Object executeStatement(String stId, Object parameter, Page page) throws SQLException {
        ExecuteProxy proxy = this.executeCache.computeIfAbsent(stId, s -> {
            String space = "";
            String dynamicId = stId;
            if (stId.contains(".")) {
                int index = stId.lastIndexOf(".");
                space = stId.substring(0, index);
                dynamicId = stId.substring(index + 1);
            }
            RegistryManager context = new DalContext(space, this.dalRegistry);
            return new ExecuteProxy(dynamicId, context);
        });

        return this.lambdaTemplate().getJdbc().execute((ConnectionCallback<Object>) con -> {
            Map<String, Object> mapData = extractData(parameter);
            return proxy.execute(con, mapData, page, false, this.dialect);
        });
    }

    protected Map<String, Object> extractData(Object parameter) {
        final MergedMap<String, Object> mergedMap = new MergedMap<>();
        mergedMap.appendMap(new HashMap<>(), false);

        if (parameter instanceof Map) {
            mergedMap.appendMap((Map<? extends String, ?>) parameter, false);
        } else if (!(parameter instanceof Collection)) {
            BeanMap beanMap = new BeanMap(parameter);
            beanMap.setTransformConvert(ConverterBean.getInstance());
            mergedMap.appendMap(beanMap, true);
        }

        return mergedMap;
    }

    protected PageSqlDialect findPageDialect(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String tmpDbType = JdbcHelper.getDbType(metaData.getURL(), metaData.getDriverName());
        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        return (!(tempDialect instanceof PageSqlDialect)) ? DefaultSqlDialect.DEFAULT : (PageSqlDialect) tempDialect;
    }

    private class DalLambdaTemplate extends LambdaTemplate {
        public DalLambdaTemplate(Connection localConn) {
            super(localConn);
        }

        public DalLambdaTemplate(DynamicConnection dynamicConn) {
            super(dynamicConn);
        }

        public DalLambdaTemplate(DataSource localDS) {
            super(localDS);
        }

        public <T> TableMapping<T> getTableMapping(Class<T> exampleType, MappingOptions options) {
            return dalRegistry.findByEntity(exampleType);
        }
    }
}
