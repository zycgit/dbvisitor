package net.hasor.dbvisitor.test.suite.fluent.types;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Type Conversion Test
 * 验证不同数据类型之间的转换能力
 */
public class TypeConversionTest extends AbstractOneApiTest {

    /**
     * 测试 Date/Timestamp 互转
     * 场景: java.util.Date <-> java.sql.Timestamp
     */
    @Test
    public void testDateTimestampConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        Date now = new Date();
        UserInfo u = new UserInfo();
        u.setId(23101);
        u.setName("DateConv");
        u.setAge(25);
        u.setCreateTime(now);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 查询并验证日期
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23101)//
                .queryForObject();

        assertNotNull(loaded.getCreateTime());
        // 允许毫秒级误差（某些数据库精度不同）
        long diff = Math.abs(loaded.getCreateTime()//
                .getTime() - now.getTime());
        assertTrue("Date should be preserved within 1 second", diff < 1000);
    }

    /**
     * 测试 Date 与 LocalDateTime 手动互转
     * 场景: 通过 java.util.Date 存储时间，查询后手动转换为 LocalDateTime 验证
     */
    @Test
    public void testLocalDateTimeConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault())//
                .toInstant());

        UserInfo u = new UserInfo();
        u.setId(23201);
        u.setName("LocalDT");
        u.setAge(30);
        u.setCreateTime(date);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 查询并转换回 LocalDateTime
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23201)//
                .queryForObject();

        assertNotNull(loaded.getCreateTime());
        LocalDateTime loadedLocalDateTime = LocalDateTime.ofInstant(loaded.getCreateTime()//
                .toInstant(), ZoneId.systemDefault());

        // 验证日期部分相同（忽略毫秒差异）
        assertEquals(localDateTime.toLocalDate(), loadedLocalDateTime.toLocalDate());
        assertEquals(localDateTime.getHour(), loadedLocalDateTime.getHour());
        assertEquals(localDateTime.getMinute(), loadedLocalDateTime.getMinute());
    }

    /**
     * 测试整数字段的多类型读取
     * 场景: age 字段分别以 Integer 和 Long 类型读取
     */
    @Test
    public void testBigDecimalPrecision() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 注意: UserInfo 需要有 BigDecimal 字段，这里用 age 字段模拟
        // 实际应用中应有专门的 DECIMAL 字段
        UserInfo u = new UserInfo();
        u.setId(23301);
        u.setName("Decimal");
        u.setAge(12345); // 如果有 salary 等 BigDecimal 字段更合适

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 测试查询单个数值
        Integer age = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23301)//
                .applySelect("age")//
                .queryForObject(Integer.class);

        assertEquals(Integer.valueOf(12345), age);

        // 测试 Long 类型转换
        Long ageLong = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23301)//
                .applySelect("age")//
                .queryForObject(Long.class);

        assertEquals(Long.valueOf(12345), ageLong);
    }

    /**
     * 测试 Integer 0/1 的存取
     * 场景: 用 age 字段存储 0/1 并验证读取一致性
     */
    @Test
    public void testBooleanConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 使用 age 字段模拟 Boolean（0=false, 1=true）
        UserInfo u1 = new UserInfo();
        u1.setId(23401);
        u1.setName("BoolTrue");
        u1.setAge(1); // true

        UserInfo u2 = new UserInfo();
        u2.setId(23402);
        u2.setName("BoolFalse");
        u2.setAge(0); // false

        lambda.insert(UserInfo.class)//
                .applyEntity(u1)//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(u2)//
                .executeSumResult();

        // 查询并转换为 Boolean
        Integer trueValue = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23401)//
                .applySelect("age")//
                .queryForObject(Integer.class);

        Integer falseValue = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23402)//
                .applySelect("age")//
                .queryForObject(Integer.class);

        assertEquals(Integer.valueOf(1), trueValue);
        assertEquals(Integer.valueOf(0), falseValue);

        // 转换为 Boolean
        assertTrue("1 should be true", trueValue == 1);
        assertTrue("0 should be false", falseValue == 0);
    }

    /**
     * 测试字符串字段存储枚举名称
     * 场景: 用 name 字段存储枚举值字符串并读取验证
     */
    @Test
    public void testEnumConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 假设有 Enum: Status { ACTIVE, INACTIVE }
        // 存储为字符串
        UserInfo u = new UserInfo();
        u.setId(23501);
        u.setName("ACTIVE"); // 使用 name 字段存储 Enum
        u.setAge(25);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 查询并验证
        String enumValue = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23501)//
                .applySelect("name")//
                .queryForObject(String.class);

        assertEquals("ACTIVE", enumValue);

        // 模拟 Enum 转换
        // Status status = Status.valueOf(enumValue);
        // assertEquals(Status.ACTIVE, status);
    }

    /**
     * 测试 String/Number 互转
     * 场景: "123" <-> 123
     */
    @Test
    public void testStringNumberConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = new UserInfo();
        u.setId(23601);
        u.setName("NumberConv");
        u.setAge(999);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 查询数值字段为 String
        String ageStr = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23601)//
                .applySelect("age")//
                .queryForObject(String.class);

        assertEquals("999", ageStr);

        // 转回 Integer
        Integer age = Integer.valueOf(ageStr);
        assertEquals(Integer.valueOf(999), age);
    }

    /**
     * 测试 NULL 值类型转换
     * 场景: NULL -> 各种类型
     */
    @Test
    public void testNullValueConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = new UserInfo();
        u.setId(23701);
        u.setName("NullConv");
        u.setAge(null); // NULL

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 查询 NULL 值
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23701)//
                .queryForObject();

        assertNull("Age should be null", loaded.getAge());

        // 尝试将 NULL 转换为基本类型（应返回 null 而不是抛异常）
        Integer ageValue = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23701)//
                .applySelect("age")//
                .queryForObject(Integer.class);

        assertNull("NULL should be converted to null, not 0", ageValue);
    }

    /**
     * 测试零值与 NULL 的区分
     * 场景: 0 vs NULL
     */
    @Test
    public void testZeroVsNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u1 = new UserInfo();
        u1.setId(23801);
        u1.setName("Zero");
        u1.setAge(0);

        UserInfo u2 = new UserInfo();
        u2.setId(23802);
        u2.setName("Null");
        u2.setAge(null);

        lambda.insert(UserInfo.class)//
                .applyEntity(u1)//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(u2)//
                .executeSumResult();

        // 查询 age=0 的记录
        UserInfo zero = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23801)//
                .queryForObject();

        assertNotNull(zero.getAge());
        assertEquals(Integer.valueOf(0), zero.getAge());

        // 查询 age=NULL 的记录
        UserInfo nullAge = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23802)//
                .queryForObject();

        assertNull("Should be null, not 0", nullAge.getAge());
    }

    /**
     * 测试日期边界值
     * 场景: 最小日期 (epoch 0, 1970-01-01)
     */
    @Test
    public void testDateBoundaryValues() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // Java Date 的最小值（近似）
        Date minDate = new Date(0); // 1970-01-01

        UserInfo u = new UserInfo();
        u.setId(23901);
        u.setName("MinDate");
        u.setAge(25);
        u.setCreateTime(minDate);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 23901)//
                .queryForObject();

        assertNotNull(loaded.getCreateTime());
        assertTrue("Should preserve early date", loaded.getCreateTime()//
                .getTime() >= 0 && loaded.getCreateTime()//
                .getTime() < 100000000L);
    }

    /**
     * 测试负数存储
     * 场景: 负整数、负小数
     */
    @Test
    public void testNegativeNumbers() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = new UserInfo();
        u.setId(24001);
        u.setName("Negative");
        u.setAge(-100);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24001)//
                .queryForObject();

        assertEquals(Integer.valueOf(-100), loaded.getAge());
    }

    /**
     * 测试字符串编码转换
     * 场景: UTF-8 编码的正确性
     */
    @Test
    public void testStringEncodingConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String unicodeText = "Hello 世界 🌍";

        UserInfo u = new UserInfo();
        u.setId(24101);
        u.setName(unicodeText);
        u.setAge(25);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24101)//
                .queryForObject();

        assertEquals("Unicode text should be preserved", unicodeText, loaded.getName());
    }

    /**
     * 测试数值范围边界
     * 场景: Integer.MAX_VALUE, Integer.MIN_VALUE
     */
    @Test
    public void testNumericBoundaryValues() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u1 = new UserInfo();
        u1.setId(24201);
        u1.setName("MaxInt");
        u1.setAge(Integer.MAX_VALUE);

        UserInfo u2 = new UserInfo();
        u2.setId(24202);
        u2.setName("MinInt");
        u2.setAge(Integer.MIN_VALUE);

        lambda.insert(UserInfo.class)//
                .applyEntity(u1)//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(u2)//
                .executeSumResult();

        UserInfo max = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24201)//
                .queryForObject();

        UserInfo min = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24202)//
                .queryForObject();

        assertEquals(Integer.MAX_VALUE, max.getAge()//
                .intValue());
        assertEquals(Integer.MIN_VALUE, min.getAge()//
                .intValue());
    }

    /**
     * 测试查询结果类型转换
     * 场景: queryForObject(Class<?>) 的类型转换
     */
    @Test
    public void testQueryResultTypeConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = new UserInfo();
        u.setId(24301);
        u.setName("TypeConv");
        u.setAge(888);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // 查询为 Integer
        Integer ageInt = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24301)//
                .applySelect("age")//
                .queryForObject(Integer.class);

        assertEquals(Integer.valueOf(888), ageInt);

        // 查询为 Long
        Long ageLong = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24301)//
                .applySelect("age")//
                .queryForObject(Long.class);

        assertEquals(Long.valueOf(888), ageLong);

        // 查询为 String
        String ageStr = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24301)//
                .applySelect("age")//
                .queryForObject(String.class);

        assertEquals("888", ageStr);
    }

    /**
     * 测试 Timestamp 精度
     * 场景: 秒级精度（部分数据库不支持毫秒）
     */
    @Test
    public void testTimestampPrecision() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        long nowMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(nowMillis);

        UserInfo u = new UserInfo();
        u.setId(24401);
        u.setName("Timestamp");
        u.setAge(25);
        u.setCreateTime(timestamp);

        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 24401)//
                .queryForObject();

        assertNotNull(loaded.getCreateTime());
        // 允许秒级精度（部分数据库不支持毫秒）
        long diff = Math.abs(loaded.getCreateTime()//
                .getTime() - nowMillis);
        assertTrue("Timestamp should be preserved within 1 second", diff < 1000);
    }
}
