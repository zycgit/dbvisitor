package net.hasor.dbvisitor.test.realdb.mssql.api.mapper.xml;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCallableContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlXmlMapperCallableContractTest extends XmlMapperCallableContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected void createCallableDefinitions() throws SQLException {
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_insert_user");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_double_value");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_multi_inout");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_count");
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_stats");

        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_insert_user @p_id INT, @p_name VARCHAR(255) AS
            BEGIN INSERT INTO user_info (id, name, age, create_time) VALUES (@p_id, @p_name, 0,
            """ + currentTimestampExpression() + ") END");
        jdbcTemplate.execute("CREATE PROCEDURE proc_double_value @p_input INT, @p_result INT OUTPUT AS " + //
                "BEGIN SET @p_result = @p_input * 2 END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_multi_inout @p_prefix VARCHAR(255), @p_suffix VARCHAR(255), @p_concat VARCHAR(255) OUTPUT, @p_length INT OUTPUT AS
            BEGIN SET @p_concat = @p_prefix + '-' + @p_suffix; SET @p_length = LEN(@p_concat) END
            """);
        jdbcTemplate.execute("CREATE PROCEDURE proc_user_count @p_count INT OUTPUT AS " + //
                "BEGIN SELECT @p_count = COUNT(*) FROM user_info WHERE name LIKE 'XmlCallable%' END");
        jdbcTemplate.execute("""
            CREATE PROCEDURE proc_user_stats @p_count INT OUTPUT, @p_max_id INT OUTPUT, @p_min_name VARCHAR(255) OUTPUT AS
            BEGIN
            SELECT @p_count = COUNT(*), @p_max_id = MAX(id) FROM user_info WHERE name LIKE 'XmlCallable%';
            SELECT TOP 1 @p_min_name = name FROM user_info WHERE name LIKE 'XmlCallable%' ORDER BY id;
            END
            """);
    }

    @Override
    protected String callableMapperPath() {
        return "/realdb/mssql/material/XmlCallableMapper.xml";
    }
}
