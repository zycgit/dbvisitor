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
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.Map;

public class OffsetTimeTypeHandlerTest {
    @Test
    public void testOffsetDateTimeForSqlTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetDateTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new OffsetDateTimeTypeHandler().getResult(rs, 1);
            });

            OffsetDateTime dateTime = dat.get(0);
            OffsetDateTime testData = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8));

            assert dateTime.getOffset().getId().equals(testData.getOffset().getId());
            assert dateTime.getYear() == testData.getYear();
            assert dateTime.getMonth() == testData.getMonth();
            assert dateTime.getDayOfMonth() == testData.getDayOfMonth();
            assert dateTime.getHour() == testData.getHour();
            assert dateTime.getMinute() == testData.getMinute();
            assert dateTime.getSecond() == testData.getSecond();
            assert dateTime.getNano() == testData.getNano();
        }
    }

    @Test
    public void testOffsetDateTimeForSqlTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetDateTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new OffsetDateTimeTypeHandler().getResult(rs, "c_timestamptz");
            });

            OffsetDateTime dateTime = dat.get(0);
            OffsetDateTime testData = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8));

            assert dateTime.getOffset().getId().equals(testData.getOffset().getId());
            assert dateTime.getYear() == testData.getYear();
            assert dateTime.getMonth() == testData.getMonth();
            assert dateTime.getDayOfMonth() == testData.getDayOfMonth();
            assert dateTime.getHour() == testData.getHour();
            assert dateTime.getMinute() == testData.getMinute();
            assert dateTime.getSecond() == testData.getSecond();
            assert dateTime.getNano() == testData.getNano();
        }
    }

    @Test
    public void testOffsetDateTimeForSqlTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            OffsetDateTime testData = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8));

            List<OffsetDateTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new OffsetDateTimeTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new OffsetDateTimeTypeHandler().getNullableResult(rs, 1);
            });
            OffsetDateTime dateTime = dat.get(0);

            assert dateTime.getOffset().getId().equals(testData.getOffset().getId());
            assert dateTime.getYear() == testData.getYear();
            assert dateTime.getMonth() == testData.getMonth();
            assert dateTime.getDayOfMonth() == testData.getDayOfMonth();
            assert dateTime.getHour() == testData.getHour();
            assert dateTime.getMinute() == testData.getMinute();
            assert dateTime.getSecond() == testData.getSecond();
            assert dateTime.getNano() == testData.getNano();
        }
    }

    @Test
    public void testOffsetDateTimeForSqlTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute(""//
                    + "create or replace procedure proc_timestamptz(p_out out timestamp with time zone)\n" //
                    + "AS\n" //
                    + "BEGIN\n"//
                    + "  p_out := to_timestamp_tz('2013-10-15T17:18:28-06:00','YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM');\n" //
                    + "END;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamptz(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber(), new OffsetDateTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof OffsetDateTime;
            assert objectMap.get("out").toString().equals("2013-10-15T17:18:28-06:00");
        }
    }

    @Test
    public void testOffsetDateTimeForUTCTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetDateTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsUTCOffsetDateTimeTypeHandler().getResult(rs, 1);
            });
            OffsetDateTime dateTime = dat.get(0);
            ZonedDateTime utcTime = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8))//
                    .atZoneSameInstant(ZoneOffset.UTC);

            assert dateTime.getOffset().getId().equals(utcTime.getOffset().getId());
            assert dateTime.getYear() == utcTime.getYear();
            assert dateTime.getMonth() == utcTime.getMonth();
            assert dateTime.getDayOfMonth() == utcTime.getDayOfMonth();
            assert dateTime.getHour() == utcTime.getHour();
            assert dateTime.getMinute() == utcTime.getMinute();
            assert dateTime.getSecond() == utcTime.getSecond();
            assert dateTime.getNano() == utcTime.getNano();
        }
    }

    @Test
    public void testOffsetDateTimeForUTCTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetDateTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsUTCOffsetDateTimeTypeHandler().getResult(rs, "c_timestamptz");
            });
            OffsetDateTime dateTime = dat.get(0);
            ZonedDateTime utcTime = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8))//
                    .atZoneSameInstant(ZoneOffset.UTC);

            assert dateTime.getOffset().getId().equals(utcTime.getOffset().getId());
            assert dateTime.getYear() == utcTime.getYear();
            assert dateTime.getMonth() == utcTime.getMonth();
            assert dateTime.getDayOfMonth() == utcTime.getDayOfMonth();
            assert dateTime.getHour() == utcTime.getHour();
            assert dateTime.getMinute() == utcTime.getMinute();
            assert dateTime.getSecond() == utcTime.getSecond();
            assert dateTime.getNano() == utcTime.getNano();
        }
    }

    @Test
    public void testOffsetDateTimeForUTCTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            OffsetDateTime argOffsetTime = LocalDateTime.of(1998, Month.APRIL, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8));

            List<OffsetDateTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsUTCOffsetDateTimeTypeHandler().setParameter(ps, 1, argOffsetTime, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsUTCOffsetDateTimeTypeHandler().getNullableResult(rs, 1);
            });
            OffsetDateTime dateTime = dat.get(0);
            ZonedDateTime testTime = argOffsetTime.atZoneSameInstant(ZoneOffset.UTC);

            assert dateTime.getOffset().getId().equals(testTime.getOffset().getId());
            assert dateTime.getYear() == testTime.getYear();
            assert dateTime.getMonth() == testTime.getMonth();
            assert dateTime.getDayOfMonth() == testTime.getDayOfMonth();
            assert dateTime.getHour() == testTime.getHour();
            assert dateTime.getMinute() == testTime.getMinute();
            assert dateTime.getSecond() == testTime.getSecond();
            assert dateTime.getNano() == testTime.getNano();
        }
    }

    @Test
    public void testOffsetDateTimeForUTCTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 08:09:30', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsUTCOffsetDateTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof OffsetDateTime;
            ZonedDateTime testTime = LocalDateTime.of(2008, Month.AUGUST, 9, 8, 9, 30)//
                    .atOffset(ZoneOffset.ofHours(8))//
                    .atZoneSameInstant(ZoneOffset.UTC);
            OffsetDateTime dateTime = (OffsetDateTime) objectMap.get("out");
            assert dateTime.getOffset().getId().equals(testTime.getOffset().getId());
            assert dateTime.getYear() == testTime.getYear();
            assert dateTime.getMonth() == testTime.getMonth();
            assert dateTime.getDayOfMonth() == testTime.getDayOfMonth();
            assert dateTime.getHour() == testTime.getHour();
            assert dateTime.getMinute() == testTime.getMinute();
            assert dateTime.getSecond() == testTime.getSecond();
            assert dateTime.getNano() == testTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForSqlTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new OffsetTimeTypeHandler().getResult(rs, 1);
            });
            OffsetTime dateTime = dat.get(0);
            OffsetTime localTime = LocalTime.of(18, 33, 20, 123).atOffset(ZoneOffset.ofHours(8));

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForSqlTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new OffsetTimeTypeHandler().getResult(rs, "c_timestamptz");
            });
            OffsetTime dateTime = dat.get(0);
            OffsetTime localTime = LocalTime.of(18, 33, 20, 123).atOffset(ZoneOffset.ofHours(8));

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForSqlTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            OffsetTime localTime = LocalTime.of(18, 33, 20, 123).atOffset(ZoneOffset.ofHours(8));

            List<OffsetTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new OffsetTimeTypeHandler().setParameter(ps, 1, localTime, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new OffsetTimeTypeHandler().getNullableResult(rs, 1);
            });

            OffsetTime dateTime = dat.get(0);
            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForSqlTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute(""//
                    + "create or replace procedure proc_timestamptz(p_out out timestamp with time zone)\n" //
                    + "AS\n" //
                    + "BEGIN\n"//
                    + "  p_out := to_timestamp_tz('2013-10-15T17:18:28-06:00','YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM');\n" //
                    + "END;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamptz(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber(), new OffsetTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof OffsetTime;
            assert objectMap.get("out").toString().equals("17:18:28-06:00");
        }
    }

    @Test
    public void testOffsetTimeForUTCTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsUTCOffsetTimeTypeHandler().getResult(rs, 1);
            });
            OffsetTime dateTime = dat.get(0);
            OffsetTime localTime = LocalTime.of(18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8))    //
                    .atDate(LocalDate.ofEpochDay(0))    //
                    .atZoneSameInstant(ZoneOffset.UTC)  //
                    .toOffsetDateTime().toOffsetTime();

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForUTCTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<OffsetTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsUTCOffsetTimeTypeHandler().getResult(rs, "c_timestamptz");
            });
            OffsetTime dateTime = dat.get(0);
            OffsetTime localTime = LocalTime.of(18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8))    //
                    .atDate(LocalDate.ofEpochDay(0))    //
                    .atZoneSameInstant(ZoneOffset.UTC)  //
                    .toOffsetDateTime().toOffsetTime();

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForUTCTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            OffsetTime localTime = LocalTime.of(18, 33, 20, 123).atOffset(ZoneOffset.ofHours(8));//

            List<OffsetTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsUTCOffsetTimeTypeHandler().setParameter(ps, 1, localTime, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsUTCOffsetTimeTypeHandler().getNullableResult(rs, 1);
            });

            OffsetTime dateTime = dat.get(0);
            OffsetTime testTime = localTime.atDate(LocalDate.ofEpochDay(0))    //
                    .atZoneSameInstant(ZoneOffset.UTC)  //
                    .toOffsetDateTime().toOffsetTime();

            assert dateTime.getOffset().getId().equals(testTime.getOffset().getId());
            assert dateTime.getHour() == testTime.getHour();
            assert dateTime.getMinute() == testTime.getMinute();
            assert dateTime.getSecond() == testTime.getSecond();
            assert dateTime.getNano() == testTime.getNano();
        }
    }

    @Test
    public void testOffsetTimeForUTCTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 08:09:30', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsUTCOffsetTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof OffsetTime;
            ZonedDateTime testTime = LocalDateTime.of(2008, Month.AUGUST, 9, 8, 9, 30)//
                    .atOffset(ZoneOffset.ofHours(8))//
                    .atZoneSameInstant(ZoneOffset.UTC);
            OffsetTime dateTime = (OffsetTime) objectMap.get("out");
            assert dateTime.getOffset().getId().equals(testTime.getOffset().getId());
            assert dateTime.getHour() == testTime.getHour();
            assert dateTime.getMinute() == testTime.getMinute();
            assert dateTime.getSecond() == testTime.getSecond();
            assert dateTime.getNano() == testTime.getNano();
        }
    }

    @Test
    public void testZonedDateTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<ZonedDateTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new OffsetDateTimeAsZonedDateTimeTypeHandler().getResult(rs, 1);
            });
            ZonedDateTime dateTime = dat.get(0);
            OffsetDateTime localTime = LocalDateTime.of(1998, 4, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8));

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testZonedDateTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamptz) values ('1998-04-12T18:33:20.000000123+08:00');");
            List<ZonedDateTime> dat = jdbcTemplate.queryForList("select c_timestamptz from tb_h2_types where c_timestamptz is not null limit 1;", (rs, rowNum) -> {
                return new OffsetDateTimeAsZonedDateTimeTypeHandler().getResult(rs, "c_timestamptz");
            });
            ZonedDateTime dateTime = dat.get(0);
            OffsetDateTime localTime = LocalDateTime.of(1998, 4, 12, 18, 33, 20, 123)//
                    .atOffset(ZoneOffset.ofHours(8));

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testZonedDateTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            ZonedDateTime localTime = ZonedDateTime.of(//
                    LocalDate.of(1998, 4, 12),//
                    LocalTime.of(18, 33, 20, 123),//
                    ZoneOffset.ofHours(8));

            List<ZonedDateTime> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new OffsetDateTimeAsZonedDateTimeTypeHandler().setParameter(ps, 1, localTime, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new OffsetDateTimeAsZonedDateTimeTypeHandler().getNullableResult(rs, 1);
            });

            ZonedDateTime dateTime = dat.get(0);

            assert dateTime.getOffset().getId().equals(localTime.getOffset().getId());
            assert dateTime.getHour() == localTime.getHour();
            assert dateTime.getMinute() == localTime.getMinute();
            assert dateTime.getSecond() == localTime.getSecond();
            assert dateTime.getNano() == localTime.getNano();
        }
    }

    @Test
    public void testZonedDateTimeTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.oracleConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute(""//
                    + "create or replace procedure proc_timestamptz(p_out out timestamp with time zone)\n" //
                    + "AS\n" //
                    + "BEGIN\n"//
                    + "  p_out := to_timestamp_tz('2013-10-15T17:18:28-06:00','YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM');\n" //
                    + "END;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamptz(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber(), new OffsetDateTimeAsZonedDateTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof ZonedDateTime;
            assert objectMap.get("out").toString().equals("2013-10-15T17:18:28-06:00");
        }
    }
}
