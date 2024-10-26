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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SqlXmlTypeHandlerTest {
    protected void preTable(JdbcTemplate jdbcTemplate) throws SQLException {
        try {
            jdbcTemplate.executeUpdate("drop table tb_oracle_types_onlyxml");
        } catch (Exception e) {
            /**/
        }
        jdbcTemplate.executeUpdate("create table tb_oracle_types_onlyxml (c_xml xmltype)");
    }

    protected void preProc(JdbcTemplate jdbcTemplate) throws SQLException {
        try {
            jdbcTemplate.executeUpdate("drop procedure proc_xmltype");
        } catch (Exception e) {
            /**/
        }
        jdbcTemplate.execute(""//
                + "create or replace procedure proc_xmltype(p_out out xmltype) as " //
                + "begin " //
                + "  SELECT (XMLTYPE('<xml>abc</xml>')) into p_out FROM DUAL; " //
                + "end;");
    }

    @Test
    public void testSqlXmlTypeHandler_1() throws SQLException {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values ('<xml>abc</xml>')");
            List<String> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).trim().equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlTypeHandler_2() throws SQLException {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values ('<xml>abc</xml>')");
            List<String> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlTypeHandler().getResult(rs, "c_xml");
            });
            assert dat.get(0).trim().equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlTypeHandler_3() throws SQLException {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> dat = jdbcTemplate.queryForList("select ? from dual", ps -> {
                new SqlXmlTypeHandler().setParameter(ps, 1, "<xml>abc</xml>", JDBCType.SQLXML.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlXmlTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlTypeHandler_4() throws SQLException {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preProc(jdbcTemplate);

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_xmltype(?)}",//
                    SqlArg.asOut("out", JDBCType.SQLXML.getVendorTypeNumber(), new SqlXmlTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof String;
            assert objectMap.get("out").equals("<xml>abc</xml>");
            assert objectMap.get("#update-count-1").equals(-1);// in oracle ,no more result is -1
        }
    }

    @Test
    public void testSqlXmlForInputStreamTypeHandler_1() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values ('<xml>abc</xml>')");
            List<InputStream> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlAsInputStreamTypeHandler().getResult(rs, 1);
            });
            String xmlBody = IOUtils.readToString(dat.get(0), "UTF-8");
            assert xmlBody.equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlForInputStreamTypeHandler_2() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values ('<xml>abc</xml>')");
            List<InputStream> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlAsInputStreamTypeHandler().getResult(rs, "c_xml");
            });
            String xmlBody = IOUtils.readToString(dat.get(0), "UTF-8");
            assert xmlBody.equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlForInputStreamTypeHandler_3() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<InputStream> dat = jdbcTemplate.queryForList("select ? from dual", ps -> {
                new SqlXmlAsInputStreamTypeHandler().setParameter(ps, 1, new ByteArrayInputStream("<xml>abc</xml>".getBytes()), JDBCType.SQLXML.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlXmlAsInputStreamTypeHandler().getNullableResult(rs, 1);
            });
            String xmlBody = IOUtils.readToString(dat.get(0), "UTF-8");
            assert xmlBody.equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlForInputStreamTypeHandler_4() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preProc(jdbcTemplate);

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_xmltype(?)}",//
                    SqlArg.asOut("out", JDBCType.SQLXML.getVendorTypeNumber(), new SqlXmlAsInputStreamTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof InputStream;
            String xmlBody = IOUtils.readToString((InputStream) objectMap.get("out"), "UTF-8");
            assert xmlBody.equals("<xml>abc</xml>");
            assert objectMap.get("#update-count-1").equals(-1);// in oracle ,no more result is -1
        }
    }

    @Test
    public void testSqlXmlForReaderTypeHandler_1() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values ('<xml>abc</xml>')");
            List<Reader> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlAsReaderTypeHandler().getResult(rs, 1);
            });
            String xmlBody = IOUtils.readToString(dat.get(0));
            assert xmlBody.equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlForReaderTypeHandler_2() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values ('<xml>abc</xml>')");
            List<Reader> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlAsReaderTypeHandler().getResult(rs, "c_xml");
            });
            String xmlBody = IOUtils.readToString(dat.get(0));
            assert xmlBody.equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlForReaderTypeHandler_3() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preTable(jdbcTemplate);

            jdbcTemplate.executeUpdate("insert into tb_oracle_types_onlyxml (c_xml) values (?)", ps -> {
                new SqlXmlAsReaderTypeHandler().setParameter(ps, 1, new StringReader("<xml>abc</xml>"), JDBCType.SQLXML.getVendorTypeNumber());
            });

            List<Reader> dat = jdbcTemplate.queryForList("select c_xml from tb_oracle_types_onlyxml where c_xml is not null", (rs, rowNum) -> {
                return new SqlXmlAsReaderTypeHandler().getResult(rs, "c_xml");
            });
            String xmlBody = IOUtils.readToString(dat.get(0));
            assert xmlBody.equals("<xml>abc</xml>");
        }
    }

    @Test
    public void testSqlXmlForReaderTypeHandler_4() throws Exception {
        try (Connection c = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            preProc(jdbcTemplate);

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_xmltype(?)}",//
                    SqlArg.asOut("out", JDBCType.SQLXML.getVendorTypeNumber(), new SqlXmlAsReaderTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Reader;
            String xmlBody = IOUtils.readToString((Reader) objectMap.get("out"));
            assert xmlBody.equals("<xml>abc</xml>");
            assert objectMap.get("#update-count-1").equals(-1);// in oracle ,no more result is -1
        }
    }
}
