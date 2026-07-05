package net.hasor.dbvisitor.test.realdb.pg.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.FunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlFunctionContractTest extends FunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_add_numbers CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_multiply CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_calc_numbers CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_multi_resultsets CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_filter_users CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_complex_params CASCADE");

        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_add_numbers(a INT, b INT) " + //
                "RETURNS INT AS $$ BEGIN RETURN a + b; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_multiply(x INT, y INT) " + //
                "RETURNS INT AS $$ BEGIN RETURN x * y; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_get_username(user_id INT) " + //
                "RETURNS VARCHAR AS $$ DECLARE username VARCHAR; BEGIN SELECT name INTO username FROM user_info WHERE id = user_id; RETURN username; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_transform_string(INOUT text_value VARCHAR, IN suffix VARCHAR) " + //
                "AS $$ BEGIN text_value := UPPER(text_value) || suffix; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_calc_numbers(" + //
                "IN a INT, IN b INT, OUT sum_result INT, OUT diff_result INT, OUT mult_result INT, OUT div_result NUMERIC) " + //
                "AS $$ BEGIN sum_result := a + b; diff_result := a - b; mult_result := a * b; div_result := a::NUMERIC / b; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_multi_resultsets() " + //
                "RETURNS TABLE(result_set INT, name VARCHAR, value INT) AS $$ BEGIN " + //
                "RETURN QUERY SELECT 1, u.name::VARCHAR, u.age FROM user_info u WHERE u.id BETWEEN 918101 AND 918103 ORDER BY u.id; " + //
                "RETURN QUERY SELECT 2, b.string_value::VARCHAR, b.int_value FROM basic_types_test b WHERE b.id BETWEEN 918101 AND 918102 ORDER BY b.id; " + //
                "END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_filter_users(IN min_age INT) " + //
                "RETURNS TABLE(id INT, name VARCHAR, age INT) AS $$ BEGIN " + //
                "RETURN QUERY SELECT u.id, u.name, u.age FROM user_info u WHERE u.id BETWEEN 918101 AND 918103 AND u.age >= min_age ORDER BY u.age, u.id; " + //
                "END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_complex_params(IN input_id INT, INOUT counter INT, OUT user_name VARCHAR, OUT user_age INT) " + //
                "AS $$ BEGIN counter := counter + 1; SELECT name, age INTO user_name, user_age FROM user_info WHERE id = input_id; " + //
                "IF user_name IS NULL THEN user_name := 'Unknown'; user_age := 0; END IF; END; $$ LANGUAGE plpgsql");
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
        return "SELECT * FROM nxn_fn_multi_resultsets()";
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
    protected String calcNumbersCallableSql() {
        return "SELECT * FROM nxn_fn_calc_numbers(?, ?)";
    }

    @Override
    protected String getUsernameQuerySql() {
        return "SELECT nxn_fn_get_username(?) AS username";
    }

    @Override
    protected String transformStringQuerySql() {
        return "SELECT * FROM nxn_fn_transform_string(?, ?)";
    }
}
