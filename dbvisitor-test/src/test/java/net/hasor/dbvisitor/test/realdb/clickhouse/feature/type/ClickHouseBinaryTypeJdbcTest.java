/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.BinaryTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.After;

public class ClickHouseBinaryTypeJdbcTest extends BinaryTypeJdbcCase {
    private Connection binaryConnection;

    @Override
    public void setup() throws IOException, SQLException {
        super.setup();
        Properties properties = new Properties();
        properties.setProperty("use_binary_string", "true");
        this.binaryConnection = OneApiDataSourceManager.getConnection("clickhouse", properties);
        this.jdbcTemplate = new JdbcTemplate(this.binaryConnection);
    }

    @After
    public void closeBinaryConnection() throws SQLException {
        if (this.binaryConnection != null) {
            this.binaryConnection.close();
        }
    }

    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
