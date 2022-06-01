/*
 * Copyright 2002-2005 the original author or authors.
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
import net.hasor.cobble.convert.ConverterBean;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.execute.ExecuteProxy;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.page.Page;

import javax.sql.DataSource;
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
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class DalSession extends JdbcAccessor {
    private final DalRegistry                      dalRegistry;
    private final PageSqlDialect                   dialect;
    private final LambdaTemplate                   defaultTemplate;
    private final Function<String, LambdaTemplate> spaceTemplateFactory;
    private final Map<String, ExecuteProxy>        executeCache = new ConcurrentHashMap<>();

    public DalSession(Connection connection) throws SQLException {
        this(connection, DalRegistry.DEFAULT, null);
    }

    public DalSession(DataSource dataSource) throws SQLException {
        this(dataSource, DalRegistry.DEFAULT, null);
    }

    public DalSession(DynamicConnection dynamicConnection) throws SQLException {
        this(dynamicConnection, DalRegistry.DEFAULT, null);
    }

    public DalSession(Connection connection, DalRegistry dalRegistry) throws SQLException {
        this(connection, dalRegistry, null);
    }

    public DalSession(DataSource dataSource, DalRegistry dalRegistry) throws SQLException {
        this(dataSource, dalRegistry, null);
    }

    public DalSession(Connection connection, DalRegistry dalRegistry, PageSqlDialect dialect) throws SQLException {
        this.setConnection(Objects.requireNonNull(connection, "connection is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.defaultTemplate = new DalLambdaTemplate(connection, null);
        this.spaceTemplateFactory = space -> new DalLambdaTemplate(connection, space);

        if (dialect == null) {
            this.dialect = this.lambdaTemplate().execute(this::findPageDialect);
        } else {
            this.dialect = dialect;
        }
    }

    public DalSession(DynamicConnection dynamicConn, DalRegistry dalRegistry, PageSqlDialect dialect) throws SQLException {
        this.setDynamic(Objects.requireNonNull(dynamicConn, "dynamicConnection is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.defaultTemplate = new DalLambdaTemplate(dynamicConn, null);
        this.spaceTemplateFactory = space -> new DalLambdaTemplate(dynamicConn, space);

        if (dialect == null) {
            this.dialect = this.lambdaTemplate().execute(this::findPageDialect);
        } else {
            this.dialect = dialect;
        }
    }

    public DalSession(DataSource dataSource, DalRegistry dalRegistry, PageSqlDialect dialect) throws SQLException {
        this.setDataSource(Objects.requireNonNull(dataSource, "dataSource is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.defaultTemplate = new DalLambdaTemplate(dataSource, null);
        this.spaceTemplateFactory = space -> new DalLambdaTemplate(dataSource, space);

        if (dialect == null) {
            this.dialect = this.lambdaTemplate().execute(this::findPageDialect);
        } else {
            this.dialect = dialect;
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
        if (this.dalRegistry.findTableMapping(null, entityType.getName()) == null) {
            this.dalRegistry.loadAsMapping(null, entityType);
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

        BaseMapperHandler mapperHandler = null;
        if (BaseMapper.class.isAssignableFrom(mapperType)) {
            ResolvableType type = ResolvableType.forClass(mapperType).as(BaseMapper.class);
            Class<?>[] generics = type.resolveGenerics(Object.class);
            Class<?> entityType = generics[0];
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
                dynamicId = stId.substring(index);
            }
            DynamicContext context = dalRegistry.createContext(space);
            return new ExecuteProxy(dynamicId, context);
        });

        return this.lambdaTemplate().execute((ConnectionCallback<Object>) con -> {
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
        String tmpDbType = JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        return (!(tempDialect instanceof PageSqlDialect)) ? DefaultSqlDialect.DEFAULT : (PageSqlDialect) tempDialect;
    }

    private class DalLambdaTemplate extends LambdaTemplate {
        private final String space;

        public DalLambdaTemplate(Connection localConn, String space) {
            super(localConn);
            this.space = space;
        }

        public DalLambdaTemplate(DynamicConnection dynamicConn, String space) {
            super(dynamicConn);
            this.space = space;
        }

        public DalLambdaTemplate(DataSource localDS, String space) {
            super(localDS);
            this.space = space;
        }

        protected <T> TableMapping<T> getTableMapping(Class<T> exampleType, MappingOptions options) {
            return dalRegistry.findTableMapping(this.space, exampleType.getName());
        }

        @Override
        protected SqlDialect getDefaultDialect() {
            return dialect;
        }
    }
}
