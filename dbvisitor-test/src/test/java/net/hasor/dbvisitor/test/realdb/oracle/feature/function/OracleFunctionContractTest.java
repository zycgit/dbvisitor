package net.hasor.dbvisitor.test.realdb.oracle.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.AbstractFunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleFunctionContractTest extends AbstractFunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_add_numbers(a IN NUMBER, b IN NUMBER) RETURN NUMBER AS "
                + "BEGIN RETURN a + b; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_multiply(x IN NUMBER, y IN NUMBER) RETURN NUMBER AS "
                + "BEGIN RETURN x * y; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_get_username(user_id IN NUMBER) RETURN VARCHAR2 AS "
                + "username VARCHAR2(255); BEGIN SELECT name INTO username FROM user_info WHERE id = user_id; RETURN username; END;");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION nxn_fn_transform_string(text_value IN VARCHAR2, suffix IN VARCHAR2) RETURN VARCHAR2 AS "
                + "BEGIN RETURN UPPER(text_value) || suffix; END;");
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
}
