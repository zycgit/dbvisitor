/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mssql.jdbc.call;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.jdbc.call.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlJdbcCallResultTest extends JdbcCallResultCase {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected void createCallFixture() throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 918001");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918001, 'ProcAlice', 25, 'proc-alice@test.com')");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_result_set_users");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_result_set_users @p_id INT AS BEGIN SELECT id, name, age, email FROM user_info WHERE id = @p_id END");
    }

    @Override
    protected String callCommand() {
        return "{call nxn_sp_result_set_users(#{p_id,jdbcType=integer})}";
    }
}
