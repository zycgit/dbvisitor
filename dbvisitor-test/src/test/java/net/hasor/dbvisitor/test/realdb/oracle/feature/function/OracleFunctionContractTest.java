package net.hasor.dbvisitor.test.realdb.oracle.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.FunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleFunctionContractTest extends FunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        dropFunction("nxn_fn_add_numbers");
        dropFunction("nxn_fn_multiply");
        dropFunction("nxn_fn_get_username");
        dropFunction("nxn_fn_transform_string");
        dropFunction("nxn_fn_calc_numbers");
        dropFunction("nxn_fn_multi_resultsets");
        dropFunction("nxn_fn_filter_users");
        dropFunction("nxn_fn_complex_params");

        dropTypeForce("nxn_fn_calc_table");
        dropTypeForce("nxn_fn_calc_row");
        dropTypeForce("nxn_fn_multi_table");
        dropTypeForce("nxn_fn_multi_row");
        dropTypeForce("nxn_fn_user_table");
        dropTypeForce("nxn_fn_user_row");
        dropTypeForce("nxn_fn_complex_table");
        dropTypeForce("nxn_fn_complex_row");

        jdbcTemplate.execute("CREATE TYPE nxn_fn_calc_row AS OBJECT (sum_result NUMBER(10), diff_result NUMBER(10), mult_result NUMBER(10), div_result NUMBER(10, 2))");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_calc_table AS TABLE OF nxn_fn_calc_row");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_multi_row AS OBJECT (result_set NUMBER(10), name VARCHAR2(255), value NUMBER(10))");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_multi_table AS TABLE OF nxn_fn_multi_row");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_user_row AS OBJECT (id NUMBER(10), name VARCHAR2(255), age NUMBER(10))");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_user_table AS TABLE OF nxn_fn_user_row");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_complex_row AS OBJECT (counter NUMBER(10), user_name VARCHAR2(255), user_age NUMBER(10))");
        jdbcTemplate.execute("CREATE TYPE nxn_fn_complex_table AS TABLE OF nxn_fn_complex_row");

        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_add_numbers(a IN NUMBER, b IN NUMBER) RETURN NUMBER AS "
                + "BEGIN RETURN a + b; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_multiply(x IN NUMBER, y IN NUMBER) RETURN NUMBER AS "
                + "BEGIN RETURN x * y; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_get_username(user_id IN NUMBER) RETURN VARCHAR2 AS "
                + "username VARCHAR2(255); BEGIN SELECT name INTO username FROM user_info WHERE id = user_id; RETURN username; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_transform_string(text_value IN VARCHAR2, suffix IN VARCHAR2) RETURN VARCHAR2 AS "
                + "BEGIN RETURN UPPER(text_value) || suffix; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_calc_numbers(a IN NUMBER, b IN NUMBER) RETURN nxn_fn_calc_table AS "
                + "BEGIN RETURN nxn_fn_calc_table(nxn_fn_calc_row(a + b, a - b, a * b, a / b)); END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_multi_resultsets RETURN nxn_fn_multi_table AS "
                + "rows nxn_fn_multi_table; BEGIN "
                + "SELECT nxn_fn_multi_row(result_set, name, value) BULK COLLECT INTO rows FROM ("
                + "SELECT 1 AS result_set, CAST(name AS VARCHAR2(255)) AS name, age AS value FROM user_info WHERE id BETWEEN 918101 AND 918103 "
                + "UNION ALL SELECT 2 AS result_set, CAST(string_value AS VARCHAR2(255)) AS name, int_value AS value FROM basic_types_test WHERE id BETWEEN 918101 AND 918102"
                + "); RETURN rows; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_filter_users(min_age IN NUMBER) RETURN nxn_fn_user_table AS "
                + "rows nxn_fn_user_table; BEGIN "
                + "SELECT nxn_fn_user_row(id, CAST(name AS VARCHAR2(255)), age) BULK COLLECT INTO rows "
                + "FROM user_info WHERE id BETWEEN 918101 AND 918103 AND age >= min_age ORDER BY age, id; "
                + "RETURN rows; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_complex_params(input_id IN NUMBER, counter IN NUMBER) RETURN nxn_fn_complex_table AS "
                + "user_name VARCHAR2(255); user_age NUMBER(10); BEGIN "
                + "BEGIN SELECT name, age INTO user_name, user_age FROM user_info WHERE id = input_id; "
                + "EXCEPTION WHEN NO_DATA_FOUND THEN user_name := 'Unknown'; user_age := 0; END; "
                + "RETURN nxn_fn_complex_table(nxn_fn_complex_row(counter + 1, user_name, user_age)); END;");
    }

    @Override
    protected String addNumbersQuerySql() {
        return "SELECT nxn_fn_add_numbers(?, ?) AS result FROM dual";
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return "SELECT nxn_fn_multiply(:x, :y) AS result FROM dual";
    }

    @Override
    protected String calcNumbersQuerySql() {
        return "SELECT * FROM TABLE(nxn_fn_calc_numbers(?, ?))";
    }

    @Override
    protected String multiResultsetsQuerySql() {
        return "SELECT * FROM TABLE(nxn_fn_multi_resultsets())";
    }

    @Override
    protected String filterUsersQuerySql() {
        return "SELECT * FROM TABLE(nxn_fn_filter_users(?))";
    }

    @Override
    protected String complexParamsQuerySql() {
        return "SELECT * FROM TABLE(nxn_fn_complex_params(?, ?))";
    }

    @Override
    protected String addNumbersCallableSql() {
        return "SELECT nxn_fn_add_numbers(?, ?) FROM dual";
    }

    @Override
    protected String getUsernameQuerySql() {
        return "SELECT nxn_fn_get_username(?) AS username FROM dual";
    }

    @Override
    protected String transformStringQuerySql() {
        return "SELECT nxn_fn_transform_string(?, ?) AS text_value FROM dual";
    }

    private void dropFunction(String functionName) throws SQLException {
        try {
            jdbcTemplate.execute("DROP FUNCTION " + functionName);
        } catch (SQLException e) {
            if (e.getErrorCode() != 4043) {
                throw e;
            }
        }
    }

    private void dropTypeForce(String typeName) throws SQLException {
        try {
            jdbcTemplate.execute("DROP TYPE " + typeName + " FORCE");
        } catch (SQLException e) {
            if (e.getErrorCode() != 4043) {
                throw e;
            }
        }
    }
}
