/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.db2.api.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2JdbcPreparedValueTest extends JdbcPreparedValueCase {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected void createFixture() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute("CREATE TABLE " + this.table + " (id BIGINT NOT NULL PRIMARY KEY, name VARCHAR(2048))");
        }
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO " + this.table + " (id, name) VALUES (?, ?)";
    }

    @Override
    public void cleanupValues() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection; Statement statement = closing.createStatement()) {
                statement.execute("DROP TABLE " + this.table);
            }
        }
    }
}
