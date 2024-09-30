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
import net.hasor.dbvisitor.types.handler.IntegerAsMonthDayTypeHandler;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsMonthDayTypeHandler;
import net.hasor.dbvisitor.types.handler.StringAsMonthDayTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MonthDayTypeTest {
    @Test
    public void testMonthDayOfNumberTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_integer) values (0223);");
            List<MonthDay> dat = jdbcTemplate.queryForList("select c_integer from tb_h2_types where c_integer is not null limit 1;", (rs, rowNum) -> {
                return new IntegerAsMonthDayTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).getMonth() == Month.FEBRUARY;
            assert dat.get(0).getDayOfMonth() == 23;
        }
    }

    @Test
    public void testMonthDayOfNumberTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_integer) values (0223);");
            List<MonthDay> dat = jdbcTemplate.queryForList("select c_integer from tb_h2_types where c_integer is not null limit 1;", (rs, rowNum) -> {
                return new IntegerAsMonthDayTypeHandler().getResult(rs, "c_integer");
            });
            assert dat.get(0).getMonth() == Month.FEBRUARY;
            assert dat.get(0).getDayOfMonth() == 23;
        }
    }

    @Test
    public void testMonthDayOfNumberTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            YearMonth dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { YearMonth.of(2008, 2) }, YearMonth.class);
            assert dat1.getYear() == 2008;
            assert dat1.getMonth() == Month.FEBRUARY;

            List<MonthDay> dat2 = jdbcTemplate.queryForList("select ?", ps -> {
                new IntegerAsMonthDayTypeHandler().setParameter(ps, 1, MonthDay.of(8, 2), JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new IntegerAsMonthDayTypeHandler().getNullableResult(rs, 1);
            });
            assert dat2.get(0).getMonth() == Month.AUGUST;
            assert dat2.get(0).getDayOfMonth() == 2;
        }
    }

    @Test
    public void testMonthDayOfNumberTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_integer;");
            jdbcTemplate.execute("create procedure proc_integer(out p_out integer) begin set p_out=1112; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_integer(?)}",//
                    SqlArg.asOut("out", JDBCType.INTEGER.getVendorTypeNumber(), new IntegerAsMonthDayTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof MonthDay;
            MonthDay yearMonth = (MonthDay) objectMap.get("out");
            assert yearMonth.getMonth() == Month.NOVEMBER;
            assert yearMonth.getDayOfMonth() == 12;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testMonthDayOfStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('08-01');");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('09-03');");
            List<MonthDay> dat = jdbcTemplate.queryForList("select c_varchar from tb_h2_types where c_varchar is not null limit 2;", (rs, rowNum) -> {
                return new StringAsMonthDayTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).getMonth() == Month.AUGUST;
            assert dat.get(0).getDayOfMonth() == 1;
            assert dat.get(1).getMonth() == Month.SEPTEMBER;
            assert dat.get(1).getDayOfMonth() == 3;
        }
    }

    @Test
    public void testMonthDayOfStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('08-01');");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('09-03');");
            List<MonthDay> dat = jdbcTemplate.queryForList("select c_varchar from tb_h2_types where c_varchar is not null limit 2;", (rs, rowNum) -> {
                return new StringAsMonthDayTypeHandler().getResult(rs, "c_varchar");
            });
            assert dat.get(0).getMonth() == Month.AUGUST;
            assert dat.get(0).getDayOfMonth() == 1;
            assert dat.get(1).getMonth() == Month.SEPTEMBER;
            assert dat.get(1).getDayOfMonth() == 3;
        }
    }

    @Test
    public void testMonthDayOfStringTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            MonthDay dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "05-01" }, MonthDay.class);
            assert dat1.getMonth() == Month.MAY;
            assert dat1.getDayOfMonth() == 1;
            MonthDay dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { "12-31" }, MonthDay.class);
            assert dat2.getMonth() == Month.DECEMBER;
            assert dat2.getDayOfMonth() == 31;

            List<MonthDay> dat3 = jdbcTemplate.queryForList("select ?", ps -> {
                new StringAsMonthDayTypeHandler().setParameter(ps, 1, MonthDay.of(Month.FEBRUARY, 26), JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new StringAsMonthDayTypeHandler().getNullableResult(rs, 1);
            });
            assert dat3.get(0).getMonth() == Month.FEBRUARY;
            assert dat3.get(0).getDayOfMonth() == 26;
        }
    }

    @Test
    public void testMonthDayOfStringTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(10)) begin set p_out='11-12'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    SqlArg.asOut("out", JDBCType.VARCHAR.getVendorTypeNumber(), new StringAsMonthDayTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof MonthDay;
            MonthDay yearMonth = (MonthDay) objectMap.get("out");
            assert yearMonth.getMonth() == Month.NOVEMBER;
            assert yearMonth.getDayOfMonth() == 12;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testMonthDayOfTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<MonthDay> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsMonthDayTypeHandler().getResult(rs, 1);
            });
            MonthDay monthDay = MonthDay.now();
            assert dat.get(0).getMonth() == monthDay.getMonth();
            assert dat.get(0).getDayOfMonth() == monthDay.getDayOfMonth();
        }
    }

    @Test
    public void testMonthDayOfTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<MonthDay> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsMonthDayTypeHandler().getResult(rs, "c_timestamp");
            });
            MonthDay monthDay = MonthDay.now();
            assert dat.get(0).getMonth() == monthDay.getMonth();
            assert dat.get(0).getDayOfMonth() == monthDay.getDayOfMonth();
        }
    }

    @Test
    public void testMonthDayOfTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            MonthDay dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { new Date() }, MonthDay.class);
            MonthDay monthDay = MonthDay.now();
            assert dat1.getMonth() == monthDay.getMonth();
            assert dat1.getDayOfMonth() == monthDay.getDayOfMonth();

            List<MonthDay> dat2 = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsMonthDayTypeHandler().setParameter(ps, 1, MonthDay.of(Month.APRIL, 23), JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsMonthDayTypeHandler().getNullableResult(rs, 1);
            });
            assert dat2.get(0).getMonth() == Month.APRIL;
            assert dat2.get(0).getDayOfMonth() == 23;

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", ps -> {
                new SqlTimestampAsMonthDayTypeHandler().setParameter(ps, 1, MonthDay.of(Month.APRIL, 23), JDBCType.TIMESTAMP.getVendorTypeNumber());
            });
            MonthDay dat3 = jdbcTemplate.queryForObject("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", MonthDay.class);
            assert dat3.getMonth() == Month.APRIL;
            assert dat3.getDayOfMonth() == 23;
        }
    }

    @Test
    public void testMonthDayOfTimeTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsMonthDayTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof MonthDay;
            MonthDay yearMonth = (MonthDay) objectMap.get("out");
            assert yearMonth.getMonth() == Month.AUGUST;
            assert yearMonth.getDayOfMonth() == 9;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
