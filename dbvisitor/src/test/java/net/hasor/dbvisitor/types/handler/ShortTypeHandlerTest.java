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
import net.hasor.dbvisitor.types.handler.number.ShortTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ShortTypeHandlerTest {
    @Test
    public void testShortTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_smallint) values (123);");
            List<Short> dat = jdbcTemplate.queryForList("select c_smallint from tb_h2_types where c_smallint is not null limit 1;", (rs, rowNum) -> {
                return new ShortTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) == 123;
        }
    }

    @Test
    public void testShortTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_smallint) values (123);");
            List<Short> dat = jdbcTemplate.queryForList("select c_smallint from tb_h2_types where c_smallint is not null limit 1;", (rs, rowNum) -> {
                return new ShortTypeHandler().getResult(rs, "c_smallint");
            });
            assert dat.get(0) == 123;
        }
    }

    @Test
    public void testShortTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            short dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { 123 }, short.class);
            Short dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { 123 }, Short.class);
            assert dat1 == 123;
            assert dat2 == 123;

            List<Short> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new ShortTypeHandler().setParameter(ps, 1, (short) 123, JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new ShortTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0) == 123;
        }
    }

    @Test
    public void testShortTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_integer;");
            jdbcTemplate.execute("create procedure proc_integer(out p_out integer) begin set p_out=123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_integer(?)}",//
                    SqlArg.asOut("out", JDBCType.INTEGER.getVendorTypeNumber(), new ShortTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Short;
            assert objectMap.get("out").equals(new Short("123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
