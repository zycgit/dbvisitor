/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.oracle.api.mapper.xml;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCallableCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleXmlMapperCallableTest extends XmlMapperCallableCase {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected void createCallableDefinitions() throws SQLException {
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_insert_user(p_id IN NUMBER, p_name IN VARCHAR2) AS
            BEGIN INSERT INTO user_info (id, name, age, create_time) VALUES (p_id, p_name, 0,
            """ + currentTimestampExpression() + "); END;");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_double_value(p_input IN NUMBER, p_result IN OUT NUMBER) AS " + //
                "BEGIN p_result := p_input * 2; END;");
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_multi_inout(p_prefix IN VARCHAR2, p_suffix IN VARCHAR2, p_concat IN OUT VARCHAR2, p_length IN OUT NUMBER) AS
            BEGIN p_concat := p_prefix || '-' || p_suffix; p_length := LENGTH(p_concat); END;
            """);
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_user_count(p_count IN OUT NUMBER) AS
            BEGIN SELECT COUNT(*) INTO p_count FROM user_info WHERE name LIKE 'XmlCallable%'; END;
            """);
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_user_stats(p_count IN OUT NUMBER, p_max_id IN OUT NUMBER, p_min_name IN OUT VARCHAR2) AS
            BEGIN
            SELECT COUNT(*), MAX(id) INTO p_count, p_max_id FROM user_info WHERE name LIKE 'XmlCallable%';
            SELECT name INTO p_min_name FROM (SELECT name FROM user_info WHERE name LIKE 'XmlCallable%' ORDER BY id) WHERE ROWNUM = 1;
            END;
            """);
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_query_users(p_name IN VARCHAR2, p_out IN OUT VARCHAR2, res1 OUT SYS_REFCURSOR) AS
            BEGIN p_out := 'found:' || p_name; OPEN res1 FOR SELECT id, name, age, email FROM user_info WHERE name = p_name; END;
            """);
        jdbcTemplate.execute("""
            CREATE OR REPLACE PROCEDURE proc_query_users_multi(p_name IN VARCHAR2, p_out_msg IN OUT VARCHAR2, res_matched OUT SYS_REFCURSOR, res_unmatched OUT SYS_REFCURSOR) AS
            BEGIN
            p_out_msg := 'query:' || p_name;
            OPEN res_matched FOR SELECT id, name, age, email FROM user_info WHERE name = p_name;
            OPEN res_unmatched FOR SELECT id, name, age, email FROM user_info WHERE name LIKE 'XmlCallableCursor%' AND name <> p_name;
            END;
            """);
    }

    @Override
    protected String callableMapperPath() {
        return "/realdb/oracle/material/XmlCallableMapper.xml";
    }
}
