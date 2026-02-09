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
import java.sql.Connection;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.handler.time.PgDateTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * PostgreSQL DATE 类型处理器测试，支持公元前日期
 * @author 赵永春 (zyc@hasor.net)
 * @version 2026-02-07
 */
public class PgDateTypeHandlerTest {

    /**
     * 测试公元后日期（AD）
     */
    @Test
    public void testPgDateTypeHandler_AD() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            // 创建测试表
            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            // 测试公元后日期
            LocalDate testDate = LocalDate.of(2024, 3, 15);

            jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", ps -> {
                ps.setInt(1, 1);
                new PgDateTypeHandler().setParameter(ps, 2, testDate, JDBCType.DATE.getVendorTypeNumber());
            });

            LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM test_pg_date WHERE id = ?",//
                    new Object[] { 1 }, (rs, rowNum) -> new PgDateTypeHandler().getResult(rs, "date_value"));

            assertNotNull(loaded);
            assertEquals(testDate, loaded);
            assertEquals(2024, loaded.getYear());
            assertEquals(3, loaded.getMonthValue());
            assertEquals(15, loaded.getDayOfMonth());
        }
    }

    /**
     * 测试公元前日期（BC）- Year 0 = 1 BC
     */
    @Test
    public void testPgDateTypeHandler_BC_Year0() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            // ISO 8601: Year 0 = 公元前1年 (1 BC)
            LocalDate bcDate = LocalDate.of(0, 6, 15);

            jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", ps -> {
                ps.setInt(1, 2);
                new PgDateTypeHandler().setParameter(ps, 2, bcDate, JDBCType.DATE.getVendorTypeNumber());
            });

            // 验证数据库中存储的是 BC 格式
            String rawValue = jdbcTemplate.queryForObject("SELECT date_value::TEXT FROM test_pg_date WHERE id = ?",//
                    new Object[] { 2 }, String.class);
            assertTrue("Should contain BC suffix: " + rawValue, rawValue.contains(" BC"));

            // 读取并验证
            LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM test_pg_date WHERE id = ?", new Object[] { 2 }, (rs, rowNum) -> new PgDateTypeHandler().getResult(rs, "date_value"));

            assertNotNull(loaded);
            assertEquals(bcDate, loaded);
            assertEquals(0, loaded.getYear());
        }
    }

    /**
     * 测试公元前日期（BC）- Year -99 = 100 BC
     */
    @Test
    public void testPgDateTypeHandler_BC_Year99() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            // ISO 8601: Year -99 = 公元前100年 (100 BC)
            LocalDate ancientDate = LocalDate.of(-99, 1, 1);

            jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", ps -> {
                ps.setInt(1, 3);
                new PgDateTypeHandler().setParameter(ps, 2, ancientDate, JDBCType.DATE.getVendorTypeNumber());
            });

            // 验证数据库中存储的是 '0100-01-01 BC'
            String rawValue = jdbcTemplate.queryForObject("SELECT date_value::TEXT FROM test_pg_date WHERE id = ?",//
                    new Object[] { 3 }, String.class);
            assertTrue("Should be 100 BC: " + rawValue, rawValue.startsWith("0100-01-01") && rawValue.contains("BC"));

            LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM test_pg_date WHERE id = ?", new Object[] { 3 }, (rs, rowNum) -> new PgDateTypeHandler().getResult(rs, "date_value"));

            assertNotNull(loaded);
            assertEquals(ancientDate, loaded);
            assertEquals(-99, loaded.getYear());
            assertEquals(1, loaded.getMonthValue());
            assertEquals(1, loaded.getDayOfMonth());
        }
    }

    /**
     * 测试公元前更远的日期 - Year -499 = 500 BC
     */
    @Test
    public void testPgDateTypeHandler_BC_Year499() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            // ISO 8601: Year -499 = 公元前500年 (500 BC)
            LocalDate veryAncientDate = LocalDate.of(-499, 7, 15);

            jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", ps -> {
                ps.setInt(1, 4);
                new PgDateTypeHandler().setParameter(ps, 2, veryAncientDate, JDBCType.DATE.getVendorTypeNumber());
            });

            LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM test_pg_date WHERE id = ?",//
                    new Object[] { 4 }, (rs, rowNum) -> new PgDateTypeHandler().getResult(rs, "date_value"));

            assertNotNull(loaded);
            assertEquals(veryAncientDate, loaded);
            assertEquals(-499, loaded.getYear());
            assertEquals(7, loaded.getMonthValue());
            assertEquals(15, loaded.getDayOfMonth());
        }
    }

    /**
     * 测试 NULL 值处理
     */
    @Test
    public void testPgDateTypeHandler_Null() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", new Object[] { 5, null });

            LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM test_pg_date WHERE id = ?",//
                    new Object[] { 5 }, (rs, rowNum) -> new PgDateTypeHandler().getResult(rs, "date_value"));

            assertNull(loaded);
        }
    }

    /**
     * 测试批量操作：混合公元前后日期
     */
    @Test
    public void testPgDateTypeHandler_BatchMixed() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            PgDateTypeHandler handler = new PgDateTypeHandler();

            // 插入混合日期
            LocalDate[] dates = { LocalDate.of(2024, 1, 1),    // 公元后
                    LocalDate.of(0, 1, 1),        // 1 BC
                    LocalDate.of(-99, 2, 28),     // 100 BC
                    LocalDate.of(-999, 12, 31)    // 1000 BC
            };

            for (int i = 0; i < dates.length; i++) {
                final int id = i + 10;
                final LocalDate date = dates[i];
                jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", ps -> {
                    ps.setInt(1, id);
                    handler.setParameter(ps, 2, date, JDBCType.DATE.getVendorTypeNumber());
                });
            }

            // 读取并验证
            List<LocalDate> loaded = jdbcTemplate.queryForList("SELECT date_value FROM test_pg_date WHERE id >= 10 ORDER BY id",//
                    (rs, rowNum) -> handler.getResult(rs, "date_value"));

            assertEquals(4, loaded.size());
            for (int i = 0; i < dates.length; i++) {
                assertEquals("Date at index " + i, dates[i], loaded.get(i));
            }
        }
    }

    /**
     * 测试闰年公元前日期
     */
    @Test
    public void testPgDateTypeHandler_BC_LeapYear() throws Throwable {
        try (Connection c = DsUtils.pgConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS test_pg_date");
            jdbcTemplate.executeUpdate("CREATE TABLE test_pg_date (id INT PRIMARY KEY, date_value DATE)");

            // 由于 ISO 8601 和 PostgreSQL BC 的历法差异,没有年份既是 ISO 闰年又是 BC 闰年
            // 因此测试接近闰年边界的日期: 2月28日
            LocalDate nearLeapDate = LocalDate.of(-4, 2, 28);

            jdbcTemplate.executeUpdate("INSERT INTO test_pg_date (id, date_value) VALUES (?, ?)", ps -> {
                ps.setInt(1, 6);
                new PgDateTypeHandler().setParameter(ps, 2, nearLeapDate, JDBCType.DATE.getVendorTypeNumber());
            });

            LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM test_pg_date WHERE id = ?",//
                    new Object[] { 6 }, (rs, rowNum) -> new PgDateTypeHandler().getResult(rs, "date_value"));

            assertNotNull(loaded);
            assertEquals(nearLeapDate, loaded);
            assertEquals(-4, loaded.getYear());
            assertEquals(2, loaded.getMonthValue());
            assertEquals(28, loaded.getDayOfMonth());
        }
    }
}
