package net.hasor.dbvisitor.test.realdb.mssql.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.FunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlFunctionContractTest extends FunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_add_numbers");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_multiply");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_calc_numbers");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_multi_resultsets");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_filter_users");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_complex_params");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_get_username");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_transform_string");

        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_add_numbers(@a INT, @b INT) " + //
                "RETURNS INT AS BEGIN RETURN @a + @b END");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_multiply(@x INT, @y INT) " + //
                "RETURNS INT AS BEGIN RETURN @x * @y END");
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_get_username(@user_id INT)
            RETURNS VARCHAR(255) AS BEGIN
            DECLARE @username VARCHAR(255);
            SELECT @username = name FROM user_info WHERE id = @user_id;
            RETURN @username;
            END
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_transform_string(@text_value VARCHAR(255), @suffix VARCHAR(255))
            RETURNS VARCHAR(255) AS BEGIN RETURN UPPER(@text_value) + @suffix END
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_calc_numbers(@a INT, @b INT) RETURNS TABLE AS RETURN
            SELECT @a + @b AS sum_result, @a - @b AS diff_result, @a * @b AS mult_result,
            CAST(@a AS DECIMAL(10, 2)) / CAST(@b AS DECIMAL(10, 2)) AS div_result
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_multi_resultsets() RETURNS TABLE AS RETURN
            SELECT 1 AS result_set, CAST(name AS VARCHAR(255)) AS name, age AS value FROM user_info WHERE id BETWEEN 918101 AND 918103
            UNION ALL SELECT 2 AS result_set, CAST(string_value AS VARCHAR(255)) AS name, int_value AS value FROM basic_types_test WHERE id BETWEEN 918101 AND 918102
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_filter_users(@min_age INT) RETURNS TABLE AS RETURN
            SELECT id, CAST(name AS VARCHAR(255)) AS name, age FROM user_info WHERE id BETWEEN 918101 AND 918103 AND age >= @min_age
            """);
        jdbcTemplate.execute("""
            CREATE FUNCTION nxn_fn_complex_params(@input_id INT, @counter INT) RETURNS TABLE AS RETURN
            SELECT @counter + 1 AS counter, COALESCE((SELECT name FROM user_info WHERE id = @input_id), 'Unknown') AS user_name,
            COALESCE((SELECT age FROM user_info WHERE id = @input_id), 0) AS user_age
            """);
    }

    @Override
    protected String addNumbersQuerySql() {
        return "SELECT dbo.nxn_fn_add_numbers(?, ?) AS result";
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return "SELECT dbo.nxn_fn_multiply(:x, :y) AS result";
    }

    @Override
    protected String calcNumbersQuerySql() {
        return "SELECT * FROM dbo.nxn_fn_calc_numbers(?, ?)";
    }

    @Override
    protected String multiResultsetsQuerySql() {
        return "SELECT * FROM dbo.nxn_fn_multi_resultsets()";
    }

    @Override
    protected String filterUsersQuerySql() {
        return "SELECT * FROM dbo.nxn_fn_filter_users(?) ORDER BY age, id";
    }

    @Override
    protected String complexParamsQuerySql() {
        return "SELECT * FROM dbo.nxn_fn_complex_params(?, ?)";
    }

    @Override
    protected String addNumbersCallableSql() {
        return "SELECT dbo.nxn_fn_add_numbers(?, ?)";
    }

    @Override
    protected String getUsernameQuerySql() {
        return "SELECT dbo.nxn_fn_get_username(?) AS username";
    }

    @Override
    protected String transformStringQuerySql() {
        return "SELECT dbo.nxn_fn_transform_string(?, ?) AS text_value";
    }
}
