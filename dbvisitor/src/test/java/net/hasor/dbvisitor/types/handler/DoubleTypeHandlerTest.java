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
import net.hasor.dbvisitor.types.handler.number.DoubleTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DoubleTypeHandlerTest {
    @Test
    public void testDoubleTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_double) values (123.123);");
            List<Double> dat = jdbcTemplate.queryForList("select c_double from tb_h2_types where c_double is not null limit 1;", (rs, rowNum) -> {
                return new DoubleTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) == 123.123d;
        }
    }

    @Test
    public void testDoubleTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_double) values (123.123);");
            List<Double> dat = jdbcTemplate.queryForList("select c_double from tb_h2_types where c_double is not null limit 1;", (rs, rowNum) -> {
                return new DoubleTypeHandler().getResult(rs, "c_double");
            });
            assert dat.get(0) == 123.123d;
        }
    }

    @Test
    public void testDoubleTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            double dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { 123.123d }, double.class);
            Double dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { 123.123d }, Double.class);
            assert dat1 == 123.123d;
            assert dat2 == 123.123d;

            List<Double> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new DoubleTypeHandler().setParameter(ps, 1, 123.123d, JDBCType.DOUBLE.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new DoubleTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0) == 123.123d;
        }
    }

    @Test
    public void testCharacterTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_double;");
            jdbcTemplate.execute("create procedure proc_double(out p_out double) begin set p_out=123.123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_double(?)}",//
                    SqlArg.asOut("out", JDBCType.DOUBLE.getVendorTypeNumber(), new DoubleTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Double;
            assert objectMap.get("out").equals(123.123d);
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
