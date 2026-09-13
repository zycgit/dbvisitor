/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.pg.jdbc.call;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.jdbc.call.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJdbcCallResultTest extends JdbcCallResultCase {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected void createCallFixture() throws SQLException {
    }

    @Override
    protected String callCommand() {
        return "SELECT 'ProcAlice' AS name, 25 AS age WHERE #{p_id,jdbcType=integer} = 918001";
    }
}
