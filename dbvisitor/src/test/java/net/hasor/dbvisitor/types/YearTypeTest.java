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
import net.hasor.dbvisitor.types.handler.IntegerAsYearTypeHandler;
import net.hasor.dbvisitor.types.handler.SqlTimestampAsYearTypeHandler;
import net.hasor.dbvisitor.types.handler.StringAsYearTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

public class YearTypeTest {
    @Test
    public void testIntegerAsYearTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_smallint) values (2020);");
            List<Year> dat = jdbcTemplate.queryForList("select c_smallint from tb_h2_types where c_smallint is not null limit 1;", (rs, rowNum) -> {
                return new IntegerAsYearTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).getValue() == 2020;
        }
    }

    @Test
    public void testIntegerAsYearTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_smallint) values (2020);");
            List<Year> dat = jdbcTemplate.queryForList("select c_smallint from tb_h2_types where c_smallint is not null limit 1;", (rs, rowNum) -> {
                return new IntegerAsYearTypeHandler().getResult(rs, "c_smallint");
            });
            assert dat.get(0).getValue() == 2020;
        }
    }

    @Test
    public void testIntegerAsYearTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Year dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { Year.of(2008) }, Year.class);
            assert dat1.getValue() == 2008;

            List<Year> dat2 = jdbcTemplate.queryForList("select ?", ps -> {
                new IntegerAsYearTypeHandler().setParameter(ps, 1, Year.of(2008), JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new IntegerAsYearTypeHandler().getNullableResult(rs, 1);
            });
            assert dat2.get(0).getValue() == 2008;
        }
    }

    @Test
    public void testIntegerAsYearTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_integer;");
            jdbcTemplate.execute("create procedure proc_integer(out p_out integer) begin set p_out=2020; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_integer(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.INTEGER.getVendorTypeNumber(), new IntegerAsYearTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Year;
            Year instant = (Year) objectMap.get("out");
            assert instant.getValue() == 2020;
        }
    }

    @Test
    public void testYearOfStringTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('2008');");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('2022');");
            List<Year> dat = jdbcTemplate.queryForList("select c_varchar from tb_h2_types where c_varchar is not null limit 2;", (rs, rowNum) -> {
                return new StringAsYearTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).getValue() == 2008;
            assert dat.get(1).getValue() == 2022;
        }
    }

    @Test
    public void testYearOfStringTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('1986');");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('1998');");
            List<Year> dat = jdbcTemplate.queryForList("select c_varchar from tb_h2_types where c_varchar is not null limit 2;", (rs, rowNum) -> {
                return new StringAsYearTypeHandler().getResult(rs, "c_varchar");
            });
            assert dat.get(0).getValue() == 1986;
            assert dat.get(1).getValue() == 1998;
        }
    }

    @Test
    public void testYearOfStringTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Year dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { "0005" }, Year.class);
            assert dat1.getValue() == 5;
            Year dat2 = jdbcTemplate.queryForObject("select ?", new Object[] { "2020" }, Year.class);
            assert dat2.getValue() == 2020;

            List<Year> dat3 = jdbcTemplate.queryForList("select ?", ps -> {
                new StringAsYearTypeHandler().setParameter(ps, 1, Year.of(1998), JDBCType.SMALLINT.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new StringAsYearTypeHandler().getNullableResult(rs, 1);
            });
            assert dat3.get(0).getValue() == 1998;
        }
    }

    @Test
    public void testYearOfStringTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(10)) begin set p_out='2020'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARCHAR.getVendorTypeNumber(), new StringAsYearTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Year;
            Year instant = (Year) objectMap.get("out");
            assert instant.getValue() == 2020;
        }
    }

    @Test
    public void testYearOfTimeTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<Year> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsYearTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0).getValue() == YearMonth.now().getYear();
        }
    }

    @Test
    public void testYearOfTimeTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (CURRENT_TIMESTAMP(9));");
            List<Year> dat = jdbcTemplate.queryForList("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", (rs, rowNum) -> {
                return new SqlTimestampAsYearTypeHandler().getResult(rs, "c_timestamp");
            });
            assert dat.get(0).getValue() == YearMonth.now().getYear();
        }
    }

    @Test
    public void testYearOfTimeTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Year dat1 = jdbcTemplate.queryForObject("select ?", new Object[] { new Date() }, Year.class);
            assert dat1.getValue() == YearMonth.now().getYear();

            List<Year> dat2 = jdbcTemplate.queryForList("select ?", ps -> {
                new SqlTimestampAsYearTypeHandler().setParameter(ps, 1, Year.of(2018), JDBCType.TIMESTAMP.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new SqlTimestampAsYearTypeHandler().getNullableResult(rs, 1);
            });
            assert dat2.get(0).getValue() == 2018;

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_timestamp) values (?);", ps -> {
                new SqlTimestampAsYearTypeHandler().setParameter(ps, 1, Year.of(2018), JDBCType.TIMESTAMP.getVendorTypeNumber());
            });
            Date dat = jdbcTemplate.queryForObject("select c_timestamp from tb_h2_types where c_timestamp is not null limit 1;", Date.class);
            Calendar instance = Calendar.getInstance();
            instance.setTime(dat);
            int year = instance.get(Calendar.YEAR);
            assert year == 2018;
        }
    }

    @Test
    public void testYearOfTimeTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_timestamp;");
            jdbcTemplate.execute("create procedure proc_timestamp(out p_out timestamp) begin set p_out= str_to_date('2008-08-09 10:11:12', '%Y-%m-%d %h:%i:%s'); end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_timestamp(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.TIMESTAMP.getVendorTypeNumber(), new SqlTimestampAsYearTypeHandler())));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Year;
            Year instant = (Year) objectMap.get("out");
            assert instant.getValue() == 2008;
        }
    }
}
