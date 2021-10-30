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
import net.hasor.db.JdbcUtils;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dialect.DefaultSqlDialect;
import net.hasor.db.dialect.PageSqlDialect;
import net.hasor.db.dialect.SqlDialect;
import net.hasor.db.dialect.SqlDialectRegister;
import net.hasor.db.jdbc.ConnectionCallback;
import net.hasor.db.jdbc.core.JdbcAccessor;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.lambda.core.LambdaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger         logger = LoggerFactory.getLogger(DalSession.class);
    private final        DalRegistry    dalRegistry;
    private              PageSqlDialect dialect;

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
                this.dialect = execute(this::findPageDialect);
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }
        return this.dialect;
    }

    public JdbcTemplate jdbcTemplate() {
        Connection localConn = this.getConnection();
        if (localConn != null) {
            return new JdbcTemplate(localConn);
        }

        DataSource localDS = this.getDataSource();//获取数据源
        if (localDS != null) {
            return new JdbcTemplate(localDS);
        }

        return new JdbcTemplate();
    }

    public LambdaTemplate lambdaTemplate() {
        Connection localConn = this.getConnection();
        if (localConn != null) {
            return new LambdaTemplate(localConn);
        }

        DataSource localDS = this.getDataSource();//获取数据源
        if (localDS != null) {
            return new LambdaTemplate(localDS);
        }

        return new LambdaTemplate();
    }

    public <T> T createMapper(Class<T> mapperType) {
        ClassLoader classLoader = this.dalRegistry.getClassLoader();
        InvocationHandler handler = new ExecuteInvocationHandler(this, mapperType, this.dalRegistry);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { mapperType }, handler);
    }

    public <R> R execute(final ConnectionCallback<R> callback) throws SQLException {
        Connection localConn = this.getConnection();
        DataSource localDS = this.getDataSource();//获取数据源
        boolean usingDS = (localConn == null);
        if (logger.isDebugEnabled()) {
            logger.debug("database connection using DataSource = {}", usingDS);
        }
        if (localConn == null && localDS == null) {
            throw new IllegalArgumentException("DataSource or Connection are not available.");
        }

        Connection useConn = null;
        try {
            if (usingDS) {
                useConn = applyConnection(localDS);
            } else {
                useConn = localConn;
            }
            return callback.doInConnection(useConn);
        } finally {
            if (usingDS && useConn != null) {
                useConn.close();
            }
        }
    }

    private PageSqlDialect findPageDialect(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String tmpDbType = JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        return (!(tempDialect instanceof PageSqlDialect)) ? DefaultSqlDialect.DEFAULT : (PageSqlDialect) tempDialect;
    }
}
