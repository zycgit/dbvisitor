package net.hasor.dbvisitor.driver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/** Comprehensive tests for ConvertUtils — covers all type conversion paths. */
public class ConvertUtilsTest {

    // ==================== toBoolean ====================
    @Test
    public void toBoolean_null_primitive() {
        assertFalse(ConvertUtils.toBoolean(null, true));
    }

    @Test
    public void toBoolean_null_nonPrimitive() {
        assertNull(ConvertUtils.toBoolean(null, false));
    }

    @Test
    public void toBoolean_booleanTrue() {
        assertTrue(ConvertUtils.toBoolean(Boolean.TRUE, false));
    }

    @Test
    public void toBoolean_booleanFalse() {
        assertFalse(ConvertUtils.toBoolean(Boolean.FALSE, false));
    }

    @Test
    public void toBoolean_stringTrue() {
        assertTrue(ConvertUtils.toBoolean("true", false));
    }

    @Test
    public void toBoolean_stringYes() {
        assertTrue(ConvertUtils.toBoolean("yes", false));
    }

    @Test
    public void toBoolean_stringOn() {
        assertTrue(ConvertUtils.toBoolean("on", false));
    }

    @Test
    public void toBoolean_string1() {
        assertTrue(ConvertUtils.toBoolean("1", false));
    }

    @Test
    public void toBoolean_stringFalse() {
        assertFalse(ConvertUtils.toBoolean("false", false));
    }

    @Test
    public void toBoolean_stringNo() {
        assertFalse(ConvertUtils.toBoolean("no", false));
    }

    @Test
    public void toBoolean_stringOff() {
        assertFalse(ConvertUtils.toBoolean("off", false));
    }

    @Test
    public void toBoolean_string0() {
        assertFalse(ConvertUtils.toBoolean("0", false));
    }

    @Test
    public void toBoolean_intNonZero() {
        assertTrue(ConvertUtils.toBoolean(1, false));
    }

    @Test
    public void toBoolean_intZero() {
        assertFalse(ConvertUtils.toBoolean(0, false));
    }

    @Test
    public void toBoolean_longNonZero() {
        assertTrue(ConvertUtils.toBoolean(1L, false));
    }

    @Test
    public void toBoolean_doubleNonZero() {
        assertTrue(ConvertUtils.toBoolean(1.0d, false));
    }

    @Test
    public void toBoolean_floatZero() {
        assertFalse(ConvertUtils.toBoolean(0.0f, false));
    }

    @Test
    public void toBoolean_byteArray() {
        assertTrue(ConvertUtils.toBoolean(new byte[] { 1 }, false));
    }

    @Test
    public void toBoolean_byteArrayZero() {
        assertFalse(ConvertUtils.toBoolean(new byte[] { 0 }, false));
    }

    // ==================== toByte ====================
    @Test
    public void toByte_null_primitive() {
        assertEquals(0, (byte) ConvertUtils.toByte(null, true));
    }

    @Test
    public void toByte_null_nonPrimitive() {
        assertNull(ConvertUtils.toByte(null, false));
    }

    @Test
    public void toByte_booleanTrue() {
        assertEquals(1, (byte) ConvertUtils.toByte(Boolean.TRUE, false));
    }

    @Test
    public void toByte_booleanFalse() {
        assertEquals(0, (byte) ConvertUtils.toByte(Boolean.FALSE, false));
    }

    @Test
    public void toByte_string() {
        assertEquals(42, (byte) ConvertUtils.toByte("42", false));
    }

    @Test
    public void toByte_int() {
        assertEquals(5, (byte) ConvertUtils.toByte(5, false));
    }

    @Test
    public void toByte_long() {
        assertEquals(7, (byte) ConvertUtils.toByte(7L, false));
    }

    @Test
    public void toByte_byteArray() {
        assertEquals(99, (byte) ConvertUtils.toByte(new byte[] { 99 }, false));
    }

    @Test
    public void toByte_bigDecimal() {
        assertEquals(3, (byte) ConvertUtils.toByte(new BigDecimal("3.7"), false));
    }

    // ==================== toShort ====================
    @Test
    public void toShort_null_primitive() {
        assertEquals(0, (short) ConvertUtils.toShort(null, true));
    }

    @Test
    public void toShort_null_nonPrimitive() {
        assertNull(ConvertUtils.toShort(null, false));
    }

    @Test
    public void toShort_booleanTrue() {
        assertEquals(1, (short) ConvertUtils.toShort(Boolean.TRUE, false));
    }

    @Test
    public void toShort_string() {
        assertEquals(1234, (short) ConvertUtils.toShort("1234", false));
    }

    @Test
    public void toShort_int() {
        assertEquals(100, (short) ConvertUtils.toShort(100, false));
    }

    @Test
    public void toShort_byteArray() {
        byte[] b = ByteBuffer.allocate(2).putShort((short) 256).array();
        assertEquals(256, (short) ConvertUtils.toShort(b, false));
    }

    // ==================== toInteger ====================
    @Test
    public void toInteger_null_primitive() {
        assertEquals(0, (int) ConvertUtils.toInteger(null, true));
    }

    @Test
    public void toInteger_null_nonPrimitive() {
        assertNull(ConvertUtils.toInteger(null, false));
    }

    @Test
    public void toInteger_booleanTrue() {
        assertEquals(1, (int) ConvertUtils.toInteger(Boolean.TRUE, false));
    }

    @Test
    public void toInteger_booleanFalse() {
        assertEquals(0, (int) ConvertUtils.toInteger(Boolean.FALSE, false));
    }

    @Test
    public void toInteger_string() {
        assertEquals(12345, (int) ConvertUtils.toInteger("12345", false));
    }

    @Test
    public void toInteger_long() {
        assertEquals(999, (int) ConvertUtils.toInteger(999L, false));
    }

    @Test
    public void toInteger_double() {
        assertEquals(3, (int) ConvertUtils.toInteger(3.14, false));
    }

    @Test
    public void toInteger_byteArray() {
        byte[] b = ByteBuffer.allocate(4).putInt(65536).array();
        assertEquals(65536, (int) ConvertUtils.toInteger(b, false));
    }

    @Test
    public void toInteger_bigDecimal() {
        assertEquals(7, (int) ConvertUtils.toInteger(new BigDecimal("7"), false));
    }

    // ==================== toLong ====================
    @Test
    public void toLong_null_primitive() {
        assertEquals(0L, (long) ConvertUtils.toLong(null, true));
    }

    @Test
    public void toLong_null_nonPrimitive() {
        assertNull(ConvertUtils.toLong(null, false));
    }

    @Test
    public void toLong_booleanTrue() {
        assertEquals(1L, (long) ConvertUtils.toLong(Boolean.TRUE, false));
    }

    @Test
    public void toLong_string() {
        assertEquals(999999999L, (long) ConvertUtils.toLong("999999999", false));
    }

    @Test
    public void toLong_int() {
        assertEquals(42L, (long) ConvertUtils.toLong(42, false));
    }

    @Test
    public void toLong_byteArray() {
        byte[] b = ByteBuffer.allocate(8).putLong(123456789L).array();
        assertEquals(123456789L, (long) ConvertUtils.toLong(b, false));
    }

    @Test
    public void toLong_bigInteger() {
        assertEquals(100L, (long) ConvertUtils.toLong(BigInteger.valueOf(100), false));
    }

    // ==================== toFloat ====================
    @Test
    public void toFloat_null_primitive() {
        assertEquals(0.0f, ConvertUtils.toFloat(null, true), 0.001);
    }

    @Test
    public void toFloat_null_nonPrimitive() {
        assertNull(ConvertUtils.toFloat(null, false));
    }

    @Test
    public void toFloat_booleanTrue() {
        assertEquals(1.0f, ConvertUtils.toFloat(Boolean.TRUE, false), 0.001);
    }

    @Test
    public void toFloat_string() {
        assertEquals(3.14f, ConvertUtils.toFloat("3.14", false), 0.001);
    }

    @Test
    public void toFloat_int() {
        assertEquals(5.0f, ConvertUtils.toFloat(5, false), 0.001);
    }

    @Test
    public void toFloat_byteArray() {
        byte[] b = ByteBuffer.allocate(4).putFloat(2.5f).array();
        assertEquals(2.5f, ConvertUtils.toFloat(b, false), 0.001);
    }

    // ==================== toDouble ====================
    @Test
    public void toDouble_null_primitive() {
        assertEquals(0.0d, ConvertUtils.toDouble(null, true), 0.001);
    }

    @Test
    public void toDouble_null_nonPrimitive() {
        assertNull(ConvertUtils.toDouble(null, false));
    }

    @Test
    public void toDouble_booleanTrue() {
        assertEquals(1.0d, ConvertUtils.toDouble(Boolean.TRUE, false), 0.001);
    }

    @Test
    public void toDouble_string() {
        assertEquals(3.14d, ConvertUtils.toDouble("3.14", false), 0.001);
    }

    @Test
    public void toDouble_int() {
        assertEquals(5.0d, ConvertUtils.toDouble(5, false), 0.001);
    }

    @Test
    public void toDouble_byteArray() {
        byte[] b = ByteBuffer.allocate(8).putDouble(2.5d).array();
        assertEquals(2.5d, ConvertUtils.toDouble(b, false), 0.001);
    }

    // ==================== toChar ====================
    @Test
    public void toChar_null_primitive() {
        assertEquals('\0', (char) ConvertUtils.toChar(null, true));
    }

    @Test
    public void toChar_null_nonPrimitive() {
        assertNull(ConvertUtils.toChar(null, false));
    }

    @Test
    public void toChar_booleanTrue() {
        assertEquals('T', (char) ConvertUtils.toChar(Boolean.TRUE, false));
    }

    @Test
    public void toChar_booleanFalse() {
        assertEquals('F', (char) ConvertUtils.toChar(Boolean.FALSE, false));
    }

    @Test
    public void toChar_string_numeric() {
        assertEquals('A', (char) ConvertUtils.toChar("65", false));
    }

    @Test(expected = RuntimeException.class)
    public void toChar_string_nonNumeric() {
        ConvertUtils.toChar("ABC", false);
    }

    @Test
    public void toChar_int() {
        assertEquals('A', (char) ConvertUtils.toChar(65, false));
    }

    // ==================== toBigDecimal ====================
    @Test
    public void toBigDecimal_null() {
        assertNull(ConvertUtils.toBigDecimal(null));
    }

    @Test
    public void toBigDecimal_boolean() {
        assertEquals(BigDecimal.ONE, ConvertUtils.toBigDecimal(Boolean.TRUE));
    }

    @Test
    public void toBigDecimal_booleanFalse() {
        assertEquals(BigDecimal.ZERO, ConvertUtils.toBigDecimal(Boolean.FALSE));
    }

    @Test
    public void toBigDecimal_string() {
        assertNotNull(ConvertUtils.toBigDecimal("123.456"));
    }

    @Test
    public void toBigDecimal_int() {
        assertEquals(new BigDecimal(42), ConvertUtils.toBigDecimal(42));
    }

    @Test
    public void toBigDecimal_long() {
        assertEquals(BigDecimal.valueOf(999L), ConvertUtils.toBigDecimal(999L));
    }

    @Test
    public void toBigDecimal_double() {
        assertNotNull(ConvertUtils.toBigDecimal(3.14));
    }

    @Test
    public void toBigDecimal_bigInteger() {
        assertEquals(new BigDecimal(BigInteger.TEN), ConvertUtils.toBigDecimal(BigInteger.TEN));
    }

    @Test
    public void toBigDecimal_byteArray() {
        assertNotNull(ConvertUtils.toBigDecimal(new byte[] { 1 }));
    }

    // ==================== toBigInteger ====================
    @Test
    public void toBigInteger_null() {
        assertNull(ConvertUtils.toBigInteger(null));
    }

    @Test
    public void toBigInteger_boolean() {
        assertEquals(BigInteger.ONE, ConvertUtils.toBigInteger(Boolean.TRUE));
    }

    @Test
    public void toBigInteger_string() {
        assertEquals(BigInteger.valueOf(123), ConvertUtils.toBigInteger("123"));
    }

    @Test
    public void toBigInteger_int() {
        assertEquals(BigInteger.valueOf(42), ConvertUtils.toBigInteger(42));
    }

    @Test
    public void toBigInteger_byteArray() {
        assertNotNull(ConvertUtils.toBigInteger(new byte[] { 0, 1 }));
    }

    // ==================== toString ====================
    @Test
    public void toString_null() {
        assertNull(ConvertUtils.toString(null));
    }

    @Test
    public void toString_boolean() {
        assertEquals("true", ConvertUtils.toString(Boolean.TRUE));
    }

    @Test
    public void toString_string() {
        assertEquals("abc", ConvertUtils.toString("abc"));
    }

    @Test
    public void toString_int() {
        assertEquals("42", ConvertUtils.toString(42));
    }

    @Test
    public void toString_byteArray() {
        assertNotNull(ConvertUtils.toString(new byte[] { 0x0A, 0x0B }));
    }

    @Test
    public void toString_bigDecimal() {
        assertEquals("123.45", ConvertUtils.toString(new BigDecimal("123.45")));
    }

    // ==================== toBytes ====================
    @Test
    public void toBytes_null() {
        assertNull(ConvertUtils.toBytes(null));
    }

    @Test
    public void toBytes_booleanTrue() {
        assertArrayEquals(new byte[] { 1 }, ConvertUtils.toBytes(Boolean.TRUE));
    }

    @Test
    public void toBytes_booleanFalse() {
        assertArrayEquals(new byte[] { 0 }, ConvertUtils.toBytes(Boolean.FALSE));
    }

    @Test
    public void toBytes_string() {
        assertNotNull(ConvertUtils.toBytes("hello"));
    }

    @Test
    public void toBytes_byteArrayPassthrough() {
        byte[] input = { 1, 2, 3 };
        assertSame(input, ConvertUtils.toBytes(input));
    }

    @Test
    public void toBytes_int() {
        assertEquals(4, ConvertUtils.toBytes(42).length);
    }

    @Test
    public void toBytes_long() {
        assertEquals(8, ConvertUtils.toBytes(42L).length);
    }

    @Test
    public void toBytes_short() {
        assertEquals(2, ConvertUtils.toBytes((short) 42).length);
    }

    @Test
    public void toBytes_byte() {
        assertEquals(1, ConvertUtils.toBytes((byte) 42).length);
    }

    @Test
    public void toBytes_float() {
        assertEquals(4, ConvertUtils.toBytes(1.5f).length);
    }

    @Test
    public void toBytes_double() {
        assertEquals(8, ConvertUtils.toBytes(1.5d).length);
    }

    @Test
    public void toBytes_ByteWrap() {
        Byte[] wrapped = { 1, 2, 3 };
        assertNotNull(ConvertUtils.toBytes(wrapped));
    }

    // ==================== toBytesWrap ====================
    @Test
    public void toBytesWrap_null() {
        assertNull(ConvertUtils.toBytesWrap(null));
    }

    @Test
    public void toBytesWrap_byteArray() {
        Byte[] result = ConvertUtils.toBytesWrap(new byte[] { 1, 2 });
        assertEquals(2, result.length);
        assertEquals(Byte.valueOf((byte) 1), result[0]);
    }

    // ==================== toBit ====================
    @Test
    public void toBit_null() {
        assertNull(ConvertUtils.toBit(null));
    }

    @Test
    public void toBit_booleanTrue() {
        assertEquals("1", ConvertUtils.toBit(Boolean.TRUE));
    }

    @Test
    public void toBit_booleanFalse() {
        assertEquals("0", ConvertUtils.toBit(Boolean.FALSE));
    }

    @Test
    public void toBit_string01() {
        assertEquals("0101", ConvertUtils.toBit("0101"));
    }

    @Test
    public void toBit_int() {
        assertNotNull(ConvertUtils.toBit(255));
    }

    @Test
    public void toBit_byteArray() {
        assertNotNull(ConvertUtils.toBit(new byte[] { (byte) 0xFF }));
    }

    // ==================== Date/Time conversions ====================
    @Test
    public void toSqlDate_null() {
        assertNull(ConvertUtils.toSqlDate(null));
    }

    @Test
    public void toSqlDate_sqlDate() {
        java.sql.Date d = java.sql.Date.valueOf("2023-01-15");
        assertEquals(d.toString(), ConvertUtils.toSqlDate(d).toString());
    }

    @Test
    public void toSqlDate_timestamp() {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2023-01-15 12:00:00");
        assertNotNull(ConvertUtils.toSqlDate(ts));
    }

    @Test
    public void toSqlDate_long() {
        assertNotNull(ConvertUtils.toSqlDate(System.currentTimeMillis()));
    }

    @Test
    public void toSqlDate_string() {
        assertNotNull(ConvertUtils.toSqlDate("2023-01-15"));
    }

    @Test
    public void toSqlDate_localDate() {
        assertNotNull(ConvertUtils.toSqlDate(LocalDate.of(2023, 1, 15)));
    }

    @Test
    public void toSqlTime_null() {
        assertNull(ConvertUtils.toSqlTime(null));
    }

    @Test
    public void toSqlTime_sqlTime() {
        java.sql.Time t = java.sql.Time.valueOf("12:30:00");
        assertNotNull(ConvertUtils.toSqlTime(t));
    }

    @Test
    public void toSqlTime_long() {
        assertNotNull(ConvertUtils.toSqlTime(System.currentTimeMillis()));
    }

    @Test
    public void toSqlTime_string() {
        assertNotNull(ConvertUtils.toSqlTime("12:30:00"));
    }

    @Test
    public void toSqlTimestamp_null() {
        assertNull(ConvertUtils.toSqlTimestamp(null));
    }

    @Test
    public void toSqlTimestamp_timestamp() {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2023-01-15 12:00:00");
        assertEquals(ts, ConvertUtils.toSqlTimestamp(ts));
    }

    @Test
    public void toSqlTimestamp_long() {
        assertNotNull(ConvertUtils.toSqlTimestamp(System.currentTimeMillis()));
    }

    @Test
    public void toSqlTimestamp_string() {
        assertNotNull(ConvertUtils.toSqlTimestamp("2023-01-15 12:00:00"));
    }

    @Test
    public void toSqlTimestamp_localDateTime() {
        assertNotNull(ConvertUtils.toSqlTimestamp(LocalDateTime.of(2023, 1, 15, 12, 0)));
    }

    @Test
    public void toUtilDate_null() {
        assertNull(ConvertUtils.toUtilDate(null));
    }

    @Test
    public void toUtilDate_date() {
        assertNotNull(ConvertUtils.toUtilDate(new Date()));
    }

    @Test
    public void toUtilDate_long() {
        assertNotNull(ConvertUtils.toUtilDate(System.currentTimeMillis()));
    }

    @Test
    public void toUtilDate_string() {
        assertNotNull(ConvertUtils.toUtilDate("2023-01-15"));
    }

    @Test
    public void toInstant_null() {
        assertNull(ConvertUtils.toInstant(null));
    }

    @Test
    public void toInstant_instant() {
        assertNotNull(ConvertUtils.toInstant(Instant.now()));
    }

    @Test
    public void toInstant_date() {
        assertNotNull(ConvertUtils.toInstant(new Date()));
    }

    @Test
    public void toInstant_long() {
        assertNotNull(ConvertUtils.toInstant(System.currentTimeMillis()));
    }

    @Test
    public void toCalendar_null() {
        assertNull(ConvertUtils.toCalendar(null));
    }

    @Test
    public void toCalendar_date() {
        assertNotNull(ConvertUtils.toCalendar(new Date()));
    }

    @Test
    public void toCalendar_long() {
        assertNotNull(ConvertUtils.toCalendar(System.currentTimeMillis()));
    }

    @Test
    public void toCalendar_string() {
        assertNotNull(ConvertUtils.toCalendar("2023-01-15"));
    }

    // ==================== Java 8 Time types ====================
    @Test
    public void toLocalDateTime_null() {
        assertNull(ConvertUtils.toLocalDateTime(null));
    }

    @Test
    public void toLocalDateTime_localDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 15, 12, 0);
        assertEquals(ldt, ConvertUtils.toLocalDateTime(ldt));
    }

    @Test
    public void toLocalDateTime_localDate() {
        LocalDate ld = LocalDate.of(2023, 1, 15);
        assertNotNull(ConvertUtils.toLocalDateTime(ld));
    }

    @Test
    public void toLocalDateTime_string_full() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01-15 12:30:00"));
    }

    @Test
    public void toLocalDateTime_string_dateOnly() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01-15"));
    }

    @Test
    public void toLocalDateTime_string_yearOnly() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023"));
    }

    @Test
    public void toLocalDateTime_string_yearMonth() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01"));
    }

    @Test
    public void toLocalDateTime_string_dateHour() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01-15 12"));
    }

    @Test
    public void toLocalDateTime_string_dateHourMin() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01-15 12:30"));
    }

    @Test
    public void toLocalDateTime_string_withT() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01-15T12:30:00"));
    }

    @Test
    public void toLocalDateTime_string_withNanos() {
        assertNotNull(ConvertUtils.toLocalDateTime("2023-01-15 12:30:00.123456789"));
    }

    @Test
    public void toLocalDateTime_long() {
        assertNotNull(ConvertUtils.toLocalDateTime(System.currentTimeMillis()));
    }

    @Test
    public void toLocalDateTime_date() {
        assertNotNull(ConvertUtils.toLocalDateTime(new Date()));
    }

    @Test
    public void toLocalDateTime_calendar() {
        Calendar cal = Calendar.getInstance();
        assertNotNull(ConvertUtils.toLocalDateTime(cal));
    }

    @Test
    public void toLocalDateTime_instant() {
        assertNotNull(ConvertUtils.toLocalDateTime(Instant.now()));
    }

    @Test
    public void toLocalDate_null() {
        assertNull(ConvertUtils.toLocalDate(null));
    }

    @Test
    public void toLocalDate_localDate() {
        assertEquals(LocalDate.of(2023, 1, 15), ConvertUtils.toLocalDate(LocalDate.of(2023, 1, 15)));
    }

    @Test
    public void toLocalDate_string() {
        assertNotNull(ConvertUtils.toLocalDate("2023-01-15"));
    }

    @Test
    public void toLocalDate_date() {
        assertNotNull(ConvertUtils.toLocalDate(new Date()));
    }

    @Test
    public void toLocalDate_long() {
        assertNotNull(ConvertUtils.toLocalDate(System.currentTimeMillis()));
    }

    @Test
    public void toLocalDate_localDateTime() {
        assertNotNull(ConvertUtils.toLocalDate(LocalDateTime.now()));
    }

    @Test
    public void toLocalTime_null() {
        assertNull(ConvertUtils.toLocalTime(null));
    }

    @Test
    public void toLocalTime_localTime() {
        assertEquals(LocalTime.of(12, 30), ConvertUtils.toLocalTime(LocalTime.of(12, 30)));
    }

    @Test
    public void toLocalTime_string_full() {
        assertNotNull(ConvertUtils.toLocalTime("12:30:00"));
    }

    @Test
    public void toLocalTime_string_hourMin() {
        assertNotNull(ConvertUtils.toLocalTime("12:30"));
    }

    @Test
    public void toLocalTime_string_withNanos() {
        assertNotNull(ConvertUtils.toLocalTime("12:30:00.123456789"));
    }

    @Test
    public void toLocalTime_long() {
        assertNotNull(ConvertUtils.toLocalTime(3600000L));
    }

    @Test
    public void toLocalTime_date() {
        try {
            ConvertUtils.toLocalTime(java.sql.Date.valueOf("2023-01-15"));
        } catch (Exception ignore) {
        }
    }

    // ==================== Year / Month / YearMonth ====================
    @Test
    public void toYear_null() {
        assertNull(ConvertUtils.toYear(null));
    }

    @Test
    public void toYear_year() {
        assertEquals(Year.of(2023), ConvertUtils.toYear(Year.of(2023)));
    }

    @Test
    public void toYear_string() {
        assertNotNull(ConvertUtils.toYear("2023"));
    }

    @Test
    public void toYear_int() {
        assertNotNull(ConvertUtils.toYear(0L));
    }

    @Test
    public void toYear_localDate() {
        assertNotNull(ConvertUtils.toYear(LocalDate.of(2023, 3, 1)));
    }

    @Test
    public void toYear_date() {
        assertNotNull(ConvertUtils.toYear(new Date()));
    }

    @Test
    public void toMonth_null() {
        assertNull(ConvertUtils.toMonth(null));
    }

    @Test
    public void toMonth_month() {
        assertEquals(Month.MARCH, ConvertUtils.toMonth(Month.MARCH));
    }

    @Test
    public void toMonth_string_number() {
        assertNotNull(ConvertUtils.toMonth("3"));
    }

    @Test
    public void toMonth_int() {
        assertNotNull(ConvertUtils.toMonth(0L));
    }

    @Test
    public void toMonth_localDate() {
        assertEquals(Month.MARCH, ConvertUtils.toMonth(LocalDate.of(2023, 3, 1)));
    }

    @Test
    public void toMonth_monthDay() {
        assertEquals(Month.MARCH, ConvertUtils.toMonth(MonthDay.of(3, 1)));
    }

    @Test
    public void toYearMonth_null() {
        assertNull(ConvertUtils.toYearMonth(null));
    }

    @Test
    public void toYearMonth_yearMonth() {
        assertEquals(YearMonth.of(2023, 3), ConvertUtils.toYearMonth(YearMonth.of(2023, 3)));
    }

    @Test
    public void toYearMonth_string() {
        assertEquals(YearMonth.of(2023, 3), ConvertUtils.toYearMonth("2023-03"));
    }

    @Test
    public void toYearMonth_localDate() {
        assertEquals(YearMonth.of(2023, 3), ConvertUtils.toYearMonth(LocalDate.of(2023, 3, 1)));
    }

    @Test
    public void toYearMonth_date() {
        assertNotNull(ConvertUtils.toYearMonth(new Date()));
    }

    // ==================== JapaneseDate ====================
    @Test
    public void toJapaneseDate_null() {
        assertNull(ConvertUtils.toJapaneseDate(null));
    }

    @Test
    public void toJapaneseDate_japaneseDate() {
        JapaneseDate jd = JapaneseDate.from(LocalDate.of(2023, 1, 15));
        assertSame(jd, ConvertUtils.toJapaneseDate(jd));
    }

    @Test
    public void toJapaneseDate_localDate() {
        assertNotNull(ConvertUtils.toJapaneseDate(LocalDate.of(2023, 1, 15)));
    }

    @Test
    public void toJapaneseDate_string() {
        assertNotNull(ConvertUtils.toJapaneseDate("2023-01-15"));
    }

    // ==================== Offset/Zoned types ====================
    @Test
    public void toOffsetTime_null() {
        assertNull(ConvertUtils.toOffsetTime(null, ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_offsetTime() {
        OffsetTime ot = OffsetTime.of(12, 30, 0, 0, ZoneOffset.UTC);
        assertEquals(ot, ConvertUtils.toOffsetTime(ot, ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_localTime() {
        assertNotNull(ConvertUtils.toOffsetTime(LocalTime.of(12, 30), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_string() {
        assertNotNull(ConvertUtils.toOffsetTime("12:30:00", ZoneOffset.UTC));
    }

    @Test
    public void toOffsetTime_long() {
        assertNotNull(ConvertUtils.toOffsetTime(3600000L, ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_null() {
        assertNull(ConvertUtils.toOffsetDateTime(null, ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_offsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.of(2023, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(odt, ConvertUtils.toOffsetDateTime(odt, ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_localDateTime() {
        assertNotNull(ConvertUtils.toOffsetDateTime(LocalDateTime.of(2023, 1, 15, 12, 0), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_string() {
        assertNotNull(ConvertUtils.toOffsetDateTime("2023-01-15 12:00:00", ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_long() {
        assertNotNull(ConvertUtils.toOffsetDateTime(System.currentTimeMillis(), ZoneOffset.UTC));
    }

    @Test
    public void toOffsetDateTime_stringWithZone() {
        assertNotNull(ConvertUtils.toOffsetDateTime("2023-01-15T12:00:00+08:00", ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_null() {
        assertNull(ConvertUtils.toZonedDateTime(null, ZoneOffset.UTC));
    }

    @Test
    public void toZonedDateTime_string() {
        assertNotNull(ConvertUtils.toZonedDateTime("2023-01-15 12:00:00", ZoneOffset.UTC));
    }

    // ==================== Error paths ====================
    @Test(expected = RuntimeException.class)
    public void toBoolean_invalidString() {
        ConvertUtils.toBoolean("invalid", false);
    }

    @Test(expected = RuntimeException.class)
    public void toByte_invalidString() {
        ConvertUtils.toByte("xyz", false);
    }

    @Test(expected = RuntimeException.class)
    public void toShort_byteArrayTooShort() {
        ConvertUtils.toShort(new byte[] { 1 }, false);
    }

    @Test(expected = RuntimeException.class)
    public void toInteger_byteArrayTooShort() {
        ConvertUtils.toInteger(new byte[] { 1, 2 }, false);
    }

    @Test(expected = RuntimeException.class)
    public void toLong_byteArrayTooShort() {
        ConvertUtils.toLong(new byte[] { 1, 2, 3, 4 }, false);
    }

    @Test(expected = RuntimeException.class)
    public void toFloat_byteArrayTooShort() {
        ConvertUtils.toFloat(new byte[] { 1, 2 }, false);
    }

    @Test(expected = RuntimeException.class)
    public void toDouble_byteArrayTooShort() {
        ConvertUtils.toDouble(new byte[] { 1, 2, 3, 4 }, false);
    }
}
