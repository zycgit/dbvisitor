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
package net.hasor.realdb.mysql;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MySqlTypesRWTest {
    protected void preTable(JdbcTemplate jdbc) throws SQLException, IOException {
        try {
            jdbc.executeUpdate("drop table tb_mysql_types");
        } catch (Exception e) {
            /**/
        }
        jdbc.loadSQL("/dbvisitor_coverage/all_types/tb_mysql_types.sql");
    }

    @Test
    public void testMySqlYear() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            preTable(jdbc);
            //
            jdbc.execute("insert into tb_mysql_types (c_year,c_year_n) values ('1998','2001')");
            //
            List<Map<String, Object>> list = jdbc.queryForList("select c_year,c_year_n from tb_mysql_types");
            assert list.size() == 1;
            assert list.get(0).get("c_year") instanceof Date;
            String dateString1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(list.get(0).get("c_year"));
            assert dateString1.equals("1998-01-01 00:00:00");
            //
            assert list.get(0).get("c_year_n") instanceof Date;
            String dateString2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(list.get(0).get("c_year_n"));
            assert dateString2.equals("2001-01-01 00:00:00");
        }
    }
}
