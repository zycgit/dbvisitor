/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import java.io.IOException;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.SqlNamedFieldTypeSupport;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public class ClickHouseNamedFieldTypeTest extends SqlNamedFieldTypeSupport {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    public void setup() throws SQLException {
        try {
            dataSource = OneApiDataSourceManager.createDataSource();
        } catch (IOException e) {
            throw new SQLException("Cannot load ClickHouse test connection", e);
        }
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        cleanTestData();
    }
}
