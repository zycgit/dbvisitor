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
package net.hasor.dbvisitor.jdbc.extractor;
import net.hasor.dbvisitor.jdbc.CallableStatementCreator;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 * @version : 2020-11-12
 * @author 赵永春 (zyc@hasor.net)
 */
public class CallableMultipleResultSetExtractorTest extends AbstractDbTest {

    public Connection initConnection() throws SQLException {
        Connection conn = DsUtils.mysqlConn();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
        jdbcTemplate.execute("drop table if exists proc_table_forcaller;");
        jdbcTemplate.execute("create table proc_table_forcaller( c_id int primary key, c_name varchar(200));");
        jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (1, 'aaa');");
        jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (2, 'bbb');");
        jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (3, 'ccc');");

        jdbcTemplate.execute("drop procedure if exists proc_select_table;");
        jdbcTemplate.execute("drop procedure if exists proc_select_multiple_table;");
        jdbcTemplate.execute("create procedure proc_select_table(in p_name varchar(200)) begin select * from proc_table_forcaller where c_name = p_name ; end;");
        jdbcTemplate.execute("create procedure proc_select_multiple_table(in p_name varchar(200)) begin select * from proc_table_forcaller where c_name = p_name ; select * from proc_table_forcaller where c_name = p_name ; end;");

        jdbcTemplate.execute("drop procedure if exists proc_select_table2;");
        jdbcTemplate.execute("create procedure proc_select_table2(in userName varchar(200), out outName varchar(200)) " + //
                "begin " + //
                "  select * from proc_table_forcaller where c_name = userName;" + //
                "  select * from proc_table_forcaller;" + //
                "  set outName = concat(userName,'-str');" + //
                "end;");

        return conn;
    }

    @Test
    public void callBack_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap = new JdbcTemplate(conn).executeCreator(con -> {
                CallableStatement cs = con.prepareCall("{call proc_select_multiple_table(?)}");
                cs.setString(1, "aaa");
                return cs;
            }, new CallableMultipleResultSetExtractor());

            List<Object> objectList = new ArrayList<>(objectMap.values());
            assert objectList.size() == 3;
            assert objectList.get(0) instanceof ArrayList;
            assert objectList.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectList.get(0)).size() == 1;
            assert ((ArrayList<?>) objectList.get(1)).size() == 1;
            assert ((Map) ((ArrayList<?>) objectList.get(0)).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectList.get(0)).get(0)).get("c_id").equals(1);
            assert ((Map) ((ArrayList<?>) objectList.get(1)).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectList.get(1)).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void callBack_2() throws SQLException {
        try (Connection conn = initConnection()) {
            List<Object> objectMap = new JdbcTemplate(conn).executeCreator((CallableStatementCreator) con -> {
                CallableStatement cs = con.prepareCall("{call proc_select_table2(?,?)}");
                cs.setString(1, "aaa");
                cs.registerOutParameter(2, Types.VARCHAR);
                return cs;
            }, cs -> {
                List<Object> objects = new CallableMultipleResultSetExtractor().doInCallableStatementAsList(cs);
                objects.add(cs.getString(2));
                return objects;
            });

            assert objectMap.size() == 4;
            assert objectMap.get(0) instanceof ArrayList;
            assert objectMap.get(1) instanceof ArrayList;
            assert ((ArrayList<?>) objectMap.get(0)).size() == 1;
            assert ((ArrayList<?>) objectMap.get(1)).size() == 3;
            assert ((Map) ((ArrayList<?>) objectMap.get(0)).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get(0)).get(0)).get("c_id").equals(1);
            assert ((Map) ((ArrayList<?>) objectMap.get(1)).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get(1)).get(0)).get("c_id").equals(1);
            assert objectMap.get(3).equals("aaa-str");
        }
    }

}
