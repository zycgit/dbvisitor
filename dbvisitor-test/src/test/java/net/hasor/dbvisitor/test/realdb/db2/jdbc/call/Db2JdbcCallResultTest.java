/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.db2.jdbc.call;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.jdbc.call.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2JdbcCallResultTest extends JdbcCallResultCase {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected void createCallFixture() throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 918001");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918001, 'ProcAlice', 25, 'proc-alice@test.com')");
        try {
            jdbcTemplate.execute("DROP PROCEDURE nxn_sp_result_set_users");
        } catch (SQLException e) {
            if (!"42704".equals(e.getSQLState()) && !"42883".equals(e.getSQLState())) {
                throw e;
            }
        }
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_result_set_users(IN p_id INT)
            DYNAMIC RESULT SETS 1 LANGUAGE SQL
            BEGIN
                DECLARE c1 CURSOR WITH RETURN TO CLIENT FOR SELECT id, name, age, email FROM user_info WHERE id = p_id;
                OPEN c1;
            END
            """);
    }

    @Override
    protected String callCommand() {
        return "CALL nxn_sp_result_set_users(#{p_id,jdbcType=integer})";
    }
}
