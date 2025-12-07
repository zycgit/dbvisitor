/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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