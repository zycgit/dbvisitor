package net.hasor.dbvisitor.test.realdb.db2.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.FunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2FunctionContractTest extends FunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        dropFunction("nxn_fn_add_numbers");
        dropFunction("nxn_fn_multiply");
        dropFunction("nxn_fn_calc_numbers");
        dropFunction("nxn_fn_multi_resultsets");
        dropFunction("nxn_fn_filter_users");
        dropFunction("nxn_fn_complex_params");
        dropFunction("nxn_fn_get_username");
        dropFunction("nxn_fn_transform_string");

        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_add_numbers(a INT, b INT) " + //
                "RETURNS INT LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN a + b");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_multiply(x INT, y INT) " + //
                "RETURNS INT LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN x * y");
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_get_username(user_id INT)
            RETURNS VARCHAR(255) LANGUAGE SQL READS SQL DATA NO EXTERNAL ACTION
            RETURN SELECT name FROM user_info WHERE id = user_id
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_transform_string(text_value VARCHAR(255), suffix VARCHAR(255))
            RETURNS VARCHAR(255) LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN UPPER(text_value) || suffix
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_calc_numbers(a INT, b INT)
            RETURNS TABLE(sum_result INT, diff_result INT, mult_result INT, div_result DECIMAL(10, 2))
            LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN
            SELECT a + b, a - b, a * b, DECIMAL(a, 10, 2) / DECIMAL(b, 10, 2) FROM SYSIBM.SYSDUMMY1
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_multi_resultsets()
            RETURNS TABLE(result_set INT, name VARCHAR(255), value INT)
            LANGUAGE SQL READS SQL DATA NO EXTERNAL ACTION RETURN
            SELECT 1, CAST(name AS VARCHAR(255)), age FROM user_info WHERE id BETWEEN 918101 AND 918103
            UNION ALL SELECT 2, CAST(string_value AS VARCHAR(255)), int_value FROM basic_types_test WHERE id BETWEEN 918101 AND 918102
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_filter_users(min_age INT)
            RETURNS TABLE(id INT, name VARCHAR(255), age INT)
            LANGUAGE SQL READS SQL DATA NO EXTERNAL ACTION RETURN
            SELECT id, CAST(name AS VARCHAR(255)), age FROM user_info WHERE id BETWEEN 918101 AND 918103 AND age >= min_age
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_complex_params(input_id INT, counter INT)
            RETURNS TABLE(counter INT, user_name VARCHAR(255), user_age INT)
            LANGUAGE SQL READS SQL DATA NO EXTERNAL ACTION RETURN
            SELECT counter + 1, COALESCE((SELECT name FROM user_info WHERE id = input_id), 'Unknown'),
            COALESCE((SELECT age FROM user_info WHERE id = input_id), 0) FROM SYSIBM.SYSDUMMY1
            """);
    }

    @Override
    protected String addNumbersQuerySql() {
        return "SELECT nxn_fn_add_numbers(?, ?) AS result FROM SYSIBM.SYSDUMMY1";
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return "SELECT nxn_fn_multiply(:x, :y) AS result FROM SYSIBM.SYSDUMMY1";
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
        return "SELECT * FROM TABLE(nxn_fn_filter_users(?)) ORDER BY age, id";
    }

    @Override
    protected String complexParamsQuerySql() {
        return "SELECT * FROM TABLE(nxn_fn_complex_params(?, ?))";
    }

    @Override
    protected String addNumbersCallableSql() {
        return "SELECT nxn_fn_add_numbers(?, ?) FROM SYSIBM.SYSDUMMY1";
    }

    @Override
    protected String getUsernameQuerySql() {
        return "SELECT nxn_fn_get_username(?) AS username FROM SYSIBM.SYSDUMMY1";
    }

    @Override
    protected String transformStringQuerySql() {
        return "SELECT nxn_fn_transform_string(?, ?) AS text_value FROM SYSIBM.SYSDUMMY1";
    }

    private void dropFunction(String functionName) throws SQLException {
        try {
            jdbcTemplate.execute("DROP FUNCTION " + functionName);
        } catch (SQLException e) {
            String state = e.getSQLState();
            if (!"42704".equals(state) && !"42883".equals(state)) {
                throw e;
            }
        }
    }
}
