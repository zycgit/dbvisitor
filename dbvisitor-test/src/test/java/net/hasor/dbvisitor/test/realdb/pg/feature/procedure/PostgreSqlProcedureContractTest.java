package net.hasor.dbvisitor.test.realdb.pg.feature.procedure;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.procedure.AbstractProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlProcedureContractTest extends AbstractProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected void createProcedureDefinitions() throws SQLException {
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_add_numbers(IN a INT, IN b INT, INOUT result INT) " + //
                "LANGUAGE plpgsql AS $$ BEGIN result := a + b; END $$");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_calc_numbers(" + //
                "IN a INT, IN b INT, INOUT sum_result INT, INOUT diff_result INT, INOUT mult_result INT, INOUT div_result DECIMAL) " + //
                "LANGUAGE plpgsql AS $$ BEGIN " + //
                "sum_result := a + b; diff_result := a - b; mult_result := a * b; div_result := ROUND(a::DECIMAL / b, 2); " + //
                "END $$");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_transform_string(INOUT text_value VARCHAR, IN suffix VARCHAR) " + //
                "LANGUAGE plpgsql AS $$ BEGIN text_value := UPPER(text_value) || suffix; END $$");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_get_user_info(IN user_id INT, INOUT user_name VARCHAR, INOUT user_age INT) " + //
                "LANGUAGE plpgsql AS $$ BEGIN SELECT name, age INTO user_name, user_age FROM user_info WHERE id = user_id; END $$");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_update_counter(INOUT counter INT, IN increment INT) " + //
                "LANGUAGE plpgsql AS $$ BEGIN counter := counter + increment; END $$");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_cursor_users(IN p_name VARCHAR, INOUT p_cursor refcursor) " + //
                "LANGUAGE plpgsql AS $$ BEGIN OPEN p_cursor FOR SELECT id, name, age, email FROM user_info WHERE name = p_name; END $$");
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
    protected String cursorUsersCallSql() {
        return "CALL nxn_sp_cursor_users(#{p_name,jdbcType=varchar}, "
                + "#{res,mode=cursor,javaType=net.hasor.dbvisitor.test.contract.material.model.UserInfo})";
    }
}
