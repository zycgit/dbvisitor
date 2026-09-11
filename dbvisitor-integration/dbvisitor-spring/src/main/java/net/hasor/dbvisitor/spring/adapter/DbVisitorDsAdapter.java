/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.spring.adapter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.transaction.ConnectionProxy;
import net.hasor.dbvisitor.transaction.DataSourceUtils;

/**
 * <pre class="code">
 * {@code
 *     <bean id="session" class="net.hasor.dbvisitor.spring.support.SessionBean">
 *         ...
 *         <property name="dsAdapterClass" value="net.hasor.dbvisitor.spring.adapter.DbVisitorDsAdapter"/>
 *         ...
 *     </bean>
 *     <bean id="oneMapper" class="net.hasor.dbvisitor.spring.support.MapperBean">
 *         <property name="session" ref="session"/>
 *         <property name="mapperInterface" value="net.hasor.dbvisitor.test.TestUserDAO"/>
 *     </bean>
 * }
 * </pre>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see Mapper
 */
public class DbVisitorDsAdapter extends AbstractDsAdapter {
    public DbVisitorDsAdapter() {
    }

    public DbVisitorDsAdapter(DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection(this.getDataSource());
    }

    @Override
    public void releaseConnection(Connection conn) throws SQLException {
        if (conn instanceof ConnectionProxy connectionProxy) {
            connectionProxy.close();
        }
    }
}
