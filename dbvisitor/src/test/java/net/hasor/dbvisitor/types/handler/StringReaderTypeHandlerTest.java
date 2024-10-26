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
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;

public class StringReaderTypeHandlerTest {
    @Test
    public void testClobReaderTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new ClobAsReaderTypeHandler().getResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testClobReaderTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new ClobAsReaderTypeHandler().getResult(rs, "c_char_lage");
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testClobReaderTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<Reader> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new ClobAsReaderTypeHandler().setParameter(ps, 1, new StringReader("abcedfg"), JDBCType.CLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new ClobAsReaderTypeHandler().getNullableResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcedfg");
        }
    }

    @Test
    public void testClobReaderTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_text;");
            jdbcTemplate.execute("create procedure proc_text(out p_out text) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_text(?)}",//
                    SqlArg.asOut("out", JDBCType.CLOB.getVendorTypeNumber(), new ClobAsReaderTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Reader;
            String body = IOUtils.readToString((Reader) objectMap.get("out"));
            assert body.equals("abcdefg");
        }
    }

    @Test
    public void testNClobReaderTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NClobAsReaderTypeHandler().getResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testNClobReaderTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NClobAsReaderTypeHandler().getResult(rs, "c_char_lage");
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testNClobReaderTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<Reader> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new NClobAsReaderTypeHandler().setParameter(ps, 1, new StringReader("abcedfg"), JDBCType.CLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new NClobAsReaderTypeHandler().getNullableResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcedfg");
        }
    }

    @Test
    public void testNClobReaderTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_text;");
            jdbcTemplate.execute("create procedure proc_text(out p_out text) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_text(?)}",//
                    SqlArg.asOut("out", JDBCType.NCLOB.getVendorTypeNumber(), new NClobAsReaderTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Reader;
            String body = IOUtils.readToString((Reader) objectMap.get("out"));
            assert body.equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new StringAsReaderTypeHandler().getResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new StringAsReaderTypeHandler().getResult(rs, "c_char_lage");
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
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

            List<Reader> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new StringAsReaderTypeHandler().setParameter(ps, 1, new StringReader("abcdefg"), JDBCType.CLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new StringAsReaderTypeHandler().getNullableResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testStringTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(10)) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    SqlArg.asOut("out", JDBCType.VARCHAR.getVendorTypeNumber(), new StringAsReaderTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Reader;
            String body = IOUtils.readToString((Reader) objectMap.get("out"));
            assert "abcdefg".equals(body);
        }
    }

    @Test
    public void testNStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NStringAsReaderTypeHandler().getResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testNStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char_lage) values ('abcdefg');");
            List<Reader> dat = jdbcTemplate.queryForList("select c_char_lage from tb_h2_types where c_char_lage is not null limit 1;", (rs, rowNum) -> {
                return new NStringAsReaderTypeHandler().getResult(rs, "c_char_lage");
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
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

            List<Reader> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new NStringAsReaderTypeHandler().setParameter(ps, 1, new StringReader("abcdefg"), JDBCType.CLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new NStringAsReaderTypeHandler().getNullableResult(rs, 1);
            });
            String readerDat = IOUtils.toString(dat.get(0));
            assert readerDat.equals("abcdefg");
        }
    }

    @Test
    public void testNStringTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_nvarchar;");
            jdbcTemplate.execute("create procedure proc_nvarchar(out p_out nvarchar(10)) begin set p_out='abcdefg'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_nvarchar(?)}",//
                    SqlArg.asOut("out", JDBCType.NVARCHAR.getVendorTypeNumber(), new NStringAsReaderTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Reader;
            String body = IOUtils.readToString((Reader) objectMap.get("out"));
            assert "abcdefg".equals(body);
        }
    }
}
