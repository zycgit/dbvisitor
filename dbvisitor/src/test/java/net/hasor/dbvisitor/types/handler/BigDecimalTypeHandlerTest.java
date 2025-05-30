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
import net.hasor.dbvisitor.types.handler.number.BigDecimalTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BigDecimalTypeHandlerTest {
    @Test
    public void testBigDecimalTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_numeric_10) values (1234567890.1234567890);");
            List<BigDecimal> dat1 = jdbcTemplate.queryForList("select c_numeric_10 from tb_h2_types where c_numeric_10 is not null limit 1;", (rs, rowNum) -> {
                return new BigDecimalTypeHandler().getResult(rs, 1);
            });
            assert dat1.get(0).toString().equals("1234567890.1234567890");

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_numeric_2) values (1234567890.1234567890);");
            List<BigDecimal> dat2 = jdbcTemplate.queryForList("select c_numeric_2 from tb_h2_types where c_numeric_2 is not null limit 1;", (rs, rowNum) -> {
                return new BigDecimalTypeHandler().getResult(rs, 1);
            });
            assert dat2.get(0).toString().equals("1234567890.12");
        }
    }

    @Test
    public void testBigDecimalTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_numeric_10) values (1234567890.1234567890);");
            List<BigDecimal> dat1 = jdbcTemplate.queryForList("select c_numeric_10 from tb_h2_types where c_numeric_10 is not null limit 1;", (rs, rowNum) -> {
                return new BigDecimalTypeHandler().getResult(rs, "c_numeric_10");
            });
            assert dat1.get(0).toString().equals("1234567890.1234567890");

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_numeric_2) values (1234567890.1234567890);");
            List<BigDecimal> dat2 = jdbcTemplate.queryForList("select c_numeric_2 from tb_h2_types where c_numeric_2 is not null limit 1;", (rs, rowNum) -> {
                return new BigDecimalTypeHandler().getResult(rs, "c_numeric_2");
            });
            assert dat2.get(0).toString().equals("1234567890.12");
        }
    }

    @Test
    public void testBigDecimalTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<BigDecimal> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new BigDecimalTypeHandler().setParameter(ps, 1, new BigDecimal("1234567890.1234567890"), JDBCType.DECIMAL.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BigDecimalTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).toString().equals("1234567890.1234567890");
        }
    }

    @Test
    public void testBigDecimalTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_decimal;");
            jdbcTemplate.execute("create procedure proc_decimal(out p_out decimal(10,2)) begin set p_out=123.123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_decimal(?)}",//
                    SqlArg.asOut("out", JDBCType.NUMERIC.getVendorTypeNumber(), new BigDecimalTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof BigDecimal;
            assert objectMap.get("out").equals(new BigDecimal("123.12"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
