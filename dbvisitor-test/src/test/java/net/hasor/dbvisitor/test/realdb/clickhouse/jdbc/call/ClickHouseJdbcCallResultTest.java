/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.jdbc.call;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public class ClickHouseJdbcCallResultTest extends JdbcCallResultCase {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected void createCallFixture() throws SQLException {
    }

    @Override
    protected String callCommand() {
        return "SELECT 'ProcAlice' AS name, 25 AS age WHERE #{p_id,jdbcType=integer} = 918001";
    }
}
