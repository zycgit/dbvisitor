package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.xml;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCallableContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlXmlMapperCallableContractTest extends XmlMapperCallableContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }

    @Override
    protected void createCallableDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_insert_user");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_double_value");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_multi_inout");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_count");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_stats");

        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_insert_user(IN p_id INT, IN p_name VARCHAR(255))
            BEGIN INSERT INTO user_info (id, name, age, create_time) VALUES (p_id, p_name, 0,
            """ + currentTimestampExpression() + "); END");
        jdbcTemplate.execute("CREATE PROCEDURE proc_double_value(IN p_input INT, INOUT p_result INT) " + //
                "BEGIN SET p_result = p_input * 2; END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_multi_inout(IN p_prefix VARCHAR(255), IN p_suffix VARCHAR(255), INOUT p_concat VARCHAR(255), INOUT p_length INT)
            BEGIN SET p_concat = CONCAT(p_prefix, '-', p_suffix); SET p_length = CHAR_LENGTH(p_concat); END
            """);
        jdbcTemplate.execute("CREATE PROCEDURE proc_user_count(INOUT p_count INT) " + //
                "BEGIN SELECT COUNT(*) INTO p_count FROM user_info WHERE name LIKE 'XmlCallable%'; END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_user_stats(INOUT p_count INT, INOUT p_max_id INT, INOUT p_min_name VARCHAR(255))
            BEGIN
            SELECT COUNT(*), MAX(id) INTO p_count, p_max_id FROM user_info WHERE name LIKE 'XmlCallable%';
            SELECT name INTO p_min_name FROM user_info WHERE name LIKE 'XmlCallable%' ORDER BY id LIMIT 1;
            END
            """);
    }

    @Override
    protected String callableMapperPath() {
        return "/realdb/mysql/material/XmlCallableMapper.xml";
    }
}
