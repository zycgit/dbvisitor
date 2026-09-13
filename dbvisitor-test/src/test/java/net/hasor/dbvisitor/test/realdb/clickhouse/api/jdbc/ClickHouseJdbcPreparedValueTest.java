/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.api.jdbc;

import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public class ClickHouseJdbcPreparedValueTest extends JdbcPreparedValueCase {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected void createFixture() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute("CREATE TABLE " + this.table + " (id Int64, name Nullable(String)) ENGINE = MergeTree ORDER BY id");
        }
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO " + this.table + " (id, name) VALUES (?, ?)";
    }
}
