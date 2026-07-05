package net.hasor.dbvisitor.test.realdb.mysql.feature.procedure;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.procedure.ProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlProcedureContractTest extends ProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }

    @Override
    protected void createProcedureDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_add_numbers");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_calc_numbers");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_transform_string");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_get_user_info");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_update_counter");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_result_set_users");

        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_add_numbers(IN a INT, IN b INT, INOUT result INT) " + //
                "BEGIN SET result = a + b; END");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_calc_numbers(" + //
                "IN a INT, IN b INT, INOUT sum_result INT, INOUT diff_result INT, INOUT mult_result INT, INOUT div_result DECIMAL(10, 2)) " + //
                "BEGIN " + //
                "SET sum_result = a + b; " + //
                "SET diff_result = a - b; " + //
                "SET mult_result = a * b; " + //
                "SET div_result = ROUND(a / b, 2); " + //
                "END");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_transform_string(INOUT text_value VARCHAR(255), IN suffix VARCHAR(255)) " + //
                "BEGIN SET text_value = CONCAT(UPPER(text_value), suffix); END");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_get_user_info(IN user_id INT, INOUT user_name VARCHAR(255), INOUT user_age INT) " + //
                "BEGIN SELECT name, age INTO user_name, user_age FROM user_info WHERE id = user_id; END");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_update_counter(INOUT counter INT, IN increment INT) " + //
                "BEGIN SET counter = counter + increment; END");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_result_set_users(IN p_id INT) " + //
                "BEGIN SELECT id, name, age, email FROM user_info WHERE id = p_id; END");
    }

    @Override
    protected String addNumbersPositionalCallSql() {
        return "CALL nxn_sp_add_numbers(?, ?, ?)";
    }

    @Override
    protected String addNumbersNamedCallSql() {
        return "CALL nxn_sp_add_numbers(:a, :b, :result)";
    }

    @Override
    protected String transformStringHashCallSql() {
        return "CALL nxn_sp_transform_string(#{text_value,mode=inout,jdbcType=varchar}, #{suffix,jdbcType=varchar})";
    }

    @Override
    protected String calcNumbersCallSql() {
        return "CALL nxn_sp_calc_numbers(?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUserInfoNamedCallSql() {
        return "CALL nxn_sp_get_user_info(:user_id, :user_name, :user_age)";
    }

    @Override
    protected String addNumbersJavaTypeHashCallSql() {
        return "CALL nxn_sp_add_numbers(#{a,jdbcType=integer}, #{b,jdbcType=integer}, #{result,mode=inout,javaType=java.lang.Integer})";
    }

    @Override
    protected String addNumbersTypeHandlerHashCallSql() {
        return "CALL nxn_sp_add_numbers(#{a,jdbcType=integer}, #{b,jdbcType=integer}, "
                + "#{result,mode=inout,jdbcType=integer,typeHandler=net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler})";
    }

    @Override
    protected String updateCounterAliasHashCallSql() {
        return "CALL nxn_sp_update_counter(#{counter,name=cnt,mode=inout,jdbcType=integer}, #{increment,jdbcType=integer})";
    }

    @Override
    protected String addNumbersInferredHashCallSql() {
        return "CALL nxn_sp_add_numbers(#{a}, #{b}, #{result,mode=inout,jdbcType=integer})";
    }

    @Override
    protected String resultSetUsersCallSql() {
        return "CALL nxn_sp_result_set_users(#{p_id,jdbcType=integer})";
    }
}
