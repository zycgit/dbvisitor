package net.hasor.dbvisitor.test.realdb.h2.feature.function;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.function.AbstractFunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2FunctionContractTest extends AbstractFunctionContractTest {
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

        String className = H2FunctionContractTest.class.getName();
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_add_numbers FOR '" + className + ".addNumbers'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_multiply FOR '" + className + ".multiply'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_get_username FOR '" + className + ".getUsername'");
        jdbcTemplate.execute("CREATE ALIAS nxn_fn_transform_string FOR '" + className + ".transformString'");
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
}
