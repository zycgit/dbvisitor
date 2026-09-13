/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.pg.api.mapper.xml;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCallableCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlXmlMapperCallableTest extends XmlMapperCallableCase {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected void createCallableDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_insert_user");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_insert_user(IN p_id INT, IN p_name VARCHAR) LANGUAGE plpgsql AS $$ BEGIN INSERT INTO user_info (id, name, age, create_time) VALUES (p_id, p_name, 0, " + currentTimestampExpression() + "); END; $$");

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_double_value");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_double_value(IN p_input INT, INOUT p_result INT) LANGUAGE plpgsql AS $$ BEGIN p_result := p_input * 2; END; $$");

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_multi_inout");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_multi_inout(
            IN p_prefix VARCHAR, IN p_suffix VARCHAR, INOUT p_concat VARCHAR, INOUT p_length INT)
            LANGUAGE plpgsql AS $$ BEGIN
            p_concat := p_prefix || '-' || p_suffix;
            p_length := LENGTH(p_concat);
            END; $$
            """);

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_count");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_user_count(INOUT p_count INT) LANGUAGE plpgsql AS $$ BEGIN SELECT COUNT(*) INTO p_count FROM user_info WHERE name LIKE 'XmlCallable%'; END; $$");

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_stats");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_user_stats(
            INOUT p_count INT, INOUT p_max_id INT, INOUT p_min_name VARCHAR)
            LANGUAGE plpgsql AS $$ BEGIN
            SELECT COUNT(*), MAX(id) INTO p_count, p_max_id FROM user_info WHERE name LIKE 'XmlCallable%';
            SELECT name INTO p_min_name FROM user_info WHERE name LIKE 'XmlCallable%' ORDER BY id LIMIT 1;
            END; $$
            """);

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_query_users");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_query_users(
            IN p_name VARCHAR, INOUT p_out VARCHAR, INOUT res1 refcursor)
            LANGUAGE plpgsql AS $$ BEGIN
            p_out := 'found:' || p_name;
            OPEN res1 FOR SELECT * FROM user_info WHERE name = p_name;
            END; $$
            """);

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_query_users_multi");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_query_users_multi(
            IN p_name VARCHAR, INOUT p_out_msg VARCHAR, INOUT res_matched refcursor, INOUT res_unmatched refcursor)
            LANGUAGE plpgsql AS $$ BEGIN
            p_out_msg := 'query:' || p_name;
            OPEN res_matched FOR SELECT * FROM user_info WHERE name = p_name;
            OPEN res_unmatched FOR SELECT * FROM user_info WHERE name LIKE 'XmlCallableCursor%' AND name <> p_name;
            END; $$
            """);
    }

    @Override
    protected String callableMapperPath() {
        return "/realdb/pg/material/XmlCallableMapper.xml";
    }
}
