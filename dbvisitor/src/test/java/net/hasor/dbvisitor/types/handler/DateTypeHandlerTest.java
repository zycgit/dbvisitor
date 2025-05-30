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
import net.hasor.dbvisitor.types.handler.time.SqlDateAsDateHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimeAsDateTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsDateTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DateTypeHandlerTest {
    @Test
    public void testDateTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsDateTypeHandler().getResult(rs, 1);
            });

            assert testData.getTime() == dat.get(0).getTime();
        }
    }

    @Test
    public void testDateTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsDateTypeHandler().getResult(rs, "c_timestamp");
            });

            assert testData.getTime() == dat.get(0).getTime();
        }
    }

    @Test
    public void testDateTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            List<Date> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsDateTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsDateTypeHandler().getNullableResult(rs, 1);
            });

            Date dateTime = dat.get(0);
            assert dateTime.getTime() == testData.getTime();
        }
    }

    @Test
    public void testDateTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 08:09:30', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsDateTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Date;
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(objectMap.get("out"));
            assert dateString.equals("2008-08-09 08:09:30");
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testTimeOnlyTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimeAsDateTypeHandler().getResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Data = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalDate();
            assert t1Data.toEpochDay() != t2Data.toEpochDay();

            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalTime();
            assert t1Time.toNanoOfDay() == t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testTimeOnlyTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimeAsDateTypeHandler().getResult(rs, "c_timestamp");
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Data = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalDate();
            assert t1Data.toEpochDay() != t2Data.toEpochDay();

            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalTime();
            assert t1Time.toNanoOfDay() == t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testTimeOnlyTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            List<Date> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimeAsDateTypeHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimeAsDateTypeHandler().getNullableResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Data = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalDate();
            assert t1Data.toEpochDay() != t2Data.toEpochDay();

            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalTime();
            assert t1Time.toNanoOfDay() == t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testTimeOnlyTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 08:09:30', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimeAsDateTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Date;
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(objectMap.get("out"));
            assert dateString.equals("1970-01-01 08:09:30");
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testDateOnlyTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlDateAsDateHandler().getResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Data = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalDate();
            assert t1Data.toEpochDay() == t2Data.toEpochDay();

            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalTime();
            assert t1Time.toNanoOfDay() != t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testDateOnlyTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Date> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlDateAsDateHandler().getResult(rs, "c_timestamp");
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Data = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalDate();
            assert t1Data.toEpochDay() == t2Data.toEpochDay();

            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalTime();
            assert t1Time.toNanoOfDay() != t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testDateOnlyTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            List<Date> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlDateAsDateHandler().setParameter(ps, 1, testData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlDateAsDateHandler().getNullableResult(rs, 1);
            });

            assert testData.getTime() != dat.get(0).getTime();
            LocalDate t1Data = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalDate t2Data = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalDate();
            assert t1Data.toEpochDay() == t2Data.toEpochDay();

            LocalTime t1Time = LocalDateTime.ofInstant(testData.toInstant(), ZoneId.systemDefault()).toLocalTime();
            LocalTime t2Time = LocalDateTime.ofInstant(dat.get(0).toInstant(), ZoneId.systemDefault()).toLocalTime();
            assert t1Time.toNanoOfDay() != t2Time.toNanoOfDay();
        }
    }

    @Test
    public void testDateOnlyTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 08:09:30', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlDateAsDateHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Date;
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(objectMap.get("out"));
            assert dateString.equals("2008-08-09 00:00:00");
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
