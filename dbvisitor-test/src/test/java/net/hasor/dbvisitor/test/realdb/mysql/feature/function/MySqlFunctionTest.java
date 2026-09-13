/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mysql.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.FunctionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlFunctionTest extends FunctionCase {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_add_numbers");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_multiply");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_get_username");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_transform_string");

        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_add_numbers(a INT, b INT) " + //
                "RETURNS INT DETERMINISTIC RETURN a + b");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_multiply(x INT, y INT) " + //
                "RETURNS INT DETERMINISTIC RETURN x * y");
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_get_username(user_id INT)
            RETURNS VARCHAR(255) READS SQL DATA BEGIN
            DECLARE username VARCHAR(255);
            SELECT name INTO username FROM user_info WHERE id = user_id;
            RETURN username;
            END
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_transform_string(text_value VARCHAR(255), suffix VARCHAR(255))
            RETURNS VARCHAR(255) DETERMINISTIC RETURN CONCAT(UPPER(text_value), suffix)
            """);
    }

    @Override
    protected String addNumbersQuerySql() {
        return "SELECT nxn_fn_add_numbers(?, ?) AS result";
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return "SELECT nxn_fn_multiply(:x, :y) AS result";
    }

    @Override
    protected String addNumbersCallableSql() {
        return "SELECT nxn_fn_add_numbers(?, ?)";
    }

    @Override
    protected String getUsernameQuerySql() {
        return "SELECT nxn_fn_get_username(?) AS username";
    }

    @Override
    protected String transformStringQuerySql() {
        return "SELECT nxn_fn_transform_string(?, ?) AS text_value";
    }
}
