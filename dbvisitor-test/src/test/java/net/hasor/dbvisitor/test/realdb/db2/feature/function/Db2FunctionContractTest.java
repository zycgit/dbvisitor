package net.hasor.dbvisitor.test.realdb.db2.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.AbstractFunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2FunctionContractTest extends AbstractFunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected void createFunctionDefinitions() throws SQLException {
        dropFunction("nxn_fn_add_numbers");
        dropFunction("nxn_fn_multiply");
        dropFunction("nxn_fn_get_username");
        dropFunction("nxn_fn_transform_string");

        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_add_numbers(a INT, b INT) " + //
                "RETURNS INT LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN a + b");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_multiply(x INT, y INT) " + //
                "RETURNS INT LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN x * y");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_get_username(user_id INT) " + //
                "RETURNS VARCHAR(255) LANGUAGE SQL READS SQL DATA NO EXTERNAL ACTION " + //
                "RETURN SELECT name FROM user_info WHERE id = user_id");
        jdbcTemplate.execute("CREATE FUNCTION nxn_fn_transform_string(text_value VARCHAR(255), suffix VARCHAR(255)) " + //
                "RETURNS VARCHAR(255) LANGUAGE SQL DETERMINISTIC NO EXTERNAL ACTION RETURN UPPER(text_value) || suffix");
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
