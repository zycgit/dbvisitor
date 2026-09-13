/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.db2.api.mapper.xml;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCallableCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2XmlMapperCallableTest extends XmlMapperCallableCase {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected void createCallableDefinitions() throws SQLException {
        dropProcedure("proc_insert_user");
        dropProcedure("proc_double_value");
        dropProcedure("proc_multi_inout");
        dropProcedure("proc_user_count");
        dropProcedure("proc_user_stats");

        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_insert_user(IN p_id INT, IN p_name VARCHAR(255))
            LANGUAGE SQL BEGIN ATOMIC INSERT INTO user_info (id, name, age, create_time) VALUES (p_id, p_name, 0,
            """ + currentTimestampExpression() + "); END");
        jdbcTemplate.execute("CREATE PROCEDURE proc_double_value(IN p_input INT, INOUT p_result INT) " + //
                "LANGUAGE SQL BEGIN ATOMIC SET p_result = p_input * 2; END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_multi_inout(IN p_prefix VARCHAR(255), IN p_suffix VARCHAR(255), INOUT p_concat VARCHAR(255), INOUT p_length INT)
            LANGUAGE SQL BEGIN ATOMIC SET p_concat = p_prefix || '-' || p_suffix; SET p_length = LENGTH(p_concat); END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_user_count(INOUT p_count INT)
            LANGUAGE SQL BEGIN ATOMIC SELECT COUNT(*) INTO p_count FROM user_info WHERE name LIKE 'XmlCallable%'; END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_user_stats(INOUT p_count INT, INOUT p_max_id INT, INOUT p_min_name VARCHAR(255))
            LANGUAGE SQL BEGIN ATOMIC
            SELECT COUNT(*), MAX(id) INTO p_count, p_max_id FROM user_info WHERE name LIKE 'XmlCallable%';
            SELECT name INTO p_min_name FROM user_info WHERE name LIKE 'XmlCallable%' ORDER BY id FETCH FIRST 1 ROW ONLY;
            END
            """);
    }

    @Override
    protected String callableMapperPath() {
        return "/realdb/db2/material/XmlCallableMapper.xml";
    }

    private void dropProcedure(String procedureName) throws SQLException {
        try {
            jdbcTemplate.execute("DROP PROCEDURE " + procedureName);
        } catch (SQLException e) {
            String state = e.getSQLState();
            if (!"42704".equals(state) && !"42883".equals(state)) {
                throw e;
            }
        }
    }
}
