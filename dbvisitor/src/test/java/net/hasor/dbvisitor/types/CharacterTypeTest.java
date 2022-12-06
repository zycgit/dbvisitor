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
package net.hasor.dbvisitor.types;
import net.hasor.dbvisitor.jdbc.SqlParameterUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.handler.CharacterTypeHandler;
import net.hasor.dbvisitor.types.handler.NCharacterTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CharacterTypeTest {
    @Test
    public void testCharacterTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char) values ('1234567890');");
            List<Character> bigInteger = jdbcTemplate.queryForList("select c_char from tb_h2_types where c_char is not null limit 1;", (rs, rowNum) -> {
                return new CharacterTypeHandler().getResult(rs, 1);
            });
            assert bigInteger.get(0).toString().equals("1");
        }
    }

    @Test
    public void testCharacterTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char) values ('1234567890');");
            List<Character> bigInteger = jdbcTemplate.queryForList("select c_char from tb_h2_types where c_char is not null limit 1;", (rs, rowNum) -> {
                return new CharacterTypeHandler().getResult(rs, "c_char");
            });
            assert bigInteger.get(0).toString().equals("1");
        }
    }

    @Test
    public void testCharacterTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            char dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "abc" }, char.class);
            Character dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { "abc" }, Character.class);
            assert dat1 == 'a';
            assert dat2 == 'a';

            List<Character> character1 = jdbcTemplate.queryForList("select ?", ps -> {
                new CharacterTypeHandler().setParameter(ps, 1, 'a', JDBCType.CHAR.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new CharacterTypeHandler().getNullableResult(rs, 1);
            });
            assert character1.get(0) == 'a';

            List<Character> character2 = jdbcTemplate.queryForList("select ? as ncr", ps -> {
                new CharacterTypeHandler().setParameter(ps, 1, 'a', JDBCType.CHAR.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new CharacterTypeHandler().getNullableResult(rs, "ncr");
            });
            assert character2.get(0) == 'a';
        }
    }

    @Test
    public void testCharacterTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_char;");
            jdbcTemplate.execute("create procedure proc_char(out p_out char) begin set p_out='A'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_char(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.CHAR.getVendorTypeNumber(), new CharacterTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Character;
            assert objectMap.get("out").equals('A');
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testNCharacterTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char) values ('1234567890');");
            List<Character> bigInteger = jdbcTemplate.queryForList("select c_char from tb_h2_types where c_char is not null limit 1;", (rs, rowNum) -> {
                return new NCharacterTypeHandler().getResult(rs, 1);
            });
            assert bigInteger.get(0).toString().equals("1");
        }
    }

    @Test
    public void testNCharacterTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_char) values ('1234567890');");
            List<Character> bigInteger = jdbcTemplate.queryForList("select c_char from tb_h2_types where c_char is not null limit 1;", (rs, rowNum) -> {
                return new NCharacterTypeHandler().getResult(rs, "c_char");
            });
            assert bigInteger.get(0).toString().equals("1");
        }
    }

    @Test
    public void testNCharacterTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            char dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "abc" }, char.class);
            Character dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { "abc" }, Character.class);
            assert dat1 == 'a';
            assert dat2 == 'a';

            List<Character> character1 = jdbcTemplate.queryForList("select ?", ps -> {
                new NCharacterTypeHandler().setParameter(ps, 1, 'a', JDBCType.NCHAR.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new NCharacterTypeHandler().getNullableResult(rs, 1);
            });
            assert character1.get(0) == 'a';

            List<Character> character2 = jdbcTemplate.queryForList("select ? as ncr", ps -> {
                new NCharacterTypeHandler().setParameter(ps, 1, 'a', JDBCType.NCHAR.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new NCharacterTypeHandler().getNullableResult(rs, "ncr");
            });
            assert character2.get(0) == 'a';
        }
    }

    @Test
    public void testNCharacterTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_char;");
            jdbcTemplate.execute("create procedure proc_char(out p_out char) begin set p_out='A'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_char(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.NCHAR.getVendorTypeNumber(), new NCharacterTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Character;
            assert objectMap.get("out").equals('A');
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
