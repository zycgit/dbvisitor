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
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.hasor.test.utils.TestUtils.*;

/***
 * @version : 2020-11-12
 * @author 赵永春 (zyc@hasor.net)
 */
public class MultipleExtractorTest extends AbstractDbTest {
    @Test
    public void testMultipleResultExtractor_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = 'muhammad';\n"//
                    + "select * from user_info where login_name = 'belon';\n";
            List<Object> objectList = jdbcTemplate.multipleExecute(multipleSql);

            assert objectList.size() == 2;
            assert objectList.get(0) instanceof ArrayList;
            assert objectList.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectList.get(0)).size() == 1;
            assert ((ArrayList<?>) objectList.get(1)).size() == 1;
            assert ((ArrayList<?>) objectList.get(0)).get(0) instanceof Map;
            assert ((ArrayList<?>) objectList.get(1)).get(0) instanceof Map;
            assert ((Map) ((ArrayList<?>) objectList.get(0)).get(0)).get("user_uuid").equals(beanForData1().getUserUuid());
            assert ((Map) ((ArrayList<?>) objectList.get(1)).get(0)).get("user_uuid").equals(beanForData2().getUserUuid());
        }
    }

    @Test
    public void testMultipleResultExtractor_2() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = ?;\n"//
                    + "select * from user_info where login_name = ?;\n";
            Object[] multipleSqlArgs = new Object[] { beanForData1().getLoginName(), beanForData2().getLoginName() };
            List<Object> objectList = jdbcTemplate.multipleExecute(multipleSql, multipleSqlArgs);

            assert objectList.size() == 2;
            assert objectList.get(0) instanceof ArrayList;
            assert objectList.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectList.get(0)).size() == 1;
            assert ((ArrayList<?>) objectList.get(1)).size() == 1;
            assert ((ArrayList<?>) objectList.get(0)).get(0) instanceof Map;
            assert ((ArrayList<?>) objectList.get(1)).get(0) instanceof Map;
            assert ((Map) ((ArrayList<?>) objectList.get(0)).get(0)).get("user_uuid").equals(beanForData1().getUserUuid());
            assert ((Map) ((ArrayList<?>) objectList.get(1)).get(0)).get("user_uuid").equals(beanForData2().getUserUuid());
        }
    }

    @Test
    public void testMultipleResultExtractor_3() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            Map<String, String> data = new HashMap<>();
            data.put("name1", "muhammad");
            data.put("name2", "belon");
            String multipleSql = ""//
                    + "select * from user_info where login_name = :name1 ;\n"//
                    + "select * from user_info where login_name = :name2 ;\n";
            List<Object> objectList = jdbcTemplate.multipleExecute(multipleSql, data);

            assert objectList.size() == 2;
            assert objectList.get(0) instanceof ArrayList;
            assert objectList.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectList.get(0)).size() == 1;
            assert ((ArrayList<?>) objectList.get(1)).size() == 1;
            assert ((ArrayList<?>) objectList.get(0)).get(0) instanceof Map;
            assert ((ArrayList<?>) objectList.get(1)).get(0) instanceof Map;
            assert ((Map) ((ArrayList<?>) objectList.get(0)).get(0)).get("user_uuid").equals(beanForData1().getUserUuid());
            assert ((Map) ((ArrayList<?>) objectList.get(1)).get(0)).get("user_uuid").equals(beanForData2().getUserUuid());
        }
    }

    @Test
    public void testMultipleResultExtractor_4() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            Map<String, String> data = new HashMap<>();
            data.put("name1", "muhammad");
            data.put("name2", "belon");
            String multipleSql = ""//
                    + "select * from user_info where login_name = :name1;\n"//
                    + "select * from user_info where login_name = :name2;\n";
            List<Object> objectList = jdbcTemplate.multipleExecute(multipleSql, new MapSqlParameterSource(data));

            assert objectList.size() == 2;
            assert objectList.get(0) instanceof ArrayList;
            assert objectList.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectList.get(0)).size() == 1;
            assert ((ArrayList<?>) objectList.get(1)).size() == 1;
            assert ((ArrayList<?>) objectList.get(0)).get(0) instanceof Map;
            assert ((ArrayList<?>) objectList.get(1)).get(0) instanceof Map;
            assert ((Map) ((ArrayList<?>) objectList.get(0)).get(0)).get("user_uuid").equals(beanForData1().getUserUuid());
            assert ((Map) ((ArrayList<?>) objectList.get(1)).get(0)).get("user_uuid").equals(beanForData2().getUserUuid());
        }
    }
}
