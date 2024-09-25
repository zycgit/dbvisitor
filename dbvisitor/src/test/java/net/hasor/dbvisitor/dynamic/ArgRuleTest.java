package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.rule.ArgRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.UnknownTypeHandler;
import net.hasor.dbvisitor.types.handler.*;
import net.hasor.test.dto.LicenseOfValueEnum;
import net.hasor.test.dto.ResourceType;
import net.hasor.test.dto.UserFutures2;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;

public class ArgRuleTest {

    private void assertAutomaticJavaType(String sql, Map<String, Object> ctx, Class<?> testJavaType) throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql(sql);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getJavaType() == testJavaType;
    }

    private void assertAutomaticSqlMode(String sql, Map<String, Object> ctx, SqlMode testSqlMode) throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql(sql);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getSqlMode() == testSqlMode;
    }

    private void assertAutomaticJdbcType(String sql, Map<String, Object> ctx, int testJdbcType) throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql(sql);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getJdbcType() == testJdbcType;
    }

    private void assertAutomaticHandlerType(String sql, Map<String, Object> ctx, Class<?> testHandlerType) throws SQLException {
        DefaultSqlSegment segment = DynamicParsed.getParsedSql(sql);

        SqlBuilder sqlBuilder = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder.getSqlString().equals("?");
        assert sqlBuilder.getArgs().length == 1;
        assert testHandlerType.isInstance(((SqlArg) sqlBuilder.getArgs()[0]).getTypeHandler());
    }

    @Test
    public void ruleTest_1() throws Exception {
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", Boolean.TRUE), Boolean.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", false), Boolean.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Byte((byte) 123)), Byte.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", (byte) 123), Byte.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Short((short) 123)), Short.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", (short) 123), Short.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Integer(123)), Integer.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", 123), Integer.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Long(123)), Long.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", 123L), Long.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Float(123.123f)), Float.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", 123.123f), Float.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Double(123.123f)), Double.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", 123.123d), Double.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Character('a')), Character.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", 'a'), Character.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new java.util.Date()), java.util.Date.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new java.sql.Date(11)), java.sql.Date.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new java.sql.Timestamp(11)), java.sql.Timestamp.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new java.sql.Time(11)), java.sql.Time.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", Instant.now()), Instant.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", JapaneseDate.now()), JapaneseDate.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", Year.of(2022)), Year.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", Month.MAY), Month.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", YearMonth.now()), YearMonth.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", MonthDay.now()), MonthDay.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", LocalDate.now()), LocalDate.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", LocalTime.now()), LocalTime.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", LocalDateTime.now()), LocalDateTime.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", ZonedDateTime.now()), ZonedDateTime.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", OffsetDateTime.now()), OffsetDateTime.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", OffsetTime.now()), OffsetTime.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", "abc"), String.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", BigInteger.valueOf(1L)), BigInteger.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", BigDecimal.valueOf(1L)), BigDecimal.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new StringReader("abc")), StringReader.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new ByteArrayInputStream(new byte[0])), ByteArrayInputStream.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", ArrayUtils.toObject(new byte[] { 1, 2, 3 })), Byte[].class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new byte[] { 1, 2, 3 }), byte[].class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new Object[] { new Object(), new ArgRuleTest(), new ArrayUtils() }), Object[].class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new ArgRuleTest()), ArgRuleTest.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new LongAccumulator((left, right) -> 0, 1L)), LongAccumulator.class);
        Clob mockClob = Mockito.mock(Clob.class);
        NClob mockNclob = Mockito.mock(NClob.class);
        Blob mockBlob = Mockito.mock(Blob.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", mockClob), mockClob.getClass());
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", mockNclob), mockNclob.getClass());
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", mockBlob), mockBlob.getClass());
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new URL("http://www.hasor.net")), URL.class);
        assertAutomaticJavaType("#{name}", CollectionUtils.asMap("name", new URI("http://www.hasor.net")), URI.class);
    }

    @Test
    public void ruleTest_2() throws Exception {
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Boolean.TRUE), BooleanTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", false), BooleanTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Byte((byte) 123)), ByteTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", (byte) 123), ByteTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Short((short) 123)), ShortTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", (short) 123), ShortTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Integer(123)), IntegerTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", 123), IntegerTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Long(123)), LongTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", 123L), LongTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Float(123.123f)), FloatTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", 123.123f), FloatTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Double(123.123f)), DoubleTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", 123.123d), DoubleTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Character('a')), StringAsCharTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", 'a'), StringAsCharTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new java.util.Date()), SqlTimestampAsDateTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new java.sql.Date(11)), SqlDateTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new java.sql.Timestamp(11)), SqlTimestampTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new java.sql.Time(11)), SqlTimeTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Instant.now()), SqlTimestampAsInstantTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", JapaneseDate.now()), JapaneseDateAsSqlDateTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Year.of(2022)), SqlTimestampAsYearTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Month.MAY), SqlTimestampAsMonthTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", YearMonth.now()), SqlTimestampAsYearMonthTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", MonthDay.now()), SqlTimestampAsMonthDayTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", LocalDate.now()), LocalDateTimeAsLocalDateTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", LocalTime.now()), LocalTimeTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", LocalDateTime.now()), LocalDateTimeTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", ZonedDateTime.now()), OffsetDateTimeAsZonedDateTimeTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", OffsetDateTime.now()), OffsetDateTimeTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", OffsetTime.now()), OffsetTimeTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", "abc"), StringTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", BigInteger.valueOf(1L)), BigIntegerTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", BigDecimal.valueOf(1L)), BigDecimalTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new StringReader("abc")), StringAsReaderTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new ByteArrayInputStream(new byte[0])), BytesAsInputStreamTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", ArrayUtils.toObject(new byte[] { 1, 2, 3 })), BytesAsBytesWrapTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new byte[] { 1, 2, 3 }), BytesTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new Object[] { new Object(), new ArgRuleTest(), new ArrayUtils() }), ArrayTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new ArgRuleTest()), UnknownTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new LongAccumulator((left, right) -> 0, 1L)), NumberTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Mockito.mock(Clob.class)), ClobAsStringTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Mockito.mock(NClob.class)), NClobAsStringTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", Mockito.mock(Blob.class)), BlobAsBytesTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new URL("http://www.hasor.net")), URLTypeHandler.class);
        assertAutomaticHandlerType("#{name}", CollectionUtils.asMap("name", new URI("http://www.hasor.net")), URITypeHandler.class);
    }

    @Test
    public void ruleTest_3() throws SQLException {
        Map<String, Object> ctx1 = CollectionUtils.asMap("name", "abc");
        DefaultSqlSegment segment1 = DynamicParsed.getParsedSql("#{name}");
        SqlBuilder sqlBuilder1 = segment1.buildQuery(ctx1, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getSqlMode() == SqlMode.In;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJdbcType() == Types.VARCHAR;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJavaType() == String.class;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getTypeHandler() instanceof StringTypeHandler;

        //
        Map<String, Object> ctx2 = CollectionUtils.asMap("name", "abc");
        DefaultSqlSegment segment2 = DynamicParsed.getParsedSql("#{name,mode=out,jdbcType=123,javaType=java.lang.Integer,typeHandler=net.hasor.dbvisitor.types.handler.ShortTypeHandler}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getSqlMode() == SqlMode.Out;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJdbcType() == 123;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJavaType() == Integer.class;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() instanceof ShortTypeHandler;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() == TypeHandlerRegistry.DEFAULT.getHandlerByHandlerType(ShortTypeHandler.class);
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() == TypeHandlerRegistry.DEFAULT.getTypeHandler(Short.class);
    }

    @Test
    public void ruleTest_4() throws SQLException {
        Map<String, Object> ctx2 = CollectionUtils.asMap("name", ResourceType.WORKER);
        DefaultSqlSegment segment2 = DynamicParsed.getParsedSql("#{name,javaType=net.hasor.test.dto.ResourceType}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getSqlMode() == SqlMode.In;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getValue() == ResourceType.WORKER;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJdbcType() == Types.JAVA_OBJECT;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJavaType() == ResourceType.class;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() instanceof EnumTypeHandler;
        EnumTypeHandler handler0 = (EnumTypeHandler) ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler();
        assert handler0.getEnumType() == ResourceType.class;

        assert TypeHandlerRegistry.DEFAULT.getHandlerByHandlerType(EnumTypeHandler.class) == null;
        TypeHandler<?> handler1 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        assert handler0 == handler1;
    }

    @Test
    public void ruleTest_5() {
        TypeHandler<?> handler1 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        TypeHandler<?> handler2 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        assert handler1 == handler2;

        TypeHandler<?> handler3 = TypeHandlerRegistry.DEFAULT.getTypeHandler(ResourceType.class);
        TypeHandler<?> handler4 = TypeHandlerRegistry.DEFAULT.getTypeHandler(LicenseOfValueEnum.class);
        assert handler3 != handler4;
    }

    @Test
    public void ruleTest_6() throws SQLException {
        Map<String, Object> ctx2 = CollectionUtils.asMap("name", new UserFutures2());
        DefaultSqlSegment segment2 = DynamicParsed.getParsedSql("#{name}");
        SqlBuilder sqlBuilder2 = segment2.buildQuery(ctx2, new DynamicContext());
        assert sqlBuilder2.getSqlString().equals("?");
        assert sqlBuilder2.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getJavaType() == UserFutures2.class;
        assert ((SqlArg) sqlBuilder2.getArgs()[0]).getTypeHandler() instanceof JsonUseForFastjson2TypeHandler;
    }

    @Test
    public void ruleTest_7() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc");
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("#{name,mode=out,jdbcType=123,javaType=java.lang.Integer,typeHandler=net.hasor.dbvisitor.types.handler.SqlXmlTypeHandler}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getSqlMode() == SqlMode.Out;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJdbcType() == 123;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJavaType() == Integer.class;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getTypeHandler() instanceof SqlXmlTypeHandler;
    }

    @Test
    public void ruleTest_8() throws SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("name", "abc");
        DefaultSqlSegment segment = DynamicParsed.getParsedSql("#{name,,,,}");

        SqlBuilder sqlBuilder1 = segment.buildQuery(ctx, new DynamicContext());
        assert sqlBuilder1.getSqlString().equals("?");
        assert sqlBuilder1.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getSqlMode() == SqlMode.In;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJavaType() == String.class;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getJdbcType() == Types.VARCHAR;
        assert ((SqlArg) sqlBuilder1.getArgs()[0]).getTypeHandler() instanceof StringTypeHandler;
    }

    @Test
    public void toStringTest_1() {
        assert ArgRule.INSTANCE.toString().startsWith("arg [");
    }
}
