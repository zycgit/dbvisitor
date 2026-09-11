/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.h2.feature.function;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.SimpleResultSet;
import net.hasor.dbvisitor.test.contract.feature.function.FunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2FunctionContractTest extends FunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_add_numbers");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_multiply");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_get_username");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_transform_string");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_calc_numbers");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_all_function_rows");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_filter_users");
        jdbcTemplate.execute("DROP ALIAS IF EXISTS nxn_fn_complex_params");

        String className = H2FunctionContractTest.class.getName();
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_add_numbers FOR '" + className + ".addNumbers'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_multiply FOR '" + className + ".multiply'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_get_username FOR '" + className + ".getUsername'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_transform_string FOR '" + className + ".transformString'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_calc_numbers FOR '" + className + ".calcNumbers'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_all_function_rows FOR '" + className + ".allFunctionRows'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_filter_users FOR '" + className + ".filterUsers'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_complex_params FOR '" + className + ".complexParams'");
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
    protected String calcNumbersQuerySql() {
        return "SELECT * FROM nxn_fn_calc_numbers(?, ?)";
    }

    @Override
    protected String multiResultsetsQuerySql() {
        return "SELECT * FROM nxn_fn_all_function_rows()";
    }

    @Override
    protected String filterUsersQuerySql() {
        return "SELECT * FROM nxn_fn_filter_users(?)";
    }

    @Override
    protected String complexParamsQuerySql() {
        return "SELECT * FROM nxn_fn_complex_params(?, ?)";
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

    public static Integer addNumbers(Integer a, Integer b) {
        return a + b;
    }

    public static Integer multiply(Integer x, Integer y) {
        return x * y;
    }

    public static String getUsername(Integer userId) {
        if (Integer.valueOf(918101).equals(userId)) {
            return "FuncAlice";
        }
        if (Integer.valueOf(918102).equals(userId)) {
            return "FuncBob";
        }
        if (Integer.valueOf(918103).equals(userId)) {
            return "FuncCharlie";
        }
        return null;
    }

    public static String transformString(String textValue, String suffix) {
        return textValue.toUpperCase() + suffix;
    }

    public static ResultSet calcNumbers(Integer a, Integer b) throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("sum_result", java.sql.Types.INTEGER, 10, 0);
        rs.addColumn("diff_result", java.sql.Types.INTEGER, 10, 0);
        rs.addColumn("mult_result", java.sql.Types.INTEGER, 10, 0);
        rs.addColumn("div_result", java.sql.Types.DECIMAL, 10, 2);
        if (a == null || b == null) {
            return rs;
        }
        rs.addRow(a + b, a - b, a * b, java.math.BigDecimal.valueOf(a).divide(java.math.BigDecimal.valueOf(b)));
        return rs;
    }

    public static ResultSet allFunctionRows(Connection conn) throws SQLException {
        SimpleResultSet rs = userRowsResultSet();
        try (Statement stat = conn.createStatement();
                ResultSet users = stat.executeQuery("SELECT id, name, age, email FROM user_info WHERE id IN (918101, 918102, 918103) ORDER BY id")) {
            while (users.next()) {
                rs.addRow(users.getInt("id"), users.getString("name"), users.getInt("age"), users.getString("email"));
            }
        }
        try (Statement stat = conn.createStatement();
                ResultSet basics = stat.executeQuery("SELECT id, string_value, int_value FROM basic_types_test WHERE id IN (918101, 918102) ORDER BY id")) {
            while (basics.next()) {
                rs.addRow(basics.getInt("id"), basics.getString("string_value"), basics.getInt("int_value"), null);
            }
        }
        return rs;
    }

    public static ResultSet filterUsers(Connection conn, Integer minAge) throws SQLException {
        SimpleResultSet rs = userRowsResultSet();
        if (minAge == null) {
            return rs;
        }
        try (Statement stat = conn.createStatement();
                ResultSet users = stat.executeQuery("SELECT id, name, age, email FROM user_info WHERE id IN (918101, 918102, 918103) AND age >= " + minAge + " ORDER BY age")) {
            while (users.next()) {
                rs.addRow(users.getInt("id"), users.getString("name"), users.getInt("age"), users.getString("email"));
            }
        }
        return rs;
    }

    public static ResultSet complexParams(Connection conn, Integer userId, Integer seed) throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("counter", java.sql.Types.INTEGER, 10, 0);
        rs.addColumn("user_name", java.sql.Types.VARCHAR, 255, 0);
        rs.addColumn("user_age", java.sql.Types.INTEGER, 10, 0);
        if (userId == null || seed == null) {
            return rs;
        }
        String userName = "Unknown";
        int userAge = 0;
        try (Statement stat = conn.createStatement(); ResultSet user = stat.executeQuery("SELECT name, age FROM user_info WHERE id = " + userId)) {
            if (user.next()) {
                userName = user.getString("name");
                userAge = user.getInt("age");
            }
        }
        rs.addRow(seed + 1, userName, userAge);
        return rs;
    }

    private static SimpleResultSet userRowsResultSet() {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("id", java.sql.Types.INTEGER, 10, 0);
        rs.addColumn("name", java.sql.Types.VARCHAR, 255, 0);
        rs.addColumn("age", java.sql.Types.INTEGER, 10, 0);
        rs.addColumn("email", java.sql.Types.VARCHAR, 255, 0);
        return rs;
    }
}
