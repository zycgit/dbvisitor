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
package net.hasor.dbvisitor.spring.adapter;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.transaction.ConnectionProxy;
import net.hasor.dbvisitor.transaction.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
        if (conn instanceof ConnectionProxy) {
            conn.close();
        }
    }
}