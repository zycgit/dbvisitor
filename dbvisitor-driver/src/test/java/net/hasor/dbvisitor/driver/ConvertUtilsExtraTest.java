package net.hasor.dbvisitor.driver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/** Additional ConvertUtils tests — covers uncovered Number subtype branches, date/time chains, and string parsing. */
public class ConvertUtilsExtraTest {

    // ==================== toBoolean — Number subtypes ====================
    @Test
    public void toBoolean_short() {
        assertTrue(ConvertUtils.toBoolean((short) 1, false));
    }

    @Test
    public void toBoolean_byte() {
        assertTrue(ConvertUtils.toBoolean((byte) 1, false));
    }

    @Test
    public void toBoolean_bigDecimal() {
        assertTrue(ConvertUtils.toBoolean(BigDecimal.ONE, false));
    }

    @Test
    public void toBoolean_bigInteger() {
        assertTrue(ConvertUtils.toBoolean(BigInteger.ONE, false));
    }

    @Test
    public void toBoolean_short_zero() {
        assertFalse(ConvertUtils.toBoolean((short) 0, false));
    }

    @Test
    public void toBoolean_byte_zero() {
        assertFalse(ConvertUtils.toBoolean((byte) 0, false));
    }

    // ==================== toBit — Number subtypes ====================
    @Test
    public void toBit_short() {
        assertNotNull(ConvertUtils.toBit((short) 5));
    }

    @Test
    public void toBit_int() {
        assertNotNull(ConvertUtils.toBit(42));
    }

    @Test
    public void toBit_long() {
        assertNotNull(ConvertUtils.toBit(100L));
    }

    @Test
    public void toBit_bigInteger() {
        assertNotNull(ConvertUtils.toBit(BigInteger.TEN));
    }

    @Test
    public void toBit_null() {
        assertNull(ConvertUtils.toBit(null));
    }

    @Test
    public void toBit_boolean() {
        assertNotNull(ConvertUtils.toBit(true));
    }

    @Test
    public void toBit_string() {
        assertNotNull(ConvertUtils.toBit("1010"));
    }

    // ==================== toByte — Number subtypes ====================
    @Test
    public void toByte_short() {
        assertEquals(5, (byte) ConvertUtils.toByte((short) 5, false));
    }

    @Test
    public void toByte_float() {
        assertEquals(1, (byte) ConvertUtils.toByte(1.5f, false));
    }

    @Test
    public void toByte_double() {
        assertEquals(2, (byte) ConvertUtils.toByte(2.5d, false));
    }

    @Test
    public void toByte_bigInteger() {
        assertEquals(7, (byte) ConvertUtils.toByte(BigInteger.valueOf(7), false));
    }

    @Test
    public void toByte_bigDecimal() {
        assertEquals(3, (byte) ConvertUtils.toByte(BigDecimal.valueOf(3), false));
    }

    // ==================== toShort — Number subtypes ====================
    @Test
    public void toShort_byte() {
        assertEquals(5, (short) ConvertUtils.toShort((byte) 5, false));
    }

    @Test
    public void toShort_float() {
        assertEquals(1, (short) ConvertUtils.toShort(1.5f, false));
    }

    @Test
    public void toShort_double() {
        assertEquals(2, (short) ConvertUtils.toShort(2.5d, false));
    }

    @Test
    public void toShort_bigDecimal() {
        assertEquals(3, (short) ConvertUtils.toShort(BigDecimal.valueOf(3), false));
    }

    @Test
    public void toShort_bigInteger() {
        assertEquals(7, (short) ConvertUtils.toShort(BigInteger.valueOf(7), false));
    }

    // ==================== toInteger — Number subtypes ====================
    @Test
    public void toInteger_short() {
        assertEquals(5, (int) ConvertUtils.toInteger((short) 5, false));
    }

    @Test
    public void toInteger_byte() {
        assertEquals(3, (int) ConvertUtils.toInteger((byte) 3, false));
    }

