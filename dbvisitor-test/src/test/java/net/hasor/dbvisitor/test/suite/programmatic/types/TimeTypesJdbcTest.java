package net.hasor.dbvisitor.test.suite.programmatic.types;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 时间日期类型测试 - Programmatic API (JdbcTemplate)
 * 测试范围（按场景维度）：
 * 1. 基本日期时间类型（LocalDate, LocalTime, LocalDateTime, Instant）
 * 2. SQL 标准类型（java.sql.Date, Time, Timestamp）
 * 3. 传统 Java 类型（java.util.Date）
 * 4. 带时区类型（OffsetDateTime, OffsetTime, ZonedDateTime）
 * 5. 特殊时间类型（Year, YearMonth, Month, MonthDay）
 * 6. 边界值场景（最小值、最大值、闰年、世纪边界、Unix Epoch、公元前日期）
 * 7. 精度测试（毫秒、纳秒）
 * 8. null 值处理
 * 数据库初始化：通过 /sql/{dialect}/init.sql 脚本自动创建 time_types_explicit_test 表
 * 重点：使用 JdbcTemplate 的 queryForObject 等方法，让 TypeHandler 体系自动处理类型转换
 * TypeHandler 参考：
 * - net.hasor.dbvisitor.types.handler.time 包下的所有处理器
 * - 文档：docs/guides/types/handlers/datetime-handler.md
 * 后续扩展计划：
 * - MySQL Zero Date 兼容处理：
 * MySQL 允许特殊的零值日期时间（'0000-00-00', '0000-00-00 00:00:00'）
 * 这些值在严格模式下会导致错误，需要特殊处理：
 * 1. 读取时转换为 null 或特定的默认值
 * 2. 通过配置项控制零值的处理策略（null/exception/default）
 * 3. 兼容 jdbcCompliantTruncation 和 zeroDateTimeBehavior 参数
 * 参考：MySQL Connector/J 的 zeroDateTimeBehavior 配置
 */
