package net.hasor.dbvisitor.test.realdb.clickhouse.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.AbstractFunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseFunctionContractTest extends AbstractFunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_add_numbers");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_multiply");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_get_username");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS nxn_fn_transform_string");

        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_add_numbers AS (a, b) -> a + b");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_multiply AS (x, y) -> x * y");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_get_username AS user_id -> multiIf(user_id = 918101, 'FuncAlice', user_id = 918102, 'FuncBob', user_id = 918103, 'FuncCharlie', NULL)");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_transform_string AS (text_value, suffix) -> concat(upper(text_value), suffix)");
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
