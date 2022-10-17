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
import net.hasor.dbvisitor.types.handler.MonthOfNumberTypeHandler;
import net.hasor.dbvisitor.types.handler.MonthOfStringTypeHandler;
import net.hasor.dbvisitor.types.handler.MonthOfTimeTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;

public class MonthTypeTest {
    @Test
    public void testMonthOfNumberTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_tinyint) values (05);");
            List<Month> dat = jdbcTemplate.query("select c_tinyint from tb_h2_types where c_tinyint is not null limit 1;", (rs, rowNum) -> {
                return new MonthOfNumberTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) == Month.MAY;
        }
    }

    @Test
    public void testMonthOfNumberTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_tinyint) values (05);");
            List<Month> dat = jdbcTemplate.query("select c_tinyint from tb_h2_types where c_tinyint is not null limit 1;", (rs, rowNum) -> {
                return new MonthOfNumberTypeHandler().getResult(rs, "c_tinyint");
            });
            assert dat.get(0) == Month.MAY;
        }
    }

    @Test
    public void testMonthOfNumberTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Month dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { 5 }, Month.class);
            assert dat1 == Month.MAY;

            List<Month> dat2 = jdbcTemplate.query("select ?", ps -> {
                new MonthOfNumberTypeHandler().setParameter(ps, 1, Month.MAY, JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new MonthOfNumberTypeHandler().getNullableResult(rs, 1);
            });
            assert dat2.get(0) == Month.MAY;
        }
    }

    @Test
    public void testMonthOfNumberTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_smallint;");
            jdbcTemplate.execute("create procedure proc_smallint(out p_out smallint) begin set p_out=1; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_smallint(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.SMALLINT.getVendorTypeNumber(), new MonthOfNumberTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Month;
            assert objectMap.get("out") == Month.JANUARY;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testMonthOfStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('05');");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('may');");
            List<Month> dat = jdbcTemplate.query("select c_varchar from tb_h2_types where c_varchar is not null limit 2;", (rs, rowNum) -> {
                return new MonthOfStringTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) == Month.MAY;
            assert dat.get(1) == Month.MAY;
        }
    }

    @Test
    public void testMonthOfStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('05');");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('may');");
            List<Month> dat = jdbcTemplate.query("select c_varchar from tb_h2_types where c_varchar is not null limit 2;", (rs, rowNum) -> {
                return new MonthOfStringTypeHandler().getResult(rs, "c_varchar");
            });
            assert dat.get(0) == Month.MAY;
            assert dat.get(1) == Month.MAY;
        }
    }

    @Test
    public void testMonthOfStringTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Month dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "5" }, Month.class);
            assert dat1 == Month.MAY;
            Month dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { "may" }, Month.class);
            assert dat2 == Month.MAY;

            List<Month> dat3 = jdbcTemplate.query("select ?", ps -> {
                new MonthOfStringTypeHandler().setParameter(ps, 1, Month.MAY, JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new MonthOfStringTypeHandler().getNullableResult(rs, 1);
            });
            assert dat3.get(0) == Month.MAY;
        }
    }

    @Test
    public void testMonthOfStringTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(10)) begin set p_out='may'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARCHAR.getVendorTypeNumber(), new MonthOfStringTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Month;
            assert objectMap.get("out") == Month.MAY;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testMonthOfTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<Month> dat = jdbcTemplate.query("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new MonthOfTimeTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) == YearMonth.now().getMonth();
        }
    }

    @Test
    public void testMonthOfTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<Month> dat = jdbcTemplate.query("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new MonthOfTimeTypeHandler().getResult(rs, "c_timestamp");
            });
            assert dat.get(0) == YearMonth.now().getMonth();
        }
    }

    @Test
    public void testMonthOfTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Month dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { new Date() }, Month.class);
            assert dat1 == YearMonth.now().getMonth();

            List<Month> dat2 = jdbcTemplate.query("select ?", ps -> {
                new MonthOfTimeTypeHandler().setParameter(ps, 1, Month.MAY, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new MonthOfTimeTypeHandler().getNullableResult(rs, 1);
            });
            assert dat2.get(0) == Month.MAY;

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", ps -> {
                new MonthOfTimeTypeHandler().setParameter(ps, 1, Month.MAY, JDBCType.TIMESTAMP.getVendorTypeNumber());
            });
            Date dat = jdbcTemplate.queryForObject("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", Date.class);
            Calendar instance = Calendar.getInstance();
            instance.setTime(dat);
            int month = instance.get(Calendar.MONTH);
            assert month == Calendar.MAY;
        }
    }

    @Test
    public void testMonthOfTimeTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new MonthOfTimeTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Month;
            assert objectMap.get("out") == Month.AUGUST;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
