/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mssql.feature.procedure;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.procedure.ProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlProcedureContractTest extends ProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected void createProcedureDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_add_numbers");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_calc_numbers");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_transform_string");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_get_user_info");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_update_counter");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS nxn_sp_result_set_users");

        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_add_numbers @a INT, @b INT, @result INT OUTPUT AS " + //
                "BEGIN SET @result = @a + @b END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_calc_numbers
            @a INT, @b INT, @sum_result INT OUTPUT, @diff_result INT OUTPUT, @mult_result INT OUTPUT, @div_result DECIMAL(10, 2) OUTPUT AS
            BEGIN
            SET @sum_result = @a + @b;
            SET @diff_result = @a - @b;
            SET @mult_result = @a * @b;
            SET @div_result = ROUND(CAST(@a AS DECIMAL(10, 2)) / CAST(@b AS DECIMAL(10, 2)), 2);
            END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_transform_string @text_value VARCHAR(255) OUTPUT, @suffix VARCHAR(255) AS
            BEGIN SET @text_value = UPPER(@text_value) + @suffix END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_get_user_info @user_id INT, @user_name VARCHAR(255) OUTPUT, @user_age INT OUTPUT AS
            BEGIN SELECT @user_name = name, @user_age = age FROM user_info WHERE id = @user_id END
            """);
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_update_counter @counter INT OUTPUT, @increment INT AS " + //
                "BEGIN SET @counter = @counter + @increment END");
        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_result_set_users @p_id INT AS " + //
                "BEGIN SELECT id, name, age, email FROM user_info WHERE id = @p_id END");
    }

    @Override
    protected String addNumbersPositionalCallSql() {
        return "{call nxn_sp_add_numbers(?, ?, ?)}";
    }

    @Override
    protected String addNumbersNamedCallSql() {
        return "{call nxn_sp_add_numbers(:a, :b, :result)}";
    }

    @Override
    protected String transformStringHashCallSql() {
        return "{call nxn_sp_transform_string(#{text_value,mode=inout,jdbcType=varchar}, #{suffix,jdbcType=varchar})}";
    }

    @Override
    protected String calcNumbersCallSql() {
        return "{call nxn_sp_calc_numbers(?, ?, ?, ?, ?, ?)}";
    }

    @Override
    protected String getUserInfoNamedCallSql() {
        return "{call nxn_sp_get_user_info(:user_id, :user_name, :user_age)}";
    }

    @Override
    protected String addNumbersJavaTypeHashCallSql() {
        return "{call nxn_sp_add_numbers(#{a,jdbcType=integer}, #{b,jdbcType=integer}, #{result,mode=inout,javaType=java.lang.Integer})}";
    }

    @Override
    protected String addNumbersTypeHandlerHashCallSql() {
        return """
            {call nxn_sp_add_numbers(#{a,jdbcType=integer}, #{b,jdbcType=integer},
            #{result,mode=inout,jdbcType=integer,typeHandler=net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler})}
            """;
    }

    @Override
    protected String updateCounterAliasHashCallSql() {
        return "{call nxn_sp_update_counter(#{counter,name=cnt,mode=inout,jdbcType=integer}, #{increment,jdbcType=integer})}";
    }

    @Override
    protected String addNumbersInferredHashCallSql() {
        return "{call nxn_sp_add_numbers(#{a}, #{b}, #{result,mode=inout,jdbcType=integer})}";
    }

    @Override
    protected String resultSetUsersCallSql() {
        return "{call nxn_sp_result_set_users(#{p_id,jdbcType=integer})}";
    }
}