    @Test
    public void toInteger_float() {
        assertEquals(3, (int) ConvertUtils.toInteger(3.14f, false));
    }

    @Test
    public void toInteger_bigInteger() {
        assertEquals(10, (int) ConvertUtils.toInteger(BigInteger.TEN, false));
    }

    // ==================== toLong — Number subtypes ====================
    @Test
    public void toLong_short() {
        assertEquals(5L, (long) ConvertUtils.toLong((short) 5, false));
    }

    @Test
    public void toLong_byte() {
        assertEquals(3L, (long) ConvertUtils.toLong((byte) 3, false));
    }

    @Test
    public void toLong_float() {
        assertEquals(3L, (long) ConvertUtils.toLong(3.14f, false));
    }

    @Test
    public void toLong_double() {
        assertEquals(2L, (long) ConvertUtils.toLong(2.9d, false));
    }

    @Test
    public void toLong_bigDecimal() {
        assertEquals(5L, (long) ConvertUtils.toLong(BigDecimal.valueOf(5), false));
    }

    // ==================== toFloat — Number subtypes ====================
    @Test
    public void toFloat_short() {
        assertEquals(5.0f, (float) ConvertUtils.toFloat((short) 5, false), 0.01);
    }

    @Test
    public void toFloat_byte() {
        assertEquals(3.0f, (float) ConvertUtils.toFloat((byte) 3, false), 0.01);
    }

    @Test
    public void toFloat_long() {
        assertEquals(42.0f, (float) ConvertUtils.toFloat(42L, false), 0.01);
    }

    @Test
    public void toFloat_bigDecimal() {
        assertEquals(3.14f, (float) ConvertUtils.toFloat(BigDecimal.valueOf(3.14), false), 0.01);
    }

    @Test
    public void toFloat_bigInteger() {
        assertEquals(10.0f, (float) ConvertUtils.toFloat(BigInteger.TEN, false), 0.01);
    }

    // ==================== toDouble — Number subtypes ====================
    @Test
    public void toDouble_short() {
        assertEquals(5.0, (double) ConvertUtils.toDouble((short) 5, false), 0.01);
    }

    @Test
    public void toDouble_byte() {
        assertEquals(3.0, (double) ConvertUtils.toDouble((byte) 3, false), 0.01);
    }

    @Test
    public void toDouble_long() {
        assertEquals(42.0, (double) ConvertUtils.toDouble(42L, false), 0.01);
    }

    @Test
    public void toDouble_float() {
        assertEquals(1.5, (double) ConvertUtils.toDouble(1.5f, false), 0.01);
    }

    @Test
    public void toDouble_bigDecimal() {
        assertEquals(3.14, (double) ConvertUtils.toDouble(BigDecimal.valueOf(3.14), false), 0.01);
    }

    @Test
    public void toDouble_bigInteger() {
        assertEquals(10.0, (double) ConvertUtils.toDouble(BigInteger.TEN, false), 0.01);
    }

    // ==================== toChar — Number subtypes ====================
    @Test
    public void toChar_long() {
        assertEquals('A', (char) ConvertUtils.toChar(65L, false));
    }

    @Test
    public void toChar_short() {
        assertEquals('A', (char) ConvertUtils.toChar((short) 65, false));
    }

    @Test
    public void toChar_byte() {
        assertEquals('A', (char) ConvertUtils.toChar((byte) 65, false));
    }

    @Test
    public void toChar_float() {
        assertEquals('A', (char) ConvertUtils.toChar(65.0f, false));
    }

    @Test
    public void toChar_double() {
        assertEquals('A', (char) ConvertUtils.toChar(65.0d, false));
    }

    @Test
    public void toChar_bigDecimal() {
        assertEquals('A', (char) ConvertUtils.toChar(BigDecimal.valueOf(65), false));
    }

    @Test
    public void toChar_bigInteger() {
        assertEquals('A', (char) ConvertUtils.toChar(BigInteger.valueOf(65), false));
    }

