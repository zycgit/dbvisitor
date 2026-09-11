/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.oracle.feature.procedure;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.procedure.ProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleProcedureContractTest extends ProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected void createProcedureDefinitions() throws SQLException {
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_add_numbers(a IN NUMBER, b IN NUMBER, result IN OUT NUMBER) AS "
                + "BEGIN result := a + b; END;");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE nxn_sp_calc_numbers(
            a IN NUMBER, b IN NUMBER, sum_result IN OUT NUMBER, diff_result IN OUT NUMBER, mult_result IN OUT NUMBER, div_result IN OUT NUMBER) AS
            BEGIN sum_result := a + b; diff_result := a - b; mult_result := a * b; div_result := ROUND(a / b, 2); END;
            """);
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE nxn_sp_transform_string(text_value IN OUT VARCHAR2, suffix IN VARCHAR2) AS
            BEGIN text_value := UPPER(text_value) || suffix; END;
            """);
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE nxn_sp_get_user_info(user_id IN NUMBER, user_name IN OUT VARCHAR2, user_age IN OUT NUMBER) AS
            BEGIN SELECT name, age INTO user_name, user_age FROM user_info WHERE id = user_id; END;
            """);
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE nxn_sp_update_counter(counter IN OUT NUMBER, increment IN NUMBER) AS "
                + "BEGIN counter := counter + increment; END;");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE nxn_sp_cursor_users(p_name IN VARCHAR2, res OUT SYS_REFCURSOR) AS
            BEGIN OPEN res FOR SELECT id, name, age, email FROM user_info WHERE name = p_name; END;
            """);
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
        return """
            CALL nxn_sp_add_numbers(#{a,jdbcType=integer}, #{b,jdbcType=integer},
            #{result,mode=inout,jdbcType=integer,typeHandler=net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler})
            """;
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
