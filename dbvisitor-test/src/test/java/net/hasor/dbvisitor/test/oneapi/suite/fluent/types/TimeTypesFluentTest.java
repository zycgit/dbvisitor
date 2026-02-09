package net.hasor.dbvisitor.test.oneapi.suite.fluent.types;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.types.TimeTypesExplicitModel;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 时间日期类型测试 - Fluent API (LambdaTemplate)
 * 对应 programmatic/types/TimeTypesJdbcTest，使用 LambdaTemplate + TimeTypesExplicitModel 映射
 * 测试范围：
 * 1. 基本日期时间类型（java.sql.Date, Time, Timestamp）
 * 2. Java 8 时间 API（LocalDate, LocalTime, LocalDateTime）—— 通过 TIMESTAMP 列映射
 * 3. Instant、java.util.Date 类型
 * 4. 带时区类型（OffsetDateTime, ZonedDateTime）
 * 5. 特殊时间类型（Year, YearMonth, Month, MonthDay）
 * 6. 边界值场景（闰年、Y2K、Unix Epoch、午夜/一天结束、极端年份、公元前）
 * 7. 精度测试（毫秒）
 * 8. null 值处理
 * 注意：TimeTypesExplicitModel 使用 java.sql.Date/Time/Timestamp 和 Java 8 时间类型，
 * 部分场景需结合 JdbcTemplate 手动读取验证（如 Year/Month/MonthDay/BCE 等特殊类型）
 */
