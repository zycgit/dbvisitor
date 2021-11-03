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
package net.hasor.db.dal.session;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.resolvable.ResolvableType;
import net.hasor.db.JdbcUtils;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dialect.DefaultSqlDialect;
import net.hasor.db.dialect.PageSqlDialect;
import net.hasor.db.dialect.SqlDialect;
import net.hasor.db.dialect.SqlDialectRegister;
import net.hasor.db.jdbc.core.JdbcAccessor;
import net.hasor.db.lambda.core.LambdaTemplate;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.mapping.resolve.MappingOptions;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 基础数据库操作接口
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class DalSession extends JdbcAccessor {
    private final DalRegistry    dalRegistry;
    private       PageSqlDialect dialect;
    private       LambdaTemplate lambdaTemplate;

    public DalSession(Connection connection, DalRegistry dalRegistry) {
        this(connection, dalRegistry, null);
    }

    public DalSession(Connection connection, DalRegistry dalRegistry, PageSqlDialect dialect) {
        this.setConnection(Objects.requireNonNull(connection, "connection is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.dialect = dialect;
    }

    public DalSession(DataSource dataSource, DalRegistry dalRegistry) {
        this(dataSource, dalRegistry, null);
    }

    public DalSession(DataSource dataSource, DalRegistry dalRegistry, PageSqlDialect dialect) {
        this.setDataSource(Objects.requireNonNull(dataSource, "dataSource is null."));
        this.dalRegistry = Objects.requireNonNull(dalRegistry, "dalRegistry is null.");
        this.dialect = dialect;
    }

    public DalRegistry getDalRegistry() {
        return this.dalRegistry;
    }

    public PageSqlDialect getDialect() {
        if (this.dialect != null) {
            return this.dialect;
        }
        synchronized (this) {
            if (this.dialect != null) {
                return this.dialect;
            }
            try {
                this.dialect = this.lambdaTemplate().execute(this::findPageDialect);
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }
        return this.dialect;
    }

    public LambdaTemplate lambdaTemplate() {
        if (this.lambdaTemplate == null) {
            Connection localConn = this.getConnection();
            DataSource localDS = this.getDataSource();//获取数据源

            if (localConn != null) {
                this.lambdaTemplate = new DalLambdaTemplate(localConn);
            } else if (localDS != null) {
                this.lambdaTemplate = new DalLambdaTemplate(localDS);
            } else {
                this.lambdaTemplate = new DalLambdaTemplate();
            }

            this.lambdaTemplate.setAccessorApply(this.getAccessorApply());
        }

        return this.lambdaTemplate;
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

    private PageSqlDialect findPageDialect(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String tmpDbType = JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        return (!(tempDialect instanceof PageSqlDialect)) ? DefaultSqlDialect.DEFAULT : (PageSqlDialect) tempDialect;
    }

    private class DalLambdaTemplate extends LambdaTemplate {
        public DalLambdaTemplate() {
        }

        public DalLambdaTemplate(Connection localConn) {
            super(localConn);
        }

        public DalLambdaTemplate(DataSource localDS) {
            super(localDS);
        }

        protected <T> TableMapping<T> getTableMapping(Class<T> exampleType, MappingOptions options) {
            return dalRegistry.findTableMapping(null, exampleType);
        }

        @Override
        protected SqlDialect getDefaultDialect() {
            return getDialect();
        }
    }
}
