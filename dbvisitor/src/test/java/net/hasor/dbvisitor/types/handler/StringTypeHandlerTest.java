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
import net.hasor.dbvisitor.types.handler.string.ClobAsStringTypeHandler;
import net.hasor.dbvisitor.types.handler.string.NClobAsStringTypeHandler;
import net.hasor.dbvisitor.types.handler.string.NStringTypeHandler;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StringTypeHandlerTest {
    @Test
    public void testClobTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new ClobAsStringTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testClobTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new ClobAsStringTypeHandler().getResult(rs, "c_char_lage");
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testClobTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new ClobAsStringTypeHandler().setParameter(ps, 1, "abcedfg", JDBCType.CLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new ClobAsStringTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).equals("abcedfg");
        }
    }

    @Test
    public void testClobTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_text;");
            jdbcTemplate.execute("create procedure proc_text(out p_out text) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_text(?)}",//
                    SqlArg.asOut("out", JDBCType.CLOB.getVendorTypeNumber(), new ClobAsStringTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof String;
            assert objectMap.get("out").equals("abcdefg");
        }
    }

    @Test
    public void testNClobTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NClobAsStringTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testNClobTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NClobAsStringTypeHandler().getResult(rs, "c_char_lage");
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testNClobTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new NClobAsStringTypeHandler().setParameter(ps, 1, "abcedfg", JDBCType.CLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new NClobAsStringTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).equals("abcedfg");
        }
    }

    @Test
    public void testNClobTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_text;");
            jdbcTemplate.execute("create procedure proc_text(out p_out text) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_text(?)}",//
                    SqlArg.asOut("out", JDBCType.CLOB.getVendorTypeNumber(), new NClobAsStringTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof String;
            assert objectMap.get("out").equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new StringTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new StringTypeHandler().getResult(rs, "c_char_lage");
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "abcdefg" }, String.class);
            String dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { null }, String.class);
            assert dat1.equals("abcdefg");
            assert dat2 == null;

            List<String> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new StringTypeHandler().setParameter(ps, 1, "abcdefg", JDBCType.VARCHAR.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new StringTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(10)) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    SqlArg.asOut("out", JDBCType.VARCHAR.getVendorTypeNumber(), new StringTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof String;
            assert objectMap.get("out").equals("abcdefg");
        }
    }

    @Test
    public void testNStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NStringTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testNStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<String> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NStringTypeHandler().getResult(rs, "c_char_lage");
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testNStringTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "abcdefg" }, String.class);
            String dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { null }, String.class);
            assert dat1.equals("abcdefg");
            assert dat2 == null;

            List<String> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new NStringTypeHandler().setParameter(ps, 1, "abcdefg", JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new NStringTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).equals("abcdefg");
        }
    }

    @Test
    public void testNStringTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_nvarchar;");
            jdbcTemplate.execute("create procedure proc_nvarchar(out p_out nvarchar(10)) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_nvarchar(?)}",//
                    SqlArg.asOut("out", JDBCType.NVARCHAR.getVendorTypeNumber(), new NStringTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof String;
            assert objectMap.get("out").equals("abcdefg");
        }
    }
}
