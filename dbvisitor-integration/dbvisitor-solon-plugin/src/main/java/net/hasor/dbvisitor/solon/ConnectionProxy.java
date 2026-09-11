/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.solon;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import org.noear.solon.data.tran.TranUtils;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-03-20
 */
public class ConnectionProxy implements DynamicConnection {
    private final DataSource dataSource;

    public ConnectionProxy(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return TranUtils.getConnectionProxy(dataSource);
    }

    @Override
    public void releaseConnection(Connection conn) throws SQLException {
        conn.close();
    }
}
