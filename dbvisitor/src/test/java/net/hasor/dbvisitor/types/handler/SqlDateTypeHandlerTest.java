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
import net.hasor.dbvisitor.types.handler.time.SqlDateTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimeTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SqlDateTypeHandlerTest {
    @Test
    public void testSqlTimestampTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampTypeHandler().getResult(rs, 1);
            });

            assert dat.get(0).getTime() == testData.getTime();
        }
    }

    @Test
    public void testSqlTimestampTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampTypeHandler().getResult(rs, "c_timestamp");
            });

            assert dat.get(0).getTime() == testData.getTime();
        }
    }

    @Test
    public void testSqlTimestampTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Timestamp testData = new Timestamp(new Date().getTime());
            List<Date> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampTypeHandler().getNullableResult(rs, 1);
            });

            assert dat.get(0).getTime() == testData.getTime();
        }
    }

    @Test
    public void testSqlTimestampTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Timestamp;
            Date dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-09 10:11:12");
            Timestamp timestamp = (Timestamp) objectMap.get("out");
            assert timestamp.equals(Timestamp.from(dateString.toInstant()));
        }
    }

    @Test
    public void testSqlTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Time> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimeTypeHandler().getResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = dat.get(0).toLocalTime();
            assert t1Time.toNanoOfDay() != t2Time.toNanoOfDay();
            assert t1Time.toSecondOfDay() == t2Time.toSecondOfDay();
        }
    }

    @Test
    public void testSqlTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Time> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimeTypeHandler().getResult(rs, "c_timestamp");
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = dat.get(0).toLocalTime();
            assert t1Time.toNanoOfDay() != t2Time.toNanoOfDay();
            assert t1Time.toSecondOfDay() == t2Time.toSecondOfDay();
        }
    }

    @Test
    public void testSqlTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Time testTime = new Time(new Date().getTime());
            List<Time> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimeTypeHandler().setParameter(ps, 1, testTime, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimeTypeHandler().getNullableResult(rs, 1);
            });

            assert testTime.getTime() != dat.get(0).getTime();
            LocalTime t1Time = testTime.toLocalTime();
            LocalTime t2Time = dat.get(0).toLocalTime();
            assert t1Time.toNanoOfDay() == t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testSqlTimeTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimeTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Time;
            Time timestamp = (Time) objectMap.get("out");
            assert timestamp.equals(Time.valueOf("10:11:12"));
        }
    }

    @Test
    public void testSqlDateTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<java.sql.Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlDateTypeHandler().getResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Date = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Date = dat.get(0).toLocalDate();
            assert t1Date.equals(t2Date);
        }
    }

    @Test
    public void testSqlDateTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<java.sql.Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlDateTypeHandler().getResult(rs, "c_timestamp");
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Date = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Date = dat.get(0).toLocalDate();
            assert t1Date.equals(t2Date);
        }
    }

    @Test
    public void testSqlDateTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            java.sql.Date testData = new java.sql.Date(new Date().getTime());
            List<java.sql.Date> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlDateTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlDateTypeHandler().getNullableResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = testData.toLocalDate();
            LocalDate t2Data = dat.get(0).toLocalDate();
            assert t1Data.toEpochDay() == t2Data.toEpochDay();
        }
    }

    @Test
    public void testSqlDateTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlDateTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof java.sql.Date;
            java.sql.Date timestamp = (java.sql.Date) objectMap.get("out");
            Date dateString = new SimpleDateFormat("yyyy-MM-dd").parse("2008-08-09");
            assert timestamp.getTime() == new java.sql.Date(dateString.getTime()).getTime();
        }
    }
}
