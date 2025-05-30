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
import net.hasor.dbvisitor.types.handler.time.LocalDateTimeAsLocalDateTypeHandler;
import net.hasor.dbvisitor.types.handler.time.LocalDateTimeTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsLocalTimeTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

public class LocalTimeTypeHandlerTest {
    @Test
    public void testLocalDateTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<LocalDateTime> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new LocalDateTimeTypeHandler().getResult(rs, 1);
            });

            LocalDateTime localNow = LocalDateTime.now();
            LocalDateTime dateTime = dat.get(0);
            assert dateTime.getMonth() == localNow.getMonth();
            assert dateTime.getDayOfMonth() == localNow.getDayOfMonth();
            assert dateTime.getHour() == localNow.getHour();
            assert dateTime.getMinute() == localNow.getMinute();
            assert dateTime.getSecond() == localNow.getSecond()  //
                    || (dateTime.getSecond() + 1) == localNow.getSecond(); // UnitTest cross the seconds
        }
    }

    @Test
    public void testLocalDateTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<LocalDateTime> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new LocalDateTimeTypeHandler().getResult(rs, "c_timestamp");
            });

            LocalDateTime localNow = LocalDateTime.now();
            LocalDateTime dateTime = dat.get(0);
            assert dateTime.getMonth() == localNow.getMonth();
            assert dateTime.getDayOfMonth() == localNow.getDayOfMonth();
            assert dateTime.getHour() == localNow.getHour();
            assert dateTime.getMinute() == localNow.getMinute();
            assert dateTime.getSecond() == localNow.getSecond()  //
                    || (dateTime.getSecond() + 1) == localNow.getSecond(); // UnitTest cross the seconds
        }
    }

    @Test
    public void testLocalDateTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            LocalDateTime testData = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123);
            List<LocalDateTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new LocalDateTimeTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new LocalDateTimeTypeHandler().getNullableResult(rs, 1);
            });

            LocalDateTime dateTime = dat.get(0);
            assert dateTime.getMonth() == testData.getMonth();
            assert dateTime.getDayOfMonth() == testData.getDayOfMonth();
            assert dateTime.getHour() == testData.getHour();
            assert dateTime.getMinute() == testData.getMinute();
            assert dateTime.getSecond() == testData.getSecond();
            assert dateTime.getNano() == testData.getNano();
        }
    }

    @Test
    public void testLocalDateTimeTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new LocalDateTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof LocalDateTime;
            assert objectMap.get("#update-count-1").equals(0);

            LocalDateTime dateTime = (LocalDateTime) objectMap.get("out");
            assert dateTime.getYear() == 2008;
            assert dateTime.getMonth() == Month.AUGUST;
            assert dateTime.getDayOfMonth() == 9;
            assert dateTime.getHour() == 10;
            assert dateTime.getMinute() == 11;
            assert dateTime.getSecond() == 12;
            assert dateTime.getNano() == 0;
        }
    }

    @Test
    public void testLocalDateTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<LocalDate> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new LocalDateTimeAsLocalDateTypeHandler().getResult(rs, 1);
            });

            LocalDate localNow = LocalDate.now();
            LocalDate dateTime = dat.get(0);
            assert dateTime.getYear() == localNow.getYear();
            assert dateTime.getMonth() == localNow.getMonth();
            assert dateTime.getDayOfMonth() == localNow.getDayOfMonth();
        }
    }

    @Test
    public void testLocalDateTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<LocalDate> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new LocalDateTimeAsLocalDateTypeHandler().getResult(rs, "c_timestamp");
            });

            LocalDate localNow = LocalDate.now();
            LocalDate dateTime = dat.get(0);
            assert dateTime.getYear() == localNow.getYear();
            assert dateTime.getMonth() == localNow.getMonth();
            assert dateTime.getDayOfMonth() == localNow.getDayOfMonth();
        }
    }

    @Test
    public void testLocalDateTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            LocalDate testData = LocalDate.of(1998, Month.APRIL, 12);
            List<LocalDate> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new LocalDateTimeAsLocalDateTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new LocalDateTimeAsLocalDateTypeHandler().getNullableResult(rs, 1);
            });

            LocalDate dateTime = dat.get(0);
            assert dateTime.getYear() == testData.getYear();
            assert dateTime.getMonth() == testData.getMonth();
            assert dateTime.getDayOfMonth() == testData.getDayOfMonth();
        }
    }

    @Test
    public void testLocalDateTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new LocalDateTimeAsLocalDateTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof LocalDate;
            assert objectMap.get("#update-count-1").equals(0);

            LocalDate dateTime = (LocalDate) objectMap.get("out");
            assert dateTime.getYear() == 2008;
            assert dateTime.getMonth() == Month.AUGUST;
            assert dateTime.getDayOfMonth() == 9;
        }
    }

    @Test
    public void testLocalTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<LocalTime> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsLocalTimeTypeHandler().getResult(rs, 1);
            });

            LocalTime localNow = LocalTime.now();
            LocalTime dateTime = dat.get(0);
            assert dateTime.getHour() == localNow.getHour();
            assert dateTime.getMinute() == localNow.getMinute();
            assert dateTime.getSecond() == localNow.getSecond()  //
                    || (dateTime.getSecond() + 1) == localNow.getSecond(); // UnitTest cross the seconds
        }
    }

    @Test
    public void testLocalTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<LocalTime> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsLocalTimeTypeHandler().getResult(rs, "c_timestamp");
            });

            LocalTime localNow = LocalTime.now();
            LocalTime dateTime = dat.get(0);
            assert dateTime.getHour() == localNow.getHour();
            assert dateTime.getMinute() == localNow.getMinute();
            assert dateTime.getSecond() == localNow.getSecond()  //
                    || (dateTime.getSecond() + 1) == localNow.getSecond(); // UnitTest cross the seconds
        }
    }

    @Test
    public void testLocalTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            LocalTime testData = LocalTime.of(12, 33, 45, 1243);
            List<LocalTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsLocalTimeTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsLocalTimeTypeHandler().getNullableResult(rs, 1);
            });

            LocalTime dateTime = dat.get(0);
            assert dateTime.getHour() == testData.getHour();
            assert dateTime.getMinute() == testData.getMinute();
            assert dateTime.getSecond() == testData.getSecond();
            assert dateTime.getNano() == testData.getNano();
        }
    }

    @Test
    public void testLocalTimeTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsLocalTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof LocalTime;
            assert objectMap.get("#update-count-1").equals(0);

            LocalTime dateTime = (LocalTime) objectMap.get("out");
            assert dateTime.getHour() == 10;
            assert dateTime.getMinute() == 11;
            assert dateTime.getSecond() == 12;
            assert dateTime.getNano() == 0;
        }
    }
}
