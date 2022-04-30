/*
 * Copyright 2010-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.db.spring.support;
import net.hasor.db.dal.session.Mapper;
import net.hasor.db.transaction.ConnectionProxy;
import net.hasor.db.transaction.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <pre class="code">
 * {@code
 *     <bean id="dalSession" class="net.hasor.db.spring.support.DalSessionBean">
 *         ...
 *         <property name="dsAdapterClass" value="net.hasor.db.spring.support.HasordbDsAdapter"/>
 *         ...
 *     </bean>
 *
 *     <bean id="oneMapper" class="net.hasor.db.spring.support.DalMapperBean">
 *         <property name="dalSession" ref="dalSession"/>
 *         <property name="mapperInterface" value="net.hasor.db.test.TestUserDAO"/>
 *     </bean>
 * }
 * </pre>
 *
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
public class HasordbDsAdapter extends AbstractDsAdapter {
    public HasordbDsAdapter() {
    }

    public HasordbDsAdapter(DataSource dataSource) {
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