/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.dynamic;
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.test.utils.DsUtils;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class H2DynamicConnection implements DynamicConnection {
    @Override
    public Connection getConnection() throws SQLException {
        return DsUtils.h2Conn();
    }

    @Override
    public void releaseConnection(Connection conn) throws SQLException {
        conn.close();
    }
}