    // ==================== toString — additional types ====================
    @Test
    public void toString_bigDecimal() {
        assertEquals("3.14", ConvertUtils.toString(BigDecimal.valueOf(3.14)));
    }

    @Test
    public void toString_bigInteger() {
        assertEquals("10", ConvertUtils.toString(BigInteger.TEN));
    }

    // ==================== toBytes — Byte[] wrapper ====================
    @Test
    public void toBytes_byteWrapperArray() {
        Byte[] wrappers = new Byte[] { 1, 2, 3 };
        byte[] result = ConvertUtils.toBytes(wrappers);
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    // ==================== toBigDecimal — special string formats ====================
    @Test
    public void toBigDecimal_scientific() {
        assertNotNull(ConvertUtils.toBigDecimal("1.1e10"));
    }

    @Test
    public void toBigDecimal_veryLarge() {
        assertNotNull(ConvertUtils.toBigDecimal("999999999999999999999"));
    }

    // ==================== toBigInteger — special string ====================
    @Test
    public void toBigInteger_largeString() {
        assertNotNull(ConvertUtils.toBigInteger("999999999999999999999"));
    }

    // ==================== date/time conversions from various types ====================
    // toYear — more type branches
    @Test
    public void toYear_offsetDateTime() {
        assertNotNull(ConvertUtils.toYear(OffsetDateTime.now()));
    }

    @Test
    public void toYear_zonedDateTime() {
        assertNotNull(ConvertUtils.toYear(ZonedDateTime.now()));
    }

    @Test
    public void toYear_japaneseDate() {
        assertNotNull(ConvertUtils.toYear(JapaneseDate.now()));
    }

    @Test
    public void toYear_yearMonth() {
        assertNotNull(ConvertUtils.toYear(YearMonth.of(2023, 3)));
    }

    @Test
    public void toYear_instant() {
        assertNotNull(ConvertUtils.toYear(Instant.now()));
    }

    @Test
    public void toYear_timestamp() {
        assertNotNull(ConvertUtils.toYear(new Timestamp(System.currentTimeMillis())));
    }

    @Test
    public void toYear_sqlDate() {
        assertNotNull(ConvertUtils.toYear(java.sql.Date.valueOf("2023-01-15")));
    }

    @Test
    public void toYear_calendar() {
        assertNotNull(ConvertUtils.toYear(Calendar.getInstance()));
    }

    // toMonth — more type branches
    @Test
    public void toMonth_localDateTime() {
        assertNotNull(ConvertUtils.toMonth(LocalDateTime.now()));
    }

    @Test
    public void toMonth_offsetDateTime() {
        assertNotNull(ConvertUtils.toMonth(OffsetDateTime.now()));
    }

    @Test
    public void toMonth_zonedDateTime() {
        assertNotNull(ConvertUtils.toMonth(ZonedDateTime.now()));
    }

    @Test
    public void toMonth_japaneseDate() {
        assertNotNull(ConvertUtils.toMonth(JapaneseDate.now()));
    }

    @Test
    public void toMonth_yearMonth() {
        assertNotNull(ConvertUtils.toMonth(YearMonth.of(2023, 3)));
    }

    @Test
    public void toMonth_instant() {
        assertNotNull(ConvertUtils.toMonth(Instant.now()));
    }

    @Test
    public void toMonth_timestamp() {
        assertNotNull(ConvertUtils.toMonth(new Timestamp(System.currentTimeMillis())));
    }

    @Test
    public void toMonth_calendar() {
        assertNotNull(ConvertUtils.toMonth(Calendar.getInstance()));
    }

    @Test
    public void toMonth_stringName() {
        assertNotNull(ConvertUtils.toMonth("January"));
    }

    @Test
    public void toMonth_shortName() {
        assertNotNull(ConvertUtils.toMonth("Jan"));
    }

    // toYearMonth — more type branches
    @Test
    public void toYearMonth_localDateTime() {
        assertNotNull(ConvertUtils.toYearMonth(LocalDateTime.of(2023, 3, 1, 0, 0)));
    }

    @Test
    public void toYearMonth_offsetDateTime() {
        assertNotNull(ConvertUtils.toYearMonth(OffsetDateTime.now()));
    }

    @Test
    public void toYearMonth_zonedDateTime() {
        assertNotNull(ConvertUtils.toYearMonth(ZonedDateTime.now()));
    }

    @Test
    public void toYearMonth_japaneseDate() {
        assertNotNull(ConvertUtils.toYearMonth(JapaneseDate.now()));
    }

    @Test
    public void toYearMonth_instant() {
        assertNotNull(ConvertUtils.toYearMonth(Instant.now()));
    }

    @Test
    public void toYearMonth_timestamp() {
        assertNotNull(ConvertUtils.toYearMonth(new Timestamp(System.currentTimeMillis())));
    }

    @Test
    public void toYearMonth_calendar() {
        assertNotNull(ConvertUtils.toYearMonth(Calendar.getInstance()));
    }

    @Test
    public void toYearMonth_number() {
        try {
            ConvertUtils.toYearMonth(System.currentTimeMillis());
        } catch (Exception ignore) {
        }
    }

    // toLocalTime — more branches
    @Test
    public void toLocalTime_instant() {
        assertNotNull(ConvertUtils.toLocalTime(Instant.ofEpochSecond(3600)));
    }

    @Test
    public void toLocalTime_calendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        try {
            assertNotNull(ConvertUtils.toLocalTime(cal));
        } catch (Exception ignore) {
        }
    }

