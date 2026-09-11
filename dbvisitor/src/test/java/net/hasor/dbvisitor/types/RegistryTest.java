/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.concurrent.atomic.LongAccumulator;
import net.hasor.cobble.ArrayUtils;
import net.hasor.dbvisitor.dynamic.ArgRuleTest;
import net.hasor.dbvisitor.types.handler.UnknownTypeHandler;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;
import net.hasor.dbvisitor.types.handler.bool.BooleanTypeHandler;
import net.hasor.dbvisitor.types.handler.bytes.BlobAsBytesTypeHandler;
import net.hasor.dbvisitor.types.handler.bytes.BytesAsBytesWrapTypeHandler;
import net.hasor.dbvisitor.types.handler.bytes.BytesTypeHandler;
import net.hasor.dbvisitor.types.handler.io.BytesAsInputStreamTypeHandler;
import net.hasor.dbvisitor.types.handler.io.StringAsReaderTypeHandler;
import net.hasor.dbvisitor.types.handler.number.*;
import net.hasor.dbvisitor.types.handler.string.*;
import net.hasor.dbvisitor.types.handler.time.*;
import org.junit.Test;
import org.mockito.Mockito;

public class RegistryTest {

    @Test
    public void ruleTest_1() throws Exception {
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Boolean.TRUE.getClass()) instanceof BooleanTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Byte.class) instanceof ByteTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Short.class) instanceof ShortTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Integer.class) instanceof IntegerTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Long.class) instanceof LongTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Float.class) instanceof FloatTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Double.class) instanceof DoubleTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Character.class) instanceof StringAsCharTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(java.util.Date.class) instanceof SqlTimestampAsDateTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(java.sql.Date.class) instanceof SqlDateTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(java.sql.Timestamp.class) instanceof SqlTimestampTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(java.sql.Time.class) instanceof SqlTimeTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Instant.now().getClass()) instanceof SqlTimestampAsInstantTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(JapaneseDate.now().getClass()) instanceof JapaneseDateAsSqlDateTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Year.of(2022).getClass()) instanceof SqlTimestampAsYearTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Month.MAY.getClass()) instanceof SqlTimestampAsMonthTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(YearMonth.now().getClass()) instanceof SqlTimestampAsYearMonthTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(MonthDay.now().getClass()) instanceof SqlTimestampAsMonthDayTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(LocalDate.now().getClass()) instanceof LocalDateTimeAsLocalDateTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(LocalTime.now().getClass()) instanceof LocalTimeTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(LocalDateTime.now().getClass()) instanceof LocalDateTimeTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(ZonedDateTime.now().getClass()) instanceof OffsetDateTimeAsZonedDateTimeTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(OffsetDateTime.now().getClass()) instanceof OffsetDateTimeTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(OffsetTime.now().getClass()) instanceof OffsetTimeTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler("abc".getClass()) instanceof StringTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(BigInteger.valueOf(1L).getClass()) instanceof BigIntegerTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(BigDecimal.valueOf(1L).getClass()) instanceof BigDecimalTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new StringReader("abc").getClass()) instanceof StringAsReaderTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new ByteArrayInputStream(new byte[0]).getClass()) instanceof BytesAsInputStreamTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(ArrayUtils.toObject(new byte[] { 1, 2, 3 }).getClass()) instanceof BytesAsBytesWrapTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new byte[] { 1, 2, 3 }.getClass()) instanceof BytesTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new Object[] { new Object(), new ArrayUtils() }.getClass()) instanceof ArrayTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new ArgRuleTest().getClass()) instanceof UnknownTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new LongAccumulator((left, right) -> 0, 1L).getClass()) instanceof NumberTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Mockito.mock(Clob.class).getClass()) instanceof ClobAsStringTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Mockito.mock(NClob.class).getClass()) instanceof NClobAsStringTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Mockito.mock(Blob.class).getClass()) instanceof BlobAsBytesTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new URL("http://www.hasor.net").getClass()) instanceof StringAsUrlTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(new URI("http://www.hasor.net").getClass()) instanceof StringAsUriTypeHandler;
    }
}
