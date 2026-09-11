/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.core;
import java.sql.Connection;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.DynamicConnection;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-16
 */
public class JdbcAccessor {
    private DataSource        dataSource;
    private Connection        connection;
    private DynamicConnection dynamic;

    /** Return the DataSource used by this template. */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /** Set the JDBC DataSource to obtain connections from. */
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Return the Connection used by this template. */
    public Connection getConnection() {
        return this.connection;
    }

    /** Set the JDBC Connection to obtain connection from. */
    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    public DynamicConnection getDynamic() {
        return dynamic;
    }

    public void setDynamic(DynamicConnection dynamic) {
        this.dynamic = dynamic;
    }
}
