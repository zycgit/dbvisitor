package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class TimeTypeJdbcContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 700000;
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_SQL_DATE)
    public void timeSqlDate_shouldRoundTripDateColumn() throws SQLException {
        int id = baseId() + 1;
        LocalDate date = LocalDate.of(2024, 3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", //
                new Object[] { id, java.sql.Date.valueOf(date) });

        java.sql.Date loadedSql = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, java.sql.Date.class);

        assertNotNull(loadedSql);
        assertEquals(date, loadedSql.toLocalDate());
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_SQL_STANDARD)
    public void timeSqlStandardTypes_shouldRoundTripTimeAndTimestampColumns() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int timeId = baseId() + 2;
        int timestampId = baseId() + 3;
        Time time = Time.valueOf("14:30:45");
        Timestamp timestamp = Timestamp.valueOf("2024-03-15 14:30:45.123");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)", new Object[] { timeId, time });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { timestampId, timestamp });

        Time loadedTime = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?", new Object[] { timeId }, Time.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { timestampId }, Timestamp.class);

        assertNotNull(loadedTime);
        assertNotNull(loadedTimestamp);
        assertEquals(time.toString(), loadedTime.toString());
        assertTrue(Math.abs(timestamp.getTime() - loadedTimestamp.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_LOCAL_DATE)
    public void timeLocalDate_shouldRoundTripDateColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_LOCAL_DATE);
        int id = baseId() + 4;
        LocalDate date = LocalDate.of(2024, 3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", //
                new Object[] { id, java.sql.Date.valueOf(date) });

        LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, LocalDate.class);

        assertEquals(date, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_LOCAL_TIME)
    public void timeLocalTime_shouldRoundTripTimeColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int id = baseId() + 5;
        LocalTime time = LocalTime.of(14, 30, 45);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)", //
                new Object[] { id, java.sql.Time.valueOf(time) });

        LocalTime loaded = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, LocalTime.class);

        assertEquals(time, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_LOCAL_DATETIME)
    public void timeLocalDateTimeAndTimestamp_shouldRoundTripTimestampColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int id = baseId() + 6;
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", //
                new Object[] { id, timestamp });

        LocalDateTime loadedDateTime = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, LocalDateTime.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, Timestamp.class);

        assertNotNull(loadedDateTime);
        assertNotNull(loadedTimestamp);
        assertEquals(dateTime.getYear(), loadedDateTime.getYear());
        assertEquals(dateTime.getMonth(), loadedDateTime.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loadedDateTime.getDayOfMonth());
        assertEquals(dateTime.getHour(), loadedDateTime.getHour());
        assertEquals(dateTime.getMinute(), loadedDateTime.getMinute());
        assertEquals(dateTime.getSecond(), loadedDateTime.getSecond());
        assertTrue(Math.abs(timestamp.getTime() - loadedTimestamp.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_INSTANT)
    public void timeInstantAndUtilDate_shouldRoundTripTimestampColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int id = baseId() + 7;
        Instant instant = Instant.parse("2024-03-15T14:30:45.123Z");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", //
                new Object[] { id, Timestamp.from(instant) });

        Instant loadedInstant = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, Instant.class);
        Date loadedDate = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, Date.class);

        assertNotNull(loadedInstant);
        assertNotNull(loadedDate);
        assertTrue(Math.abs(instant.toEpochMilli() - loadedInstant.toEpochMilli()) < 1000);
        assertTrue(Math.abs(instant.toEpochMilli() - loadedDate.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_ZONE_INSTANT)
    public void timeZoneValues_shouldRoundTripAsEquivalentInstants() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int offsetId = baseId() + 8;
        int zonedId = baseId() + 9;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2024-03-15T14:30:45+08:00");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2024-03-15T14:30:45+08:00[Asia/Shanghai]");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", //
                new Object[] { offsetId, Timestamp.from(offsetDateTime.toInstant()) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", //
                new Object[] { zonedId, Timestamp.from(zonedDateTime.toInstant()) });

        Timestamp loadedOffset = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { offsetId }, Timestamp.class);
        Timestamp loadedZoned = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { zonedId }, Timestamp.class);

        assertNotNull(loadedOffset);
        assertNotNull(loadedZoned);
        assertTrue(Math.abs(offsetDateTime.toInstant().toEpochMilli() - loadedOffset.toInstant().toEpochMilli()) < 2000);
        assertTrue(Math.abs(zonedDateTime.toInstant().toEpochMilli() - loadedZoned.toInstant().toEpochMilli()) < 2000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL)
    public void timePartialTypes_shouldReadYearMonthAndMonthDayFromDateColumn() throws SQLException {
        int yearId = baseId() + 10;
        int yearMonthId = baseId() + 11;
        int monthId = baseId() + 12;
        int monthDayId = baseId() + 13;
        Year year = Year.of(2024);
        YearMonth yearMonth = YearMonth.of(2024, 3);
        Month month = Month.MARCH;
        MonthDay monthDay = MonthDay.of(3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { yearId, java.sql.Date.valueOf(year.atMonth(1).atDay(1)) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { yearMonthId, java.sql.Date.valueOf(yearMonth.atDay(1)) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { monthId, java.sql.Date.valueOf(Year.of(2024).atMonth(month).atDay(1)) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { monthDayId, java.sql.Date.valueOf(monthDay.atYear(2024)) });

        assertEquals(year, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { yearId }, Year.class));
        assertEquals(yearMonth, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { yearMonthId }, YearMonth.class));
        assertEquals(month, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { monthId }, Month.class));
        assertEquals(monthDay, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { monthDayId }, MonthDay.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_BOUNDARY)
    public void timeBoundaryValues_shouldRoundTripLeapDayMillenniumEpochAndDayEdges() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int leapId = baseId() + 14;
        int y1999Id = baseId() + 15;
        int y2000Id = baseId() + 16;
        int epochId = baseId() + 17;
        int midnightId = baseId() + 18;
        int endOfDayId = baseId() + 19;
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        LocalDate y1999 = LocalDate.of(1999, 12, 31);
        LocalDate y2000 = LocalDate.of(2000, 1, 1);
        LocalDateTime midnight = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 3, 15, 23, 59, 59);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { leapId, java.sql.Date.valueOf(leapDay) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { y1999Id, java.sql.Date.valueOf(y1999) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { y2000Id, java.sql.Date.valueOf(y2000) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { epochId, Timestamp.from(Instant.EPOCH) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { midnightId, Timestamp.valueOf(midnight) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { endOfDayId, Timestamp.valueOf(endOfDay) });

        assertEquals(leapDay, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { leapId }, java.sql.Date.class).toLocalDate());
        assertEquals(y1999, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { y1999Id }, java.sql.Date.class).toLocalDate());
        assertEquals(y2000, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { y2000Id }, java.sql.Date.class).toLocalDate());
        assertEquals(0L, jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { epochId }, Instant.class).getEpochSecond());
        assertEquals(midnight, jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { midnightId }, LocalDateTime.class));
        assertEquals(endOfDay, jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { endOfDayId }, LocalDateTime.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_EXTREME_DATE)
    public void timeExtremeDates_shouldRoundTripSupportedHistoricalAndFutureDates() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_EXTREME_DATE);
        int earlyId = baseId() + 20;
        int lateId = baseId() + 21;
        LocalDate earlyDate = LocalDate.of(1900, 1, 1);
        LocalDate lateDate = LocalDate.of(2100, 12, 31);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { earlyId, java.sql.Date.valueOf(earlyDate) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { lateId, java.sql.Date.valueOf(lateDate) });

        assertEquals(earlyDate, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { earlyId }, java.sql.Date.class).toLocalDate());
        assertEquals(lateDate, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { lateId }, java.sql.Date.class).toLocalDate());
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_JULIAN_DAY)
    public void timeJulianDay_shouldRoundTripBceDatesWithoutDatabaseCalendarConversion() throws SQLException {
        int bcId = baseId() + 22;
        int ancientId = baseId() + 23;
        LocalDate bcDate = LocalDate.of(-99, 1, 1);
        LocalDate ancientDate = LocalDate.of(-499, 6, 1);
        Map<String, Object> bcParams = new HashMap<>();
        bcParams.put("id", bcId);
        bcParams.put("julianDay", bcDate);
        Map<String, Object> ancientParams = new HashMap<>();
        ancientParams.put("id", ancientId);
        ancientParams.put("julianDay", ancientDate);

        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, julian_day) VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", //
                bcParams);
        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, julian_day) VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", //
                ancientParams);

        LocalDate loadedBc = jdbcTemplate.queryForObject("SELECT julian_day FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { bcId }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));
        LocalDate loadedAncient = jdbcTemplate.queryForObject("SELECT julian_day FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { ancientId }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));

        assertEquals(bcDate, loadedBc);
        assertEquals(ancientDate, loadedAncient);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PRECISION)
    public void timePrecision_shouldPreserveMillisecondAndTimestampPrecisionWithinDatabaseLimits() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int localDateTimeId = baseId() + 24;
        int timestampId = baseId() + 25;
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        Timestamp original = new Timestamp(System.currentTimeMillis());
        original.setNanos(123456789);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { localDateTimeId, Timestamp.valueOf(dateTime) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { timestampId, original });

        LocalDateTime loadedDateTime = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { localDateTimeId }, LocalDateTime.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { timestampId }, Timestamp.class);

        assertEquals(dateTime.getYear(), loadedDateTime.getYear());
        assertEquals(dateTime.getMonth(), loadedDateTime.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loadedDateTime.getDayOfMonth());
        assertEquals(dateTime.getHour(), loadedDateTime.getHour());
        assertEquals(dateTime.getMinute(), loadedDateTime.getMinute());
        assertEquals(dateTime.getSecond(), loadedDateTime.getSecond());
        assertTrue(Math.abs(dateTime.getNano() - loadedDateTime.getNano()) < 1_000_000);
        assertTrue(Math.abs(original.getTime() - loadedTimestamp.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_NULL)
    public void timeNulls_shouldRemainNull() throws SQLException {
        int id = baseId() + 26;

        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts) VALUES (?, ?, ?, ?, ?, ?, ?)", //
                new Object[] { id, null, null, null, null, null, null });

        assertNull(jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, LocalDate.class));
        assertNull(jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, LocalTime.class));
        assertNull(jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, LocalDateTime.class));
        assertNull(jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, Instant.class));

        Map<String, Object> row = jdbcTemplate.queryForMap(//
                "SELECT date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id });

        assertNull(value(row, "date_value"));
        assertNull(value(row, "time_value"));
        assertNull(value(row, "timestamp_value"));
        assertNull(value(row, "local_date_ts"));
        assertNull(value(row, "local_time_ts"));
        assertNull(value(row, "local_datetime_ts"));
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
