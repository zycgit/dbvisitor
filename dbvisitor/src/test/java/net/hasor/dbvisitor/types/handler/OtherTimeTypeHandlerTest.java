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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.chrono.JapaneseDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OtherTimeTypeHandlerTest {
    @Test
    public void testInstantTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Instant> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsInstantTypeHandler().getResult(rs, 1);
            });

            assert testData.toInstant().toEpochMilli() == dat.get(0).toEpochMilli();
        }
    }

    @Test
    public void testInstantTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<Instant> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsInstantTypeHandler().getResult(rs, "c_timestamp");
            });

            assert testData.toInstant().toEpochMilli() == dat.get(0).toEpochMilli();
        }
    }

    @Test
    public void testInstantTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            List<Instant> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsInstantTypeHandler().setParameter(ps, 1, testData.toInstant(), JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsInstantTypeHandler().getNullableResult(rs, 1);
            });

            assert testData.toInstant().toEpochMilli() == dat.get(0).toEpochMilli();
        }
    }

    @Test
    public void testInstantTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsInstantTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Instant;
            Date dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-09 10:11:12");
            Instant instant = (Instant) objectMap.get("out");
            assert dateString.toInstant().toEpochMilli() == instant.toEpochMilli();
        }
    }

    @Test
    public void testJapaneseDateTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<JapaneseDate> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new JapaneseDateAsSqlDateTypeHandler().getResult(rs, 1);
            });

            assert dat.get(0).toEpochDay() == LocalDate.now().toEpochDay();
        }
    }

    @Test
    public void testJapaneseDateTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Date testData = new Date();
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", new Object[] { testData });
            List<JapaneseDate> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new JapaneseDateAsSqlDateTypeHandler().getResult(rs, "c_timestamp");
            });

            assert dat.get(0).toEpochDay() == LocalDate.now().toEpochDay();
        }
    }

    @Test
    public void testJapaneseDateTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            LocalDate testData = LocalDate.of(1998, Month.APRIL, 12);
            JapaneseDate jpData = JapaneseDate.from(testData);
            List<JapaneseDate> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new JapaneseDateAsSqlDateTypeHandler().setParameter(ps, 1, jpData, JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new JapaneseDateAsSqlDateTypeHandler().getNullableResult(rs, 1);
            });

            JapaneseDate dateTime = dat.get(0);
            assert dateTime.toEpochDay() == testData.toEpochDay();
        }
    }

    @Test
    public void testJapaneseDateTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    SqlArg.asOut("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new JapaneseDateAsSqlDateTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof JapaneseDate;
            Date testDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-09 10:11:12");
            JapaneseDate instant = (JapaneseDate) objectMap.get("out");
            assert JapaneseDateAsSqlDateTypeHandler.toJapaneseDate(testDate).equals(instant);
        }
    }
}
