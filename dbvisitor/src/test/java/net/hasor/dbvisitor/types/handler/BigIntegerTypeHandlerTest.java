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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.BigIntegerTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class BigIntegerTypeHandlerTest {
    @Test
    public void testBigIntegerTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_bigint) values (1234567890);");
            List<BigInteger> dat = jdbcTemplate.queryForList("select c_bigint from tb_h2_types where c_bigint is not null limit 1;", (rs, rowNum) -> {
                return new BigIntegerTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).toString().equals("1234567890");
        }
    }

    @Test
    public void testBigIntegerTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_bigint) values (1234567890);");
            List<BigInteger> dat = jdbcTemplate.queryForList("select c_bigint from tb_h2_types where c_bigint is not null limit 1;", (rs, rowNum) -> {
                return new BigIntegerTypeHandler().getResult(rs, "c_bigint");
            });
            assert dat.get(0).toString().equals("1234567890");
        }
    }

    @Test
    public void testBigIntegerTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<BigInteger> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new BigIntegerTypeHandler().setParameter(ps, 1, new BigInteger("1234567890"), JDBCType.BIGINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BigIntegerTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0).toString().equals("1234567890");
        }
    }

    @Test
    public void testBigIntegerTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bigint;");
            jdbcTemplate.execute("create procedure proc_bigint(out p_out bigint) begin set p_out=123123; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bigint(?)}",//
                    SqlArg.asOut("out", JDBCType.BIGINT.getVendorTypeNumber(), new BigIntegerTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof BigInteger;
            assert objectMap.get("out").equals(new BigInteger("123123"));
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