public class TimeTypesFluentTest extends AbstractOneApiTest {

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
     * 测试 java.sql.Date - 使用 LambdaTemplate 和 TimeTypesExplicitModel
     */
    @Test
    public void testSqlDate_BasicScenario() throws SQLException {
        java.sql.Date date = java.sql.Date.valueOf("2024-03-15");

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(1);
        model.setDateValue(date);

        int rows = lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 1)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getDateValue());
        assertEquals(date.toString(), loaded.getDateValue().toString());
    }

    /**
     * 测试 java.sql.Time - 使用 LambdaTemplate
     */
    @Test
    public void testSqlTime_BasicScenario() throws SQLException {
        java.sql.Time time = java.sql.Time.valueOf("14:30:45");

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(2);
        model.setTimeValue(time);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 2)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getTimeValue());
        assertEquals(time.toString(), loaded.getTimeValue().toString());
    }

    /**
     * 测试 java.sql.Timestamp - 使用 LambdaTemplate
     */
    @Test
    public void testSqlTimestamp_BasicScenario() throws SQLException {
        Timestamp timestamp = Timestamp.valueOf("2024-03-15 14:30:45.123");

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(3);
        model.setTimestampValue(timestamp);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 3)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getTimestampValue());
        long diff = Math.abs(timestamp.getTime() - loaded.getTimestampValue().getTime());
        assertTrue("Timestamp diff: " + diff + "ms", diff < 1000);
    }

    // ========== 场景 2: Java 8 时间 API（通过 TimeTypesExplicitModel 的 local_*_ts 字段）==========

    /**
     * 测试 LocalDate - 映射到 TIMESTAMP 列
     */
    @Test
    public void testLocalDate_ViaModel() throws SQLException {
        LocalDate date = LocalDate.of(2024, 3, 15);

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(4);
        model.setLocalDateTs(date);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 4)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getLocalDateTs());
        assertEquals(date, loaded.getLocalDateTs());
    }

    /**
     * 测试 LocalTime - 映射到 TIMESTAMP 列
     * 注意：PostgreSQL JDBC 驱动不支持 TIMESTAMP→LocalTime 的直接转换，
     * 因此使用 JdbcTemplate 插入和读取，通过 Timestamp 手动提取 LocalTime
     */
    @Test
    public void testLocalTime_ViaModel() throws SQLException {
        LocalTime time = LocalTime.of(14, 30, 45);

        // 使用 JdbcTemplate 通过 TIMESTAMP 写入
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, local_time_ts) VALUES (?, ?::TIMESTAMP)",//
                new Object[] { 5, "1970-01-01 " + time.toString() });

        // 使用 JdbcTemplate 读取为 Timestamp，再提取 LocalTime
        Timestamp ts = jdbcTemplate.queryForObject("SELECT local_time_ts FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 5 }, Timestamp.class);

        assertNotNull(ts);
        LocalTime loadedTime = ts.toLocalDateTime().toLocalTime();
        assertEquals(time, loadedTime);
    }

    /**
     * 测试 LocalDateTime - 映射到 TIMESTAMP 列
     */
    @Test
    public void testLocalDateTime_ViaModel() throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45);

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(6);
        model.setLocalDateTimeTs(dateTime);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 6)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getLocalDateTimeTs());
        assertEquals(dateTime, loaded.getLocalDateTimeTs());
    }

    // ========== 场景 3: Instant 和 java.util.Date ==========

    /**
     * 测试 Instant - 通过 Timestamp 转换
     */
    @Test
    public void testInstant_ViaTimestamp() throws SQLException {
        Instant instant = Instant.parse("2024-03-15T14:30:45.123Z");

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(7);
        model.setTimestampValue(Timestamp.from(instant));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 使用 JdbcTemplate 读取为 Instant（TypeHandler 自动转换）
        Instant loaded = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 7 }, Instant.class);

        assertNotNull(loaded);
        long diff = Math.abs(instant.toEpochMilli() - loaded.toEpochMilli());
        assertTrue("Instant diff: " + diff + "ms", diff < 1000);
    }

    /**
     * 测试 java.util.Date - 传统日期类型
     */
    @Test
    public void testUtilDate_LegacyType() throws SQLException {
        Date utilDate = new Date();
        Timestamp timestamp = new Timestamp(utilDate.getTime());

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(8);
        model.setTimestampValue(timestamp);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 使用 JdbcTemplate 读取为 java.util.Date
        Date loaded = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 8 }, Date.class);

        assertNotNull(loaded);
        long diff = Math.abs(utilDate.getTime() - loaded.getTime());
        assertTrue("Date diff: " + diff + "ms", diff < 1000);
    }

    // ========== 场景 4: 使用 JdbcTemplate 读取 LocalDate/Time/DateTime ==========

    /**
     * 测试 LocalDate - 使用 JdbcTemplate 的 TypeHandler 自动转换
     */
    @Test
    public void testLocalDate_ViaJdbcTemplate() throws SQLException {
        LocalDate date = LocalDate.of(2024, 3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",//
                new Object[] { 9, java.sql.Date.valueOf(date) });

        java.sql.Date loaded = jdbcTemplate.queryForObject(//
                "SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 9 }, java.sql.Date.class);

        assertNotNull(loaded);
        assertEquals(date, loaded.toLocalDate());
    }

    /**
     * 测试 LocalTime - 使用 JdbcTemplate TypeHandler 自动转换
     */
    @Test
    public void testLocalTime_ViaJdbcTemplate() throws SQLException {
        LocalTime time = LocalTime.of(14, 30, 45);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)",//
                new Object[] { 10, java.sql.Time.valueOf(time) });

        LocalTime loaded = jdbcTemplate.queryForObject(//
                "SELECT time_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 10 }, LocalTime.class);

        assertNotNull(loaded);
        assertEquals(time, loaded);
    }

    /**
     * 测试 LocalDateTime - 使用 JdbcTemplate TypeHandler 自动转换
     */
    @Test
    public void testLocalDateTime_ViaJdbcTemplate() throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)",//
                new Object[] { 11, Timestamp.valueOf(dateTime) });

        LocalDateTime loaded = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 11 }, LocalDateTime.class);

        assertNotNull(loaded);
        assertEquals(dateTime, loaded);
    }

    // ========== 场景 5: 带时区类型 ==========

    /**
     * 测试 OffsetDateTime - 带偏移量的日期时间
     */
    @Test
    public void testOffsetDateTime_WithTimezone() throws SQLException {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2024-03-15T14:30:45+08:00");

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(12);
        model.setTimestampValue(Timestamp.from(offsetDateTime.toInstant()));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        Timestamp loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 12)//
                .queryForObject()//
                .getTimestampValue();

        assertNotNull(loaded);
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

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(13);
        model.setTimestampValue(Timestamp.from(zonedDateTime.toInstant()));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        Timestamp loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 13)//
                .queryForObject()//
                .getTimestampValue();

        assertNotNull(loaded);
        Instant originalInstant = zonedDateTime.toInstant();
        Instant loadedInstant = loaded.toInstant();
        long diff = Math.abs(originalInstant.toEpochMilli() - loadedInstant.toEpochMilli());
        assertTrue("Timestamp diff should be small: " + diff + "ms", diff < 2000);
    }

    // ========== 场景 6: 特殊时间类型（通过 JdbcTemplate 读取） ==========

    /**
     * 测试 Year - 年份类型
     */
    @Test
    public void testYear_SpecialType() throws SQLException {
        Year year = Year.of(2024);

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(14);
        model.setDateValue(java.sql.Date.valueOf(year.atMonth(1).atDay(1)));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        Year loaded = jdbcTemplate.queryForObject(//
                "SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 14 }, Year.class);

        assertNotNull(loaded);
        assertEquals(year, loaded);
    }

    /**
     * 测试 YearMonth - 年月类型
     */
    @Test
    public void testYearMonth_SpecialType() throws SQLException {
        YearMonth yearMonth = YearMonth.of(2024, 3);

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(15);
        model.setDateValue(java.sql.Date.valueOf(yearMonth.atDay(1)));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        YearMonth loaded = jdbcTemplate.queryForObject(//
                "SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 15 }, YearMonth.class);

        assertNotNull(loaded);
        assertEquals(yearMonth, loaded);
    }

    /**
     * 测试 Month - 月份类型
     */
    @Test
    public void testMonth_SpecialType() throws SQLException {
        Month month = Month.MARCH;

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(16);
        model.setDateValue(java.sql.Date.valueOf(Year.now().atMonth(month).atDay(1)));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        Month loaded = jdbcTemplate.queryForObject(//
                "SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 16 }, Month.class);

        assertNotNull(loaded);
        assertEquals(month, loaded);
    }

    /**
     * 测试 MonthDay - 月日类型
     */
    @Test
    public void testMonthDay_SpecialType() throws SQLException {
        MonthDay monthDay = MonthDay.of(3, 15);

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(17);
        model.setDateValue(java.sql.Date.valueOf(monthDay.atYear(Year.now().getValue())));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        MonthDay loaded = jdbcTemplate.queryForObject(//
                "SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 17 }, MonthDay.class);

        assertNotNull(loaded);
        assertEquals(monthDay, loaded);
    }

    // ========== 场景 7: 边界值场景 ==========

    /**
     * 测试闰年 - 2月29日
     */
    @Test
    public void testBoundary_LeapYear() throws SQLException {
        LocalDate leapDay = LocalDate.of(2024, 2, 29);

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(18);
        model.setDateValue(java.sql.Date.valueOf(leapDay));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 18)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getDateValue());
        LocalDate loadedDate = loaded.getDateValue().toLocalDate();
        assertEquals(leapDay, loadedDate);
        assertEquals(29, loadedDate.getDayOfMonth());
        assertEquals(Month.FEBRUARY, loadedDate.getMonth());
    }

    /**
     * 测试世纪边界 - Y2K
     */
    @Test
    public void testBoundary_MillenniumTransition() throws SQLException {
        LocalDate y1999 = LocalDate.of(1999, 12, 31);
        LocalDate y2000 = LocalDate.of(2000, 1, 1);

        TimeTypesExplicitModel model1 = new TimeTypesExplicitModel();
        model1.setId(19);
        model1.setDateValue(java.sql.Date.valueOf(y1999));

        TimeTypesExplicitModel model2 = new TimeTypesExplicitModel();
        model2.setId(20);
        model2.setDateValue(java.sql.Date.valueOf(y2000));

        lambdaTemplate.insert(TimeTypesExplicitModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(TimeTypesExplicitModel.class).applyEntity(model2).executeSumResult();

        TimeTypesExplicitModel loaded1 = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 19).queryForObject();
        TimeTypesExplicitModel loaded2 = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 20).queryForObject();

        assertEquals(y1999, loaded1.getDateValue().toLocalDate());
        assertEquals(y2000, loaded2.getDateValue().toLocalDate());
    }

    /**
     * 测试 Unix Epoch - 1970-01-01
     */
    @Test
    public void testBoundary_UnixEpoch() throws SQLException {
        Instant epoch = Instant.EPOCH;

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(21);
        model.setTimestampValue(Timestamp.from(epoch));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        Instant loaded = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 21 }, Instant.class);

        assertNotNull(loaded);
        assertEquals(0L, loaded.getEpochSecond());
    }

    /**
     * 测试时间边界 - 午夜和一天结束
     */
    @Test
    public void testBoundary_MidnightAndEndOfDay() throws SQLException {
        LocalDateTime midnight = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 3, 15, 23, 59, 59);

        TimeTypesExplicitModel model1 = new TimeTypesExplicitModel();
        model1.setId(22);
        model1.setTimestampValue(Timestamp.valueOf(midnight));

        TimeTypesExplicitModel model2 = new TimeTypesExplicitModel();
        model2.setId(23);
        model2.setTimestampValue(Timestamp.valueOf(endOfDay));

        lambdaTemplate.insert(TimeTypesExplicitModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(TimeTypesExplicitModel.class).applyEntity(model2).executeSumResult();

        LocalDateTime loadedMidnight = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 22 }, LocalDateTime.class);
        LocalDateTime loadedEndOfDay = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 23 }, LocalDateTime.class);

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
        LocalDate earlyDate = LocalDate.of(1900, 1, 1);
        LocalDate lateDate = LocalDate.of(2100, 12, 31);

        TimeTypesExplicitModel model1 = new TimeTypesExplicitModel();
        model1.setId(24);
        model1.setDateValue(java.sql.Date.valueOf(earlyDate));

        TimeTypesExplicitModel model2 = new TimeTypesExplicitModel();
        model2.setId(25);
        model2.setDateValue(java.sql.Date.valueOf(lateDate));

        lambdaTemplate.insert(TimeTypesExplicitModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(TimeTypesExplicitModel.class).applyEntity(model2).executeSumResult();

        TimeTypesExplicitModel loaded1 = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 24).queryForObject();
        TimeTypesExplicitModel loaded2 = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 25).queryForObject();

        assertEquals(earlyDate, loaded1.getDateValue().toLocalDate());
        assertEquals(lateDate, loaded2.getDateValue().toLocalDate());
    }

    /**
     * 测试公元前日期 - 使用 JulianDayTypeHandler
     */
    @Test
    public void testBoundary_BCEDates() throws SQLException {
        LocalDate bcDate = LocalDate.of(-99, 1, 1);

        // 使用 JulianDayTypeHandler 将日期转换为儒略日数存储
        Map<String, Object> params = new HashMap<>();
        params.put("id", 26);
        params.put("julianDay", bcDate);
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, julian_day) " +//
                "VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", params);

        // 从儒略日数读取日期
        LocalDate loaded = jdbcTemplate.queryForObject(//
                "SELECT julian_day FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 26 }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));

        assertNotNull(loaded);
        assertEquals(bcDate, loaded);
        assertEquals(-99, loaded.getYear());
    }

    // ========== 场景 8: 精度测试 ==========

    /**
     * 测试毫秒精度
     */
    @Test
    public void testPrecision_Milliseconds() throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000); // 123毫秒

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(27);
        model.setTimestampValue(Timestamp.valueOf(dateTime));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        LocalDateTime loaded = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 27 }, LocalDateTime.class);

        assertNotNull(loaded);
        assertEquals(dateTime.getYear(), loaded.getYear());
        assertEquals(dateTime.getMonth(), loaded.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loaded.getDayOfMonth());
        assertEquals(dateTime.getHour(), loaded.getHour());
        assertEquals(dateTime.getMinute(), loaded.getMinute());
        assertEquals(dateTime.getSecond(), loaded.getSecond());
        long nanosDiff = Math.abs(dateTime.getNano() - loaded.getNano());
        assertTrue("Nanos diff: " + nanosDiff, nanosDiff < 1_000_000); // 1毫秒以内
    }

    /**
     * 测试 Timestamp 精度保持
     */
    @Test
    public void testPrecision_TimestampPreservation() throws SQLException {
        Timestamp original = new Timestamp(System.currentTimeMillis());

        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(28);
        model.setTimestampValue(original);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 28)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getTimestampValue());
        long timeDiff = Math.abs(original.getTime() - loaded.getTimestampValue().getTime());
        assertTrue("Timestamp diff: " + timeDiff + "ms", timeDiff < 1000);
    }

    // ========== 场景 9: null 值处理 ==========

    /**
     * 测试所有时间类型的 null 值（LambdaTemplate）
     */
    @Test
    public void testNullValues_AllTimeTypes_ViaLambdaTemplate() throws SQLException {
        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(29);
        model.setDateValue(null);
        model.setTimeValue(null);
        model.setTimestampValue(null);
        model.setLocalDateTs(null);
        model.setLocalTimeTs(null);
        model.setLocalDateTimeTs(null);

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 29)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getDateValue());
        assertNull(loaded.getTimeValue());
        assertNull(loaded.getTimestampValue());
        assertNull(loaded.getLocalDateTs());
        assertNull(loaded.getLocalTimeTs());
        assertNull(loaded.getLocalDateTimeTs());
    }

    /**
     * 测试所有时间类型的 null 值（JdbcTemplate TypeHandler 读取）
     */
    @Test
    public void testNullValues_AllTimeTypes_ViaJdbcTemplate() throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts) VALUES (?, ?, ?, ?, ?, ?, ?)",//
                new Object[] { 30, null, null, null, null, null, null });

        LocalDate localDate = jdbcTemplate.queryForObject(//
                "SELECT date_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 30 }, LocalDate.class);
        assertNull(localDate);

        LocalTime localTime = jdbcTemplate.queryForObject(//
                "SELECT time_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 30 }, LocalTime.class);
        assertNull(localTime);

        LocalDateTime localDateTime = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 30 }, LocalDateTime.class);
        assertNull(localDateTime);

        Instant instant = jdbcTemplate.queryForObject(//
                "SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?",//
                new Object[] { 30 }, Instant.class);
        assertNull(instant);
    }

    // ========== 场景 10: 更新操作 ==========

    /**
     * 测试更新时间字段
     */
    @Test
    public void testUpdate_TimeFields() throws SQLException {
        // 初始插入
        TimeTypesExplicitModel model = new TimeTypesExplicitModel();
        model.setId(31);
        model.setDateValue(java.sql.Date.valueOf("2024-01-01"));
        model.setTimestampValue(Timestamp.valueOf("2024-01-01 00:00:00"));

        lambdaTemplate.insert(TimeTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 更新日期和时间戳
        java.sql.Date newDate = java.sql.Date.valueOf("2024-12-31");
        Timestamp newTimestamp = Timestamp.valueOf("2024-12-31 23:59:59");

        int updated = lambdaTemplate.update(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 31)//
                .updateTo(TimeTypesExplicitModel::getDateValue, newDate)//
                .updateTo(TimeTypesExplicitModel::getTimestampValue, newTimestamp)//
                .doUpdate();
        assertEquals(1, updated);

        // 验证更新
        TimeTypesExplicitModel loaded = lambdaTemplate.query(TimeTypesExplicitModel.class)//
                .eq(TimeTypesExplicitModel::getId, 31)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(newDate.toString(), loaded.getDateValue().toString());
        long diff = Math.abs(newTimestamp.getTime() - loaded.getTimestampValue().getTime());
        assertTrue("Timestamp diff: " + diff + "ms", diff < 1000);
    }
}
