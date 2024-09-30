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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.handler.BooleanTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BooleanTypeTest {
    @Test
    public void testBooleanTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_boolean) values (true);");
            List<Boolean> dat = jdbcTemplate.queryForList("select c_boolean from tb_h2_types where c_boolean is not null limit 1;", (rs, rowNum) -> {
                return new BooleanTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0);
        }
    }

    @Test
    public void testBooleanTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_boolean) values (true);");
            List<Boolean> dat = jdbcTemplate.queryForList("select c_boolean from tb_h2_types where c_boolean is not null limit 1;", (rs, rowNum) -> {
                return new BooleanTypeHandler().getResult(rs, "c_boolean");
            });
            assert dat.get(0);
        }
    }

    @Test
    public void testBooleanTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            boolean dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { true }, boolean.class);
            Boolean dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { true }, Boolean.class);
            boolean dat3 = jdbcTemplate.queryForObject("select ?", new Object[] { false }, boolean.class);
            Boolean dat4 = jdbcTemplate.queryForObject("select ?", new Object[] { false }, Boolean.class);
            assert dat1;
            assert dat2;
            assert !dat3;
            assert !dat4;

            List<Boolean> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new BooleanTypeHandler().setParameter(ps, 1, true, JDBCType.BOOLEAN.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BooleanTypeHandler().getNullableResult(rs, 1);
            });
            assert dat.get(0);
        }
    }

    @Test
    public void testBooleanTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_boolean;");
            jdbcTemplate.execute("create procedure proc_boolean(out p_out boolean) begin set p_out=true; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_boolean(?)}",//
                    SqlArg.asOut("out", JDBCType.BOOLEAN.getVendorTypeNumber(), new BooleanTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Boolean;
            assert objectMap.get("out").equals(true);
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
