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
package net.hasor.realdb.oracle;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OracleTypesTest {
    protected void preTable(JdbcTemplate jdbc) throws SQLException, IOException {
        try {
            jdbc.executeUpdate("drop table tb_oracle_types");
        } catch (Exception e) {
            /**/
        }
        jdbc.loadSQL("/dbvisitor_coverage/all_types/tb_oracle_types.sql");
    }

    @Test
    public void testOracleClob() throws Exception {
        try (Connection conn = DsUtils.oracleConn()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            preTable(jdbc);

            jdbc.execute("insert into tb_oracle_types (id,c_clob,c_nclob) values ('1','123','456')");

            List<Map<String, Object>> list = jdbc.queryForList("select c_clob,c_nclob from tb_oracle_types where id = '1'");
            assert list.size() == 1;
            assert list.get(0).get("c_clob").equals("123");
            assert list.get(0).get("c_nclob").equals("456");
        }
    }

    @Test
    public void testOracleTime() throws Exception {
        try (Connection conn = DsUtils.oracleConn()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            preTable(jdbc);
            Date time = new Date(System.currentTimeMillis());
            jdbc.executeUpdate("insert into tb_oracle_types (id,c_timestamp,c_timestamp_n) values (?,?,?)", new Object[] { "1", time, time });

            List<Map<String, Object>> list = jdbc.queryForList("select c_timestamp,c_timestamp_n from tb_oracle_types where id = '1'");
            assert list.size() == 1;
            assert list.get(0).get("c_timestamp") instanceof Date;
            assert list.get(0).get("c_timestamp_n") instanceof Date;
            assert ((Date) list.get(0).get("c_timestamp")).getTime() == time.getTime();
            assert ((Date) list.get(0).get("c_timestamp_n")).getTime() == time.getTime();
        }
    }
}
