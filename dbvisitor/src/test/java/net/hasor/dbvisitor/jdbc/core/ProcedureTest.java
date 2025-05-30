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
import net.hasor.dbvisitor.jdbc.CallableStatementCreator;
import net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO;
import net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler;
import net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler2;
import net.hasor.dbvisitor.jdbc.extractor.CallableMultipleResultSetExtractor;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.LongTypeHandler;
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
 * 存储过程测试
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class ProcedureTest extends AbstractDbTest {

    @Test
    public void procedure_out_1() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(#{out,mode=out,jdbcType=bigint, typeHandler=net.hasor.dbvisitor.types.handler.number.LongTypeHandler})}");

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Long;
            assert objectMap.get("out").equals(Long.parseLong("123123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void procedure_out_2() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(#{out,mode=out,jdbcType=bigint})}");

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Long;
            assert objectMap.get("out").equals(Long.parseLong("123123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void procedure_out_3() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(#{out,mode=out,javaType=java.lang.Long})}");

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Long;
            assert objectMap.get("out").equals(Long.parseLong("123123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void procedure_out_4_bad() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);

            try {
                jdbcTemplate.call("{call proc_bigint(#{out,mode=out})}");
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().startsWith("jdbcType must not be null");
            }
        }
    }

    @Test
    public void procedure_out_5() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(?)}",//
                    SqlArg.asOut("out", Types.BIGINT, new LongTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Long;
            assert objectMap.get("out").equals(Long.parseLong("123123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void procedure_out_6() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(?)}",//
                    SqlArg.asOut("out", Types.BIGINT));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Long;
            assert objectMap.get("out").equals(Long.parseLong("123123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void procedure_out_7() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(?)}",//
                    SqlArg.asOut("out", String.class));

            assert objectMap.size() == 2;
            assert objectMap.get("out").equals("123123");
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void procedure_out_8_bad() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            try {
                jdbcTemplate.call("{call proc_bigint(?)}", SqlArg.asOut("out", null));
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().startsWith("jdbcType must not be null");
            }
        }
    }

    public Connection initConnection() throws SQLException {
        Connection conn = DsUtils.mysqlConn();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
        jdbcTemplate.execute("drop table if exists proc_table_forcaller;");
        jdbcTemplate.execute("create table proc_table_forcaller( c_id int primary key, c_name varchar(200));");
        jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (1, 'aaa');");
        jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (2, 'bbb');");
        jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (3, 'ccc');");

        jdbcTemplate.execute("drop procedure if exists proc_select_table_1;");
        jdbcTemplate.execute("drop procedure if exists proc_select_multiple_table;");
        jdbcTemplate.execute("create procedure proc_select_table_1(in p_name varchar(200)) begin select * from proc_table_forcaller where c_name = p_name ; end;");
        jdbcTemplate.execute("create procedure proc_select_multiple_table(in p_name varchar(200)) begin select * from proc_table_forcaller where c_name = p_name ; select * from proc_table_forcaller where c_name = p_name ; end;");

        jdbcTemplate.execute("drop procedure if exists proc_select_table_2;");
        jdbcTemplate.execute("create procedure proc_select_table_2(in userName varchar(200), out outName varchar(200)) " + //
                "begin " + //
                "  select * from proc_table_forcaller where c_name = userName;" + //
                "  select * from proc_table_forcaller;" + //
                "  set outName = concat(userName,'-str');" + //
                "end;");

        jdbcTemplate.execute("drop procedure if exists proc_inout;");
        jdbcTemplate.execute("create procedure proc_inout(in userName varchar(200), inout inoutName varchar(200)) " + //
                "begin " + //
                "  set inoutName = concat(userName,'-', inoutName);" + //
                "end;");

        return conn;
    }

    @Test
    public void one_result_as_map_using_nameless_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap = new JdbcTemplate(conn).call("{call proc_select_table_1(?)}", "aaa");

            assert objectMap.size() == 2;
            assert objectMap.get("#result-set-1") instanceof ArrayList;
            assert objectMap.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap.get("#result-set-1")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void one_result_as_map_using_named_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,name=res}", "aaa");

            assert objectMap.size() == 2;
            assert objectMap.get("res") instanceof ArrayList;
            assert objectMap.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap.get("res")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap.get("res")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get("res")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void one_result_as_map_using_javaType_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,name=res,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO}", "aaa");
            assert objectMap1.size() == 2;
            assert objectMap1.get("res") instanceof ArrayList;
            assert objectMap1.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getId() == 1;

            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO}", "aaa");
            assert objectMap2.size() == 2;
            assert objectMap2.get("#result-set-1") instanceof ArrayList;
            assert objectMap2.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap2.get("#result-set-1")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("#result-set-1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("#result-set-1")).get(0)).getId() == 1;
        }
    }

    @Test
    public void one_result_as_map_using_mapper_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,name=res,rowMapper=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOMapper}", "aaa");
            assert objectMap1.size() == 2;
            assert objectMap1.get("res") instanceof ArrayList;
            assert objectMap1.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getId() == 1;

            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,rowMapper=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOMapper}", "aaa");
            assert objectMap2.size() == 2;
            assert objectMap2.get("#result-set-1") instanceof ArrayList;
            assert objectMap2.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap2.get("#result-set-1")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("#result-set-1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("#result-set-1")).get(0)).getId() == 1;
        }
    }

    @Test
    public void one_result_as_map_using_extractor_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,name=res,extractor=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOExtractor}", "aaa");
            assert objectMap1.size() == 2;
            assert objectMap1.get("res") instanceof ArrayList;
            assert objectMap1.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getId() == 1;

            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,extractor=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOExtractor}", "aaa");
            assert objectMap2.size() == 2;
            assert objectMap2.get("#result-set-1") instanceof ArrayList;
            assert objectMap2.get("#update-count-2").equals(0);
            assert ((ArrayList<?>) objectMap2.get("#result-set-1")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("#result-set-1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("#result-set-1")).get(0)).getId() == 1;
        }
    }

    @Test
    public void one_result_as_map_using_handler_1() throws SQLException {
        try (Connection conn = initConnection()) {
            ProcedureTestUserDTOHandler.getResult().clear();
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,name=res,rowHandler=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler}", "aaa");
            assert objectMap1.size() == 2;
            assert objectMap1.get("res").equals("resultSet returned from stored procedure was processed");
            assert objectMap1.get("#update-count-2").equals(0);
            List<ProcedureTestUserDTO> resultList1 = ProcedureTestUserDTOHandler.getResult();
            assert resultList1.size() == 1;
            assert resultList1.get(0).getName().equals("aaa");
            assert resultList1.get(0).getId() == 1;

            //
            ProcedureTestUserDTOHandler.getResult().clear();
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call("{call proc_select_table_1(?)} @{resultSet,rowHandler=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler}", "aaa");
            assert objectMap2.size() == 2;
            assert objectMap2.get("#result-set-1").equals("resultSet returned from stored procedure was processed");
            assert objectMap2.get("#update-count-2").equals(0);
            List<ProcedureTestUserDTO> resultList2 = ProcedureTestUserDTOHandler.getResult();
            assert resultList2.size() == 1;
            assert resultList2.get(0).getName().equals("aaa");
            assert resultList2.get(0).getId() == 1;
        }
    }

    @Test
    public void multiple_result_as_map_using_nameless_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap = new JdbcTemplate(conn).call("{call proc_select_multiple_table(?)}", "aaa");

            assert objectMap.size() == 3;
            assert objectMap.get("#result-set-1") instanceof ArrayList;
            assert objectMap.get("#result-set-2") instanceof ArrayList;
            assert objectMap.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap.get("#result-set-1")).size() == 1;
            assert ((ArrayList<?>) objectMap.get("#result-set-2")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_id").equals(1);
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void multiple_result_as_map_using_named_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1} @{resultSet,name=res2}", "aaa");
            assert objectMap1.size() == 3;
            assert objectMap1.get("res1") instanceof ArrayList;
            assert objectMap1.get("res2") instanceof ArrayList;
            assert objectMap1.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap1.get("res2")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap1.get("res1")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap1.get("res1")).get(0)).get("c_id").equals(1);
            assert ((Map) ((ArrayList<?>) objectMap1.get("res2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap1.get("res2")).get(0)).get("c_id").equals(1);

            //
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1}", "aaa");
            assert objectMap2.size() == 3;
            assert objectMap2.get("res1") instanceof ArrayList;
            assert objectMap2.get("#result-set-2") instanceof ArrayList;
            assert objectMap2.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap2.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap2.get("#result-set-2")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap2.get("res1")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap2.get("res1")).get(0)).get("c_id").equals(1);
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void multiple_result_as_map_using_javaType_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO} @{resultSet,name=res2,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO}", "aaa");
            assert objectMap1.size() == 3;
            assert objectMap1.get("res1") instanceof ArrayList;
            assert objectMap1.get("res2") instanceof ArrayList;
            assert objectMap1.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap1.get("res2")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res1")).get(0)).getId() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res2")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res2")).get(0)).getId() == 1;

            //
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO}", "aaa");
            assert objectMap2.size() == 3;
            assert objectMap2.get("res1") instanceof ArrayList;
            assert objectMap2.get("#result-set-2") instanceof ArrayList;
            assert objectMap2.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap2.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap2.get("#result-set-2")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res1")).get(0)).getId() == 1;
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void multiple_result_as_map_using_mapper_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,rowMapper=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOMapper} @{resultSet,name=res2,rowMapper=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOMapper}", "aaa");
            assert objectMap1.size() == 3;
            assert objectMap1.get("res1") instanceof ArrayList;
            assert objectMap1.get("res2") instanceof ArrayList;
            assert objectMap1.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap1.get("res2")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res1")).get(0)).getId() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res2")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res2")).get(0)).getId() == 1;

            //
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,rowMapper=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOMapper}", "aaa");
            assert objectMap2.size() == 3;
            assert objectMap2.get("res1") instanceof ArrayList;
            assert objectMap2.get("#result-set-2") instanceof ArrayList;
            assert objectMap2.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap2.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap2.get("#result-set-2")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res1")).get(0)).getId() == 1;
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void multiple_result_as_map_using_extractor_1() throws SQLException {
        try (Connection conn = initConnection()) {
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,extractor=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOExtractor} @{resultSet,name=res2,extractor=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOExtractor}", "aaa");
            assert objectMap1.size() == 3;
            assert objectMap1.get("res1") instanceof ArrayList;
            assert objectMap1.get("res2") instanceof ArrayList;
            assert objectMap1.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap1.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap1.get("res2")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res1")).get(0)).getId() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res2")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res2")).get(0)).getId() == 1;

            //
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,extractor=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOExtractor}", "aaa");
            assert objectMap2.size() == 3;
            assert objectMap2.get("res1") instanceof ArrayList;
            assert objectMap2.get("#result-set-2") instanceof ArrayList;
            assert objectMap2.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap2.get("res1")).size() == 1;
            assert ((ArrayList<?>) objectMap2.get("#result-set-2")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res1")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res1")).get(0)).getId() == 1;
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void multiple_result_as_map_using_handler_1() throws SQLException {
        try (Connection conn = initConnection()) {
            ProcedureTestUserDTOHandler.getResult().clear();
            ProcedureTestUserDTOHandler2.getResult().clear();
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,rowHandler=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler} @{resultSet,name=res2,rowHandler=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler2}", "aaa");
            assert objectMap1.size() == 3;
            assert objectMap1.get("res1").equals("resultSet returned from stored procedure was processed");
            assert objectMap1.get("res2").equals("resultSet returned from stored procedure was processed");
            assert objectMap1.get("#update-count-3").equals(0);
            List<ProcedureTestUserDTO> resultList1 = ProcedureTestUserDTOHandler.getResult();
            List<ProcedureTestUserDTO> resultList2 = ProcedureTestUserDTOHandler2.getResult();
            assert resultList1.size() == 1;
            assert resultList1.get(0).getName().equals("aaa");
            assert resultList1.get(0).getId() == 1;
            assert resultList2.size() == 1;
            assert resultList2.get(0).getName().equals("aaa");
            assert resultList2.get(0).getId() == 1;

            //
            ProcedureTestUserDTOHandler.getResult().clear();
            ProcedureTestUserDTOHandler2.getResult().clear();
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call(//
                    "{call proc_select_multiple_table(?)} @{resultSet,name=res1,rowHandler=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTOHandler}", "aaa");
            assert objectMap2.size() == 3;
            assert objectMap2.get("res1").equals("resultSet returned from stored procedure was processed");
            assert objectMap2.get("#result-set-2") instanceof ArrayList;
            assert objectMap2.get("#update-count-3").equals(0);
            List<ProcedureTestUserDTO> resultList3 = ProcedureTestUserDTOHandler.getResult();
            assert resultList3.size() == 1;
            assert resultList3.get(0).getName().equals("aaa");
            assert resultList3.get(0).getId() == 1;
            assert ((ArrayList<?>) objectMap2.get("#result-set-2")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap2.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
    }

    @Test
    public void call_1() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop table if exists proc_table_forcaller;");
            jdbcTemplate.execute("create table proc_table_forcaller( c_id int primary key, c_name varchar(200));");
            jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (1, 'aaa');");
            jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (2, 'bbb');");
            jdbcTemplate.execute("insert into proc_table_forcaller (c_id,c_name) values (3, 'ccc');");

            jdbcTemplate.execute("drop procedure if exists proc_select_cross_table;");
            jdbcTemplate.execute(""//
                    + "create procedure proc_select_cross_table(in p_name varchar(200), out p_out varchar(200))" //
                    + " begin " //
                    + "   select * from proc_table_forcaller where c_name = p_name ;" //
                    + "   select * from proc_table_forcaller where c_name = p_name ;" //
                    + "   set p_out = p_name;"//
                    + " end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_select_cross_table(#{arg0,jdbcType=varchar},#{arg1,mode=out,jdbcType=varchar})}",//
                    new Object[] { "aaa", "bbb" });

            assert objectMap.size() == 4;
            assert objectMap.get("arg1").equals("aaa");
            assert objectMap.get("#result-set-1") instanceof ArrayList;
            assert objectMap.get("#result-set-2") instanceof ArrayList;
            assert objectMap.get("#update-count-3").equals(0);
            assert ((ArrayList<?>) objectMap.get("#result-set-1")).size() == 1;
            assert ((ArrayList<?>) objectMap.get("#result-set-2")).size() == 1;
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_id").equals(1);
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_id").equals(1);
        }
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
                CallableStatement cs = con.prepareCall("{call proc_select_table_2(?,?)}");
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

    @Test
    public void inout_1() throws SQLException {
        try (Connection conn = initConnection()) {
            String call = "{call proc_inout(#{inName}, #{outName,name=out,mode=inout,jdbcType=varchar})}";
            Map<String, Object> args = CollectionUtils.asMap("inName", "aaa", "outName", "bbb");
            Map<String, Object> objectMap = new JdbcTemplate(conn).call(call, args);

            assert objectMap.get("out").equals("aaa-bbb");
        }
    }

    private void jdbcTemplateExecute(JdbcTemplate jdbcTemplate, String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void cursor_result_as_javaType_1() throws SQLException {
        try (Connection conn = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplateExecute(jdbcTemplate, "drop table proc_table_forcaller");
            jdbcTemplateExecute(jdbcTemplate, "create table proc_table_forcaller( c_id int primary key, c_name varchar(200))");
            jdbcTemplateExecute(jdbcTemplate, "insert into proc_table_forcaller (c_id,c_name) values (1, 'aaa')");
            jdbcTemplateExecute(jdbcTemplate, "insert into proc_table_forcaller (c_id,c_name) values (2, 'bbb')");
            jdbcTemplateExecute(jdbcTemplate, "insert into proc_table_forcaller (c_id,c_name) values (3, 'ccc')");
            jdbcTemplateExecute(jdbcTemplate, "" + //
                    "create or replace procedure proc_out_cursor( " + //
                    "    userName in varchar2, " + //
                    "    tableCursor out sys_refcursor) " + //
                    "as " + //
                    "begin " + //
                    "    open tableCursor for select * from proc_table_forcaller where c_name = userName;" + //
                    "end;");

            //            conn.setAutoCommit(false);
            Map<String, Object> objectMap1 = new JdbcTemplate(conn).call("{call proc_out_cursor(?,#{res,mode=cursor,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO})}", "aaa");
            assert objectMap1.size() == 2;
            assert objectMap1.get("res") instanceof ArrayList;
            assert objectMap1.get("#update-count-1").equals(-1); // oracle is -1
            assert ((ArrayList<?>) objectMap1.get("res")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap1.get("res")).get(0)).getId() == 1;

            //
            //            conn.setAutoCommit(false);
            Map<String, Object> objectMap2 = new JdbcTemplate(conn).call("{call proc_out_cursor(?,#{name=res,mode=cursor,javaType=net.hasor.dbvisitor.jdbc.core.test.ProcedureTestUserDTO})}", "aaa");
            assert objectMap2.size() == 2;
            assert objectMap2.get("res") instanceof ArrayList;
            assert objectMap2.get("#update-count-1").equals(-1); // oracle is -1
            assert ((ArrayList<?>) objectMap2.get("res")).size() == 1;
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res")).get(0)).getName().equals("aaa");
            assert ((ProcedureTestUserDTO) ((ArrayList<?>) objectMap2.get("res")).get(0)).getId() == 1;
        }
    }

}
