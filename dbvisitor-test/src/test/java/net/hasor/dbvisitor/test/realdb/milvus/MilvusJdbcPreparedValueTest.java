/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

public class MilvusJdbcPreparedValueTest extends JdbcPreparedValueCase {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    protected void createFixture() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute("CREATE TABLE " + this.table + " (id INT64 PRIMARY KEY, name VARCHAR(512) NULL, v FLOAT_VECTOR(2)) WITH (consistency_level='Strong')");
            statement.execute("CREATE INDEX bound_v ON " + this.table + "(v) USING FLAT WITH (metric_type=L2)");
            statement.execute("LOAD TABLE " + this.table);
        }
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO " + this.table + " (id, name, v) VALUES (?, ?, [1, 0])";
    }
}
