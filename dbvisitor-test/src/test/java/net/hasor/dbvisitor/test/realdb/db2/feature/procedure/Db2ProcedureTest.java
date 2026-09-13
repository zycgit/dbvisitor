/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.db2.feature.procedure;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.procedure.ProcedureCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2ProcedureTest extends ProcedureCase {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected void createProcedureDefinitions() throws SQLException {
        dropProcedure("nxn_sp_add_numbers");
        dropProcedure("nxn_sp_calc_numbers");
        dropProcedure("nxn_sp_transform_string");
        dropProcedure("nxn_sp_get_user_info");
        dropProcedure("nxn_sp_update_counter");
        dropProcedure("nxn_sp_result_set_users");

        jdbcTemplate.execute("CREATE PROCEDURE nxn_sp_add_numbers(IN a INT, IN b INT, INOUT result INT) " + //
                "LANGUAGE SQL BEGIN ATOMIC SET result = a + b; END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_calc_numbers(
            IN a INT, IN b INT, INOUT sum_result INT, INOUT diff_result INT, INOUT mult_result INT, INOUT div_result DECIMAL(10, 2))
            LANGUAGE SQL BEGIN ATOMIC
            SET sum_result = a + b;
            SET diff_result = a - b;
            SET mult_result = a * b;
            SET div_result = ROUND(DECIMAL(a, 10, 2) / DECIMAL(b, 10, 2), 2);
            END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_transform_string(INOUT text_value VARCHAR(255), IN suffix VARCHAR(255))
            LANGUAGE SQL BEGIN ATOMIC SET text_value = UPPER(text_value) || suffix; END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_get_user_info(IN user_id INT, INOUT user_name VARCHAR(255), INOUT user_age INT)
            LANGUAGE SQL BEGIN ATOMIC SELECT name, age INTO user_name, user_age FROM user_info WHERE id = user_id; END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_update_counter(INOUT counter INT, IN increment INT)
            LANGUAGE SQL BEGIN ATOMIC SET counter = counter + increment; END
            """);
        jdbcTemplate.execute("""
            CREATE PROCEDURE nxn_sp_result_set_users(IN p_id INT)
            LANGUAGE SQL DYNAMIC RESULT SETS 1 BEGIN
            DECLARE c1 CURSOR WITH RETURN TO CLIENT FOR SELECT id, name, age, email FROM user_info WHERE id = p_id;
            OPEN c1;
            END
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
    protected String resultSetUsersCallSql() {
        return "CALL nxn_sp_result_set_users(#{p_id,jdbcType=integer})";
    }

    private void dropProcedure(String procedureName) throws SQLException {
        try {
            jdbcTemplate.execute("DROP PROCEDURE " + procedureName);
        } catch (SQLException e) {
            String state = e.getSQLState();
            if (!"42704".equals(state) && !"42883".equals(state)) {
                throw e;
            }
        }
    }
}
