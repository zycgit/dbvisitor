/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.transaction;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.AbstractDbTest;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2015-11-10
 */
public abstract class AbstractPropagationTest extends AbstractDbTest {
    protected void initTable(Connection conn) throws SQLException, IOException {
        conn.setTransactionIsolation(Isolation.REPEATABLE_READ.getValue());
        JdbcTemplate initJdbc = new JdbcTemplate(conn);
        initJdbc.execute("drop table if exists user_info;");
        initJdbc.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
    }

    protected int selectCount(Connection conn) throws SQLException {
        return new JdbcTemplate(conn).queryForInt("select count(*) from user_info");
    }

    protected int selectCount(JdbcTemplate jdbc) throws SQLException {
        return jdbc.queryForInt("select count(*) from user_info");
    }

    protected int selectCount(DataSource dataSource) throws SQLException {
        return new JdbcTemplate(dataSource).queryForInt("select count(*) from user_info");
    }
}
