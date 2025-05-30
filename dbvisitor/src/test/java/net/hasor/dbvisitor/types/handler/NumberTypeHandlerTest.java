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
package net.hasor.dbvisitor.types.handler;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.NumberTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NumberTypeHandlerTest {
    @Test
    public void testNumberTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Number> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new NumberTypeHandler().getResult(rs, 1);
            });

            assert dat.get(0).longValue() == testData.getTime();
        }
    }

    @Test
    public void testNumberTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Number> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new NumberTypeHandler().getResult(rs, "c_timestamp");
            });

            assert dat.get(0).longValue() == testData.getTime();
        }
    }

    @Test
    public void testNumberTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char) values ('123');");
            List<Number> dat = jdbcTemplate.queryForList("select c_char from tb_h2_types where c_char is not null limit 1;", (rs, rowNum) -> {
                return new NumberTypeHandler().getResult(rs, "c_char");
            });

            assert dat.get(0).longValue() == 123;
        }
    }

    @Test
    public void testNumberTypeHandler_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('123');");
            List<Number> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NumberTypeHandler().getResult(rs, "c_char_lage");
            });

            assert dat.get(0).longValue() == 123;
        }
    }

    @Test
    public void testNumberTypeHandler_5() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(10)) begin set p_out='123.4'; end;");
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=1234; end;");
            jdbcTemplate.execute("drop procedure if exists proc_float;");
            jdbcTemplate.execute("create procedure proc_float(out p_out float) begin set p_out='123.4'; end;");
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");
            jdbcTemplate.execute("drop procedure if exists proc_data;");
            jdbcTemplate.execute("create procedure proc_data(out p_out date) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap1 = jdbcTemplate.call("{call proc_varchar(?)}",//
                    SqlArg.asOut("out", JDBCType.VARCHAR.getVendorTypeNumber(), new NumberTypeHandler()));
            Map<String, Object> objectMap2 = jdbcTemplate.call("{call proc_bigint(?)}",//
                    SqlArg.asOut("out", JDBCType.BIGINT.getVendorTypeNumber(), new NumberTypeHandler()));
            Map<String, Object> objectMap4 = jdbcTemplate.call("{call proc_float(?)}",//
                    SqlArg.asOut("out", JDBCType.FLOAT.getVendorTypeNumber(), new NumberTypeHandler()));
            Map<String, Object> objectMap5 = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new NumberTypeHandler()));
            Map<String, Object> objectMap6 = jdbcTemplate.call("{call proc_data(?)}",//
                    SqlArg.asOut("out", JDBCType.DATE.getVendorTypeNumber(), new NumberTypeHandler()));

            assert objectMap1.size() == 2;
            assert objectMap2.size() == 2;
            assert objectMap4.size() == 2;
            assert objectMap5.size() == 2;
            assert objectMap6.size() == 2;
            assert objectMap1.get("out") instanceof Number;
            assert objectMap2.get("out") instanceof Number;
            assert objectMap4.get("out") instanceof Number;
            assert objectMap5.get("out") instanceof Number;
            assert objectMap6.get("out") instanceof Number;
            assert objectMap1.get("out").toString().equals("123.4");
            assert objectMap2.get("out").toString().equals("1234");
            assert objectMap4.get("out").toString().startsWith("123.4"); // 有可能出现精度问题
            Date parseData1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-09 10:11:12");
            assert objectMap5.get("out").toString().equals(String.valueOf(parseData1.getTime()));
            Date parseData2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-09 00:00:00");
            assert objectMap6.get("out").toString().equals(String.valueOf(parseData2.getTime()));
        }
    }
}
