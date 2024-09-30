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
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
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
    public void noargs_1() throws SQLException, IOException {
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
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void usingPosArgs_1() throws SQLException, IOException {
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
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, multipleSqlArgs);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void usingNamedArgs_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = :loginName1;\n"//
                    + "select * from user_info where login_name = :loginName2;\n";
            Map<String, String> dataMap = CollectionUtils.asMap(//
                    "loginName1", beanForData1().getLoginName(),//
                    "loginName2", beanForData2().getLoginName());
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, dataMap);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void usingNamedArgs_2() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = &loginName1;\n"//
                    + "select * from user_info where login_name = &loginName2;\n";
            Map<String, String> dataMap = CollectionUtils.asMap(//
                    "loginName1", beanForData1().getLoginName(),//
                    "loginName2", beanForData2().getLoginName());
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, dataMap);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void usingNamedArgs_3() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = #{loginName1};\n"//
                    + "select * from user_info where login_name = #{loginName2};\n";
            Map<String, String> dataMap = CollectionUtils.asMap(//
                    "loginName1", beanForData1().getLoginName(),//
                    "loginName2", beanForData2().getLoginName());
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, dataMap);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void usingInjectArgs_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = ${\"'\" + loginName1 + \"'\"};\n"//
                    + "select * from user_info where login_name = ${\"'\" + loginName2 + \"'\"};\n";
            Map<String, String> dataMap = CollectionUtils.asMap(//
                    "loginName1", beanForData1().getLoginName(),//
                    "loginName2", beanForData2().getLoginName());
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, dataMap);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void usingRuleArgs_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = @{arg,true, loginName1};\n"//
                    + "select * from user_info where login_name = @{arg,true, loginName2};\n";
            Map<String, String> dataMap = CollectionUtils.asMap(//
                    "loginName1", beanForData1().getLoginName(),//
                    "loginName2", beanForData2().getLoginName());
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, dataMap);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void argtype_as_pos_1() throws SQLException, IOException {
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
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, new Object[] { "muhammad", "belon" });
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void argType_as_map_1() throws SQLException, IOException {
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
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, data);
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void argtype_as_source_1() throws SQLException, IOException {
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
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, new MapSqlArgSource(data));
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void argtype_as_setter_1() throws SQLException, IOException {
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
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql, ps -> {
                ps.setString(1, "muhammad");
                ps.setString(2, "belon");
            });
            List<Object> objectList = new ArrayList<>(objectMap.values());

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
    public void noargs_result_as_javaType_1() throws SQLException, IOException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists user_info;");
            jdbcTemplate.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());

            String multipleSql = ""//
                    + "select * from user_info where login_name = 'muhammad'; @{result,name=res1,javaType=net.hasor.test.dto.UserInfo2}\n"//
                    + "select * from user_info where login_name = 'belon';    @{result,name=res2,javaType=net.hasor.test.dto.UserInfo2}\n";
            Map<String, Object> objectMap = jdbcTemplate.multipleExecute(multipleSql);
            List<Object> objectList = new ArrayList<>(objectMap.values());

            assert objectList.size() == 2;
            assert objectList.get(0) instanceof ArrayList;
            assert objectList.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectList.get(0)).size() == 1;
            assert ((ArrayList<?>) objectList.get(1)).size() == 1;
            assert ((ArrayList<?>) objectList.get(0)).get(0) instanceof UserInfo2;
            assert ((ArrayList<?>) objectList.get(1)).get(0) instanceof UserInfo2;
            assert ((UserInfo2) ((ArrayList<?>) objectList.get(0)).get(0)).getUid().equals(beanForData1().getUserUuid());
            assert ((UserInfo2) ((ArrayList<?>) objectList.get(1)).get(0)).getUid().equals(beanForData2().getUserUuid());
        }
    }
}