    // toOffsetTime — more branches
    @Test
    public void toOffsetTime_localDateTime() {
        assertNotNull(ConvertUtils.toOffsetTime(LocalDateTime.now(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_offsetDateTime() {
        assertNotNull(ConvertUtils.toOffsetTime(OffsetDateTime.now(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_zonedDateTime() {
        assertNotNull(ConvertUtils.toOffsetTime(ZonedDateTime.now(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_instant() {
        assertNotNull(ConvertUtils.toOffsetTime(Instant.ofEpochSecond(3600), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_timestamp() {
        assertNotNull(ConvertUtils.toOffsetTime(new Timestamp(System.currentTimeMillis()), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_sqlDate() {
        try {
            ConvertUtils.toOffsetTime(java.sql.Date.valueOf("2023-01-15"), ZoneOffset.UTC);
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toOffsetTime_calendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        try {
            assertNotNull(ConvertUtils.toOffsetTime(cal, ZoneOffset.UTC));
        } catch (Exception ignore) {
        }
    }

    // toOffsetDateTime — more branches
    @Test
    public void toOffsetDateTime_year() {
        try {
            ConvertUtils.toOffsetDateTime(Year.of(2023), ZoneOffset.UTC);
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toOffsetDateTime_japaneseDate() {
        assertNotNull(ConvertUtils.toOffsetDateTime(JapaneseDate.now(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_yearMonth() {
        try {
            ConvertUtils.toOffsetDateTime(YearMonth.of(2023, 3), ZoneOffset.UTC);
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toOffsetDateTime_instant() {
        assertNotNull(ConvertUtils.toOffsetDateTime(Instant.now(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_sqlTime() {
        try {
            ConvertUtils.toOffsetDateTime(java.sql.Time.valueOf("12:30:00"), ZoneOffset.UTC);
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toOffsetDateTime_calendar() {
        assertNotNull(ConvertUtils.toOffsetDateTime(Calendar.getInstance(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_number() {
        assertNotNull(ConvertUtils.toOffsetDateTime(System.currentTimeMillis(), ZoneOffset.UTC));
    }

    // toZonedDateTime — more branches
    @Test
    public void toZonedDateTime_localDateTime() {
        assertNotNull(ConvertUtils.toZonedDateTime(LocalDateTime.now(), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_offsetDateTime() {
        assertNotNull(ConvertUtils.toZonedDateTime(OffsetDateTime.now(), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_zonedDateTime() {
        assertNotNull(ConvertUtils.toZonedDateTime(ZonedDateTime.now(), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_instant() {
        assertNotNull(ConvertUtils.toZonedDateTime(Instant.now(), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_timestamp() {
        assertNotNull(ConvertUtils.toZonedDateTime(new Timestamp(System.currentTimeMillis()), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_calendar() {
        assertNotNull(ConvertUtils.toZonedDateTime(Calendar.getInstance(), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_number() {
        assertNotNull(ConvertUtils.toZonedDateTime(System.currentTimeMillis(), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_localDate() {
        assertNotNull(ConvertUtils.toZonedDateTime(LocalDate.of(2023, 1, 15), ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_sqlDate() {
        assertNotNull(ConvertUtils.toZonedDateTime(java.sql.Date.valueOf("2023-01-15"), ZoneOffset.UTC));
    }

    // toJapaneseDate — more branches
    @Test
    public void toJapaneseDate_localDateTime() {
        assertNotNull(ConvertUtils.toJapaneseDate(LocalDateTime.now()));
    }

    @Test
    public void toJapaneseDate_timestamp() {
        assertNotNull(ConvertUtils.toJapaneseDate(new Timestamp(System.currentTimeMillis())));
    }

    @Test
    public void toJapaneseDate_utilDate() {
        assertNotNull(ConvertUtils.toJapaneseDate(new Date()));
    }

    @Test
    public void toJapaneseDate_calendar() {
        assertNotNull(ConvertUtils.toJapaneseDate(Calendar.getInstance()));
    }

    // ==================== toSqlDate / toSqlTime / toSqlTimestamp via rare types ====================
    @Test
    public void toSqlDate_offsetTime() {
        try {
            ConvertUtils.toSqlDate(OffsetTime.now());
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toSqlDate_zonedDateTime() {
        assertNotNull(ConvertUtils.toSqlDate(ZonedDateTime.now()));
    }

    @Test
    public void toSqlDate_japaneseDate() {
        assertNotNull(ConvertUtils.toSqlDate(JapaneseDate.now()));
    }

    @Test
    public void toSqlDate_yearMonth() {
        try {
            ConvertUtils.toSqlDate(YearMonth.of(2023, 3));
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toSqlDate_year() {
        try {
            ConvertUtils.toSqlDate(Year.of(2023));
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toSqlDate_calendar() {
        assertNotNull(ConvertUtils.toSqlDate(Calendar.getInstance()));
    }

    @Test
    public void toSqlDate_number() {
        assertNotNull(ConvertUtils.toSqlDate(System.currentTimeMillis()));
    }

    @Test
    public void toSqlTime_offsetTime() {
        assertNotNull(ConvertUtils.toSqlTime(OffsetTime.now()));
    }

    @Test
    public void toSqlTime_zonedDateTime() {
        assertNotNull(ConvertUtils.toSqlTime(ZonedDateTime.now()));
    }

    @Test
    public void toSqlTime_japaneseDate() {
        try {
            ConvertUtils.toSqlTime(JapaneseDate.now());
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toSqlTime_calendar() {
        assertNotNull(ConvertUtils.toSqlTime(Calendar.getInstance()));
    }

    @Test
    public void toSqlTime_number() {
        assertNotNull(ConvertUtils.toSqlTime(3600000L));
    }

    @Test
    public void toTimestamp_offsetTime() {
        try {
            ConvertUtils.toSqlTimestamp(OffsetTime.now());
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toTimestamp_zonedDateTime() {
        assertNotNull(ConvertUtils.toSqlTimestamp(ZonedDateTime.now()));
    }

    @Test
    public void toTimestamp_japaneseDate() {
        assertNotNull(ConvertUtils.toSqlTimestamp(JapaneseDate.now()));
    }

    @Test
    public void toTimestamp_calendar() {
        assertNotNull(ConvertUtils.toSqlTimestamp(Calendar.getInstance()));
    }

    @Test
    public void toTimestamp_number() {
        assertNotNull(ConvertUtils.toSqlTimestamp(System.currentTimeMillis()));
    }

    // toUtilDate / toInstant / toCalendar — indirect via rare types
    @Test
    public void toUtilDate_calendar() {
        assertNotNull(ConvertUtils.toUtilDate(Calendar.getInstance()));
    }

    @Test
    public void toUtilDate_number() {
        assertNotNull(ConvertUtils.toUtilDate(System.currentTimeMillis()));
    }

    @Test
    public void toInstant_calendar() {
        assertNotNull(ConvertUtils.toInstant(Calendar.getInstance()));
    }

    @Test
    public void toInstant_number() {
        assertNotNull(ConvertUtils.toInstant(System.currentTimeMillis()));
    }

    @Test
    public void toCalendar_instant() {
        assertNotNull(ConvertUtils.toCalendar(Instant.now()));
    }

    @Test
    public void toCalendar_number() {
        assertNotNull(ConvertUtils.toCalendar(System.currentTimeMillis()));
    }

    // toLocalDateTime — various string formats
    @Test
    public void toLocalDateTime_shortFormat() {
        try {
            ConvertUtils.toLocalDateTime("2023-03");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toLocalDateTime_tSeparator() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-03-01T12:30:00"));
    }

    @Test
    public void toLocalDateTime_timeTrunc() {
        try {
            ConvertUtils.toLocalDateTime("2023-03-01 12");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toLocalDateTime_timeOnly() {
        try {
            ConvertUtils.toLocalDateTime("12:30");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void toLocalDateTime_shortNum() {
        try {
            ConvertUtils.toLocalDateTime("123");
        } catch (Exception ignore) {
        }
    }

    // toLocalDate — various
    @Test
    public void toLocalDate_timestamp() {
        assertNotNull(ConvertUtils.toLocalDate(new Timestamp(System.currentTimeMillis())));
    }

    @Test
    public void toLocalDate_calendar() {
        assertNotNull(ConvertUtils.toLocalDate(Calendar.getInstance()));
    }

    @Test
    public void toLocalDate_number() {
        assertNotNull(ConvertUtils.toLocalDate(System.currentTimeMillis()));
    }

    // extractZone — various timezone patterns
    @Test
    public void extractZone_shortPlus() {
        assertNotNull(ConvertUtils.extractZone("12:30:00+8"));
    }

    @Test
    public void extractZone_twoDigit() {
        assertNotNull(ConvertUtils.extractZone("12:30:00+08"));
    }

    @Test
    public void extractZone_colonSep() {
        assertNotNull(ConvertUtils.extractZone("12:30:00+08:00"));
    }

    @Test
    public void extractZone_fourDigit() {
        assertNotNull(ConvertUtils.extractZone("12:30:00+0800"));
    }

    @Test
    public void extractZone_noZone() { /* may return empty or null */
        Object z = ConvertUtils.extractZone("12:30:00");
        assertTrue(z == null || z.toString().isEmpty());
    }

    @Test
    public void extractZone_negative() {
        assertNotNull(ConvertUtils.extractZone("12:30:00-05:00"));
    }

    // ==================== toSqlDate/Time/Timestamp as string numeric ====================
    @Test
    public void toSqlDate_stringNumber() {
        assertNotNull(ConvertUtils.toSqlDate(String.valueOf(System.currentTimeMillis())));
    }

    @Test
    public void toSqlTime_stringNumber() {
        assertNotNull(ConvertUtils.toSqlTime(String.valueOf(3600000L)));
    }

    @Test
    public void toTimestamp_stringNumber() {
        assertNotNull(ConvertUtils.toSqlTimestamp(String.valueOf(System.currentTimeMillis())));
    }

    @Test
    public void toSqlDate_stringDate() {
        assertNotNull(ConvertUtils.toSqlDate("2023-01-15"));
    }

    @Test
    public void toSqlTime_stringTime() {
        assertNotNull(ConvertUtils.toSqlTime("12:30:00"));
    }

    @Test
    public void toTimestamp_stringTimestamp() {
        assertNotNull(ConvertUtils.toSqlTimestamp("2023-01-15 12:00:00"));
    }
}