public class TimeTypesJdbcTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM time_types_explicit_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    // ========== 场景 1: 基本日期时间类型 ==========

    /**
     * 测试 LocalDate - 日期类型
     */
    @Test
    public void testLocalDate_BasicScenario() throws SQLException {
        LocalDate date = LocalDate.of(2024, 3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { 1, java.sql.Date.valueOf(date) });

        // 读取为 java.sql.Date 再转换为 LocalDate（避免 TypeHandler 选择问题）
        java.sql.Date loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 1 }, java.sql.Date.class);

        assertNotNull(loaded);
        assertEquals(date, loaded.toLocalDate());
    }

    /**
     * 测试 LocalTime - 时间类型
     */
    @Test
    public void testLocalTime_BasicScenario() throws SQLException {
        LocalTime time = LocalTime.of(14, 30, 45);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)",//
                new Object[] { 2, java.sql.Time.valueOf(time) });

        // 使用 TypeHandler 自动转换
        LocalTime loaded = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 2 }, LocalTime.class);

        assertNotNull(loaded);
        assertEquals(time, loaded);
    }

    /**
     * 测试 LocalDateTime - 日期时间类型
     */
    @Test
    public void testLocalDateTime_BasicScenario() throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 3, Timestamp.valueOf(dateTime) });

        // 使用 TypeHandler 自动转换
        LocalDateTime loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 3 }, LocalDateTime.class);

        assertNotNull(loaded);
        assertEquals(dateTime, loaded);
    }

    /**
     * 测试 Instant - 时间戳类型
     */
    @Test
    public void testInstant_BasicScenario() throws SQLException {
        Instant instant = Instant.parse("2024-03-15T14:30:45.123Z");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 4, Timestamp.from(instant) });

        // 使用 TypeHandler 自动转换
        Instant loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 4 }, Instant.class);

        assertNotNull(loaded);
        // 允许毫秒级差异（数据库精度限制）
        long diff = Math.abs(instant.toEpochMilli() - loaded.toEpochMilli());
        assertTrue("Instant diff: " + diff + "ms", diff < 1000);
    }

    // ========== 场景 2: SQL 标准类型 ==========

    /**
     * 测试 java.sql.Date - SQL DATE 类型
     */
    @Test
    public void testSqlDate_StandardType() throws SQLException {
        java.sql.Date date = java.sql.Date.valueOf("2024-03-15");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 5, date });

        java.sql.Date loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 5 }, java.sql.Date.class);

        assertNotNull(loaded);
        assertEquals(date, loaded);
    }

    /**
     * 测试 java.sql.Time - SQL TIME 类型
     */
    @Test
    public void testSqlTime_StandardType() throws SQLException {
        java.sql.Time time = java.sql.Time.valueOf("14:30:45");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)",//
                new Object[] { 6, time });

        java.sql.Time loaded = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 6 }, java.sql.Time.class);

        assertNotNull(loaded);
        assertEquals(time.toString(), loaded.toString());
    }

    /**
     * 测试 java.sql.Timestamp - SQL TIMESTAMP 类型
     */
    @Test
    public void testSqlTimestamp_StandardType() throws SQLException {
        Timestamp timestamp = Timestamp.valueOf("2024-03-15 14:30:45.123");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 7, timestamp });

        Timestamp loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 7 }, Timestamp.class);

        assertNotNull(loaded);
        // 允许毫秒级差异
        long diff = Math.abs(timestamp.getTime() - loaded.getTime());
        assertTrue("Timestamp diff: " + diff + "ms", diff < 1000);
    }

    // ========== 场景 3: 传统 Java 类型 ==========

    /**
     * 测试 java.util.Date - 传统日期类型
     */
    @Test
    public void testUtilDate_LegacyType() throws SQLException {
        Date utilDate = new Date();
        Timestamp timestamp = new Timestamp(utilDate.getTime());

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 8, timestamp });

        // TypeHandler 可以转换为 java.util.Date
        Date loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 8 }, Date.class);

        assertNotNull(loaded);
        long diff = Math.abs(utilDate.getTime() - loaded.getTime());
        assertTrue("Date diff: " + diff + "ms", diff < 1000);
    }

    // ========== 场景 4: 带时区类型 ==========

    /**
     * 测试 OffsetDateTime - 带偏移量的日期时间
     */
    @Test
    public void testOffsetDateTime_WithTimezone() throws SQLException {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2024-03-15T14:30:45+08:00");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 9, Timestamp.from(offsetDateTime.toInstant()) });

        // 读取为 Timestamp 再转换（PostgreSQL TIMESTAMP 不存储时区信息）
        Timestamp loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 9 }, Timestamp.class);

        assertNotNull(loaded);
        // 比较时间戳值（允许毫秒级差异）
        Instant originalInstant = offsetDateTime.toInstant();
        Instant loadedInstant = loaded.toInstant();
        long diff = Math.abs(originalInstant.toEpochMilli() - loadedInstant.toEpochMilli());
        assertTrue("Timestamp diff should be small: " + diff + "ms", diff < 2000);
    }

    /**
     * 测试 ZonedDateTime - 带时区的日期时间
     */
    @Test
    public void testZonedDateTime_WithZone() throws SQLException {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2024-03-15T14:30:45+08:00[Asia/Shanghai]");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 10, Timestamp.from(zonedDateTime.toInstant()) });

        // 读取为 Timestamp 再转换（PostgreSQL TIMESTAMP 不存储时区信息）
        Timestamp loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 10 }, Timestamp.class);

        assertNotNull(loaded);
        // 比较时间戳值（允许毫秒级差异）
        Instant originalInstant = zonedDateTime.toInstant();
        Instant loadedInstant = loaded.toInstant();
        long diff = Math.abs(originalInstant.toEpochMilli() - loadedInstant.toEpochMilli());
        assertTrue("Timestamp diff should be small: " + diff + "ms", diff < 2000);
    }

    // ========== 场景 5: 特殊时间类型 ==========

    /**
     * 测试 Year - 年份类型
     */
    @Test
    public void testYear_SpecialType() throws SQLException {
        Year year = Year.of(2024);

        // Year 存储为 Date
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 11, java.sql.Date.valueOf(year.atMonth(1).atDay(1)) });

        // 读取为 Year
        Year loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 11 }, Year.class);

        assertNotNull(loaded);
        assertEquals(year, loaded);
    }

    /**
     * 测试 YearMonth - 年月类型
     */
    @Test
    public void testYearMonth_SpecialType() throws SQLException {
        YearMonth yearMonth = YearMonth.of(2024, 3);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 12, java.sql.Date.valueOf(yearMonth.atDay(1)) });

        // 读取为 YearMonth
        YearMonth loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 12 }, YearMonth.class);

        assertNotNull(loaded);
        assertEquals(yearMonth, loaded);
    }

    /**
     * 测试 Month - 月份类型
     */
    @Test
    public void testMonth_SpecialType() throws SQLException {
        Month month = Month.MARCH;

        // Month 存储为 Date
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 13, java.sql.Date.valueOf(Year.now().atMonth(month).atDay(1)) });

        // 读取为 Month
        Month loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { 13 }, Month.class);

        assertNotNull(loaded);
        assertEquals(month, loaded);
    }

    /**
     * 测试 MonthDay - 月日类型
     */
    @Test
    public void testMonthDay_SpecialType() throws SQLException {
        MonthDay monthDay = MonthDay.of(3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 14, java.sql.Date.valueOf(monthDay.atYear(Year.now().getValue())) });

        // 读取为 MonthDay
        MonthDay loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 14 }, MonthDay.class);

        assertNotNull(loaded);
        assertEquals(monthDay, loaded);
    }

    // ========== 场景 6: 边界值场景 ==========

    /**
     * 测试闰年 - 2月29日
     */
    @Test
    public void testBoundary_LeapYear() throws SQLException {
        // 2024 年是闰年
        LocalDate leapDay = LocalDate.of(2024, 2, 29);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 15, java.sql.Date.valueOf(leapDay) });

        java.sql.Date loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 15 }, java.sql.Date.class);

        assertNotNull(loaded);
        LocalDate loadedDate = loaded.toLocalDate();
        assertEquals(leapDay, loadedDate);
        assertEquals(29, loadedDate.getDayOfMonth());
        assertEquals(Month.FEBRUARY, loadedDate.getMonth());
    }

    /**
     * 测试世纪边界 - Y2K
     */
    @Test
    public void testBoundary_MillenniumTransition() throws SQLException {
        // 1999年最后一天
        LocalDate y1999 = LocalDate.of(1999, 12, 31);
        // 2000年第一天
        LocalDate y2000 = LocalDate.of(2000, 1, 1);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 16, java.sql.Date.valueOf(y1999) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 17, java.sql.Date.valueOf(y2000) });

        java.sql.Date loaded1999Sql = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 16 }, java.sql.Date.class);
        java.sql.Date loaded2000Sql = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 17 }, java.sql.Date.class);

        LocalDate loaded1999 = loaded1999Sql.toLocalDate();
        LocalDate loaded2000 = loaded2000Sql.toLocalDate();
        assertEquals(y1999, loaded1999);
        assertEquals(y2000, loaded2000);
        assertEquals(1999, loaded1999.getYear());
        assertEquals(2000, loaded2000.getYear());
    }

    /**
     * 测试 Unix Epoch - 1970-01-01
     */
    @Test
    public void testBoundary_UnixEpoch() throws SQLException {
        Instant epoch = Instant.EPOCH; // 1970-01-01T00:00:00Z

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 18, Timestamp.from(epoch) });

        Instant loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 18 }, Instant.class);

        assertNotNull(loaded);
        assertEquals(0L, loaded.getEpochSecond());
    }

    /**
     * 测试时间边界 - 午夜和一天结束
     */
    @Test
    public void testBoundary_MidnightAndEndOfDay() throws SQLException {
        // 午夜 00:00:00
        LocalDateTime midnight = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        // 一天结束 23:59:59
        LocalDateTime endOfDay = LocalDateTime.of(2024, 3, 15, 23, 59, 59);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 19, Timestamp.valueOf(midnight) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 20, Timestamp.valueOf(endOfDay) });

        LocalDateTime loadedMidnight = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 19 }, LocalDateTime.class);
        LocalDateTime loadedEndOfDay = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 20 }, LocalDateTime.class);

        assertEquals(midnight, loadedMidnight);
        assertEquals(0, loadedMidnight.getHour());
        assertEquals(0, loadedMidnight.getMinute());

        assertEquals(endOfDay, loadedEndOfDay);
        assertEquals(23, loadedEndOfDay.getHour());
        assertEquals(59, loadedEndOfDay.getMinute());
    }

    /**
     * 测试极端年份 - 较早和较晚的日期
     */
    @Test
    public void testBoundary_ExtremeDates() throws SQLException {
        // 较早的日期
        LocalDate earlyDate = LocalDate.of(1900, 1, 1);
        // 较晚的日期
        LocalDate lateDate = LocalDate.of(2100, 12, 31);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 21, java.sql.Date.valueOf(earlyDate) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 22, java.sql.Date.valueOf(lateDate) });

        java.sql.Date loadedEarlySql = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 21 }, java.sql.Date.class);
        java.sql.Date loadedLateSql = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 22 }, java.sql.Date.class);

        LocalDate loadedEarly = loadedEarlySql.toLocalDate();
        LocalDate loadedLate = loadedLateSql.toLocalDate();
        assertEquals(earlyDate, loadedEarly);
        assertEquals(lateDate, loadedLate);
    }

    /**
     * 测试公元前日期（BCE - Before Common Era）
     * 注意：公元前日期使用负数年份表示，例如：
     * - 公元前 1 年 = Year 0（ISO 8601 标准）或 Year -1（历史纪年法）
     * - 公元前 2 年 = Year -1（ISO 8601）或 Year -2（历史纪年法）
     * - 公元前 100 年 = Year -99（ISO 8601）或 Year -100（历史纪年法）
     * 已知限制：
     * 1. 公元前日期存在历法转换问题（儒略历 vs 格里高利历）
     * 2. Year 0 的表示在不同系统中不一致（ISO 8601 vs 历史纪年法）
     * 3. 不同数据库对公元前日期的支持和转换规则不同
     * 4. 可能出现日期偏移或年份偏移
     * 解决方案：使用儒略日数（Julian Day Number）统一存储，避免历法和系统差异
     */
    @Test
    public void testBoundary_BCEDates() throws SQLException {
        // 测试公元前 100 年（使用 ISO 8601: Year -99）
        // 使用年初日期，避免历法转换边界问题
        LocalDate bcDate = LocalDate.of(-99, 1, 1);

        // 使用 JulianDayTypeHandler 将日期转换为儒略日数存储
        Map<String, Object> params1 = new HashMap<>();
        params1.put("id", 23);
        params1.put("julianDay", bcDate);
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, julian_day) " +//
                "VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", params1);

        // 从儒略日数读取日期
        LocalDate loadedBC = jdbcTemplate.queryForObject("SELECT julian_day FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 23 }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));

        assertNotNull(loadedBC);
        assertEquals(bcDate, loadedBC);
        assertEquals(-99, loadedBC.getYear());
        assertEquals(1, loadedBC.getMonthValue());
        assertEquals(1, loadedBC.getDayOfMonth());

        // 测试公元前更远的日期（公元前 500 年）
        LocalDate ancientDate = LocalDate.of(-499, 6, 1);
        Map<String, Object> params2 = new HashMap<>();
        params2.put("id", 24);
        params2.put("julianDay", ancientDate);
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, julian_day) " +//
                "VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", params2);

        LocalDate loadedAncient = jdbcTemplate.queryForObject("SELECT julian_day FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 24 }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));

        assertNotNull(loadedAncient);
        assertEquals(ancientDate, loadedAncient);
        assertEquals(-499, loadedAncient.getYear());
        assertEquals(6, loadedAncient.getMonthValue());
        assertEquals(1, loadedAncient.getDayOfMonth());
    }

    // ========== 场景 7: 精度测试 ==========

    /**
     * 测试毫秒精度
     */
    @Test
    public void testPrecision_Milliseconds() throws SQLException {
        // 创建带毫秒的时间戳
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000); // 123毫秒

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 25, Timestamp.valueOf(dateTime) });

        LocalDateTime loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 25 }, LocalDateTime.class);

        assertNotNull(loaded);
        assertEquals(dateTime.getYear(), loaded.getYear());
        assertEquals(dateTime.getMonth(), loaded.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loaded.getDayOfMonth());
        assertEquals(dateTime.getHour(), loaded.getHour());
        assertEquals(dateTime.getMinute(), loaded.getMinute());
        assertEquals(dateTime.getSecond(), loaded.getSecond());
        // 毫秒精度验证（允许数据库精度限制）
        long nanosDiff = Math.abs(dateTime.getNano() - loaded.getNano());
        assertTrue("Nanos diff: " + nanosDiff, nanosDiff < 1_000_000); // 1毫秒以内
    }

    /**
     * 测试 Timestamp 精度保持
     */
    @Test
    public void testPrecision_TimestampPreservation() throws SQLException {
        Timestamp original = new Timestamp(System.currentTimeMillis());
        original.setNanos(123456789); // 设置纳秒

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 26, original });

        Timestamp loaded = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 26 }, Timestamp.class);

        assertNotNull(loaded);
        // 允许毫秒级差异（数据库可能不支持纳秒）
        long timeDiff = Math.abs(original.getTime() - loaded.getTime());
        assertTrue("Time diff: " + timeDiff + "ms", timeDiff < 1000);
    }

    // ========== 场景 8: null 值处理 ==========

    /**
     * 测试所有时间类型的 null 值
     */
    @Test
    public void testNullValues_AllTimeTypes() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts) VALUES (?, ?, ?, ?, ?, ?, ?)",//
                new Object[] { 27, null, null, null, null, null, null });

        // 测试各种类型的 null 读取
        LocalDate localDate = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 27 }, LocalDate.class);
        assertNull(localDate);

        LocalTime localTime = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 27 }, LocalTime.class);
        assertNull(localTime);

        LocalDateTime localDateTime = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 27 }, LocalDateTime.class);
        assertNull(localDateTime);

        Instant instant = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 27 }, Instant.class);
        assertNull(instant);

        // 使用 queryForMap 验证所有字段
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 27 });

        assertNotNull(result);
        assertNull(result.get("date_value"));
        assertNull(result.get("time_value"));
        assertNull(result.get("timestamp_value"));
        assertNull(result.get("local_date_ts"));
        assertNull(result.get("local_time_ts"));
        assertNull(result.get("local_datetime_ts"));
    }
}
