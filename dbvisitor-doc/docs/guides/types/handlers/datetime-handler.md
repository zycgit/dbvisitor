---
id: datetime-handler
sidebar_position: 5
hide_table_of_contents: true
title: 时间类型
description: dbVisitor 处理时间类型的类型处理器。
---

# 时间类型处理器

时间类型处理器位于 `net.hasor.dbvisitor.types.handler.time` 包中。

## 常见处理器

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `LocalDateTimeTypeHandler` | `java.time.LocalDateTime` | 以 getObject/setObject 方式处理 LocalDateTime 数据 |
| `LocalTimeTypeHandler` | `java.time.LocalTime` | 以 getObject/setObject 方式处理 LocalTime 数据 |
| `OffsetDateTimeTypeHandler` | `java.time.OffsetDateTime` | 以 getObject/setObject 方式处理 OffsetDateTime 数据 |
| `OffsetTimeTypeHandler` | `java.time.OffsetTime` | 以 getObject/setObject 方式处理 OffsetTime 数据 |
| `SqlDateTypeHandler` | `java.sql.Date` | 以 getObject/setObject 方式处理 Date 数据 |
| `SqlTimestampTypeHandler` | `java.sql.Timestamp` | 以 getObject/setObject 方式处理 Timestamp 数据 |
| `SqlTimeTypeHandler` | `java.sql.Time` | 以 getObject/setObject 方式处理 Time 数据 |

## 使用 java.util.Date 类型

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `SqlDateAsDateHandler` | `java.util.Date` | 以 getDate/setDate 方式处理 java.util.Date 数据 |
| `SqlTimeAsDateTypeHandler` | `java.util.Date` | 以 getTime/setTime 方式处理 java.util.Date 数据（日期部分会被丢弃） |
| `SqlTimestampAsDateTypeHandler` | `java.util.Date` | 以 getTimestamp/setTimestamp 方式处理 java.util.Date 数据 |

## 使用 java.time.Year 类型

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `IntegerAsYearTypeHandler` | `java.time.Year` | 读写一个数字表示年份 |
| `StringAsYearTypeHandler` | `java.time.Year` | 读写一个字符串形式的数字表示年份 |
| `SqlTimestampAsYearTypeHandler` | `java.time.Year` | 以 getTimestamp/setTimestamp 方式处理 Year 数据（月份/日期/时间在读写时会被丢弃） |

## 使用 java.time.YearMonth 类型

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `IntegerAsYearMonthTypeHandler` | `java.time.YearMonth` | 读写一个数字末尾 2 位表示月份，其余表示年份。不足 2 位数的按照月份处理。如果为 0 表示 0000-01 |
| `StringAsYearMonthTypeHandler` | `java.time.YearMonth` | 格式为 `yyyy-MM` 的字符串 |
| `SqlTimestampAsYearMonthTypeHandler` | `java.time.YearMonth` | 以 getTimestamp/setTimestamp 方式处理 YearMonth 数据（日期/时间在读写时会被丢弃） |

## 使用 java.time.Month 类型

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `IntegerAsMonthTypeHandler` | `java.time.Month` | 读写一个 1 ~ 12 之间的数字表示月份 |
| `StringAsMonthTypeHandler` | `java.time.Month` | 字符串形式的数字表示月份，值范围为 1 ~ 12 |
| `SqlTimestampAsMonthTypeHandler` | `java.time.Month` | 以 getTimestamp/setTimestamp 方式处理 Month 数据（年份/日期/时间在读写时会被丢弃） |

## 使用 java.time.MonthDay 类型

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `IntegerAsMonthDayTypeHandler` | `java.time.MonthDay` | 读写 4 个长度的数字，其中前 2 位数字表示月份，后 2 位数字表示日期 |
| `StringAsMonthDayTypeHandler` | `java.time.MonthDay` | 格式为 `MM-dd` 的字符串（如 `01-03`） |
| `SqlTimestampAsMonthDayTypeHandler` | `java.time.MonthDay` | 以 getTimestamp/setTimestamp 方式处理 MonthDay 数据（年份/时间在读写时会被丢弃） |

## 类型转换

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `SqlTimestampAsLocalDateTimeTypeHandler` | `java.time.LocalDateTime` | 以 getTimestamp/setTimestamp 方式处理 LocalDateTime 类型读写 |
| `SqlTimestampAsLocalDateTypeHandler` | `java.time.LocalDate` | 以 getTimestamp/setTimestamp 方式处理 LocalDate 类型读写，缺失的时间信息使用 00:00 补充 |
| `SqlTimestampAsLocalTimeTypeHandler` | `java.time.LocalTime` | 以 getTimestamp/setTimestamp 方式处理 LocalTime 类型读写，缺失的日期使用 0000-01-01 补充 |
| `LocalDateTimeAsLocalTimeTypeHandler` | `java.time.LocalTime` | 以 getObject/setObject 方式处理 LocalDateTime 数据，自动转换为 LocalTime，缺失的日期使用 0000-01-01 补充 |
| `LocalDateTimeAsLocalDateTypeHandler` | `java.time.LocalDate` | 以 getObject/setObject 方式处理 LocalDateTime 数据，自动转换为 LocalDate，缺失的时间信息使用 00:00 补充 |
| `JapaneseDateAsSqlDateTypeHandler` | `java.time.chrono.JapaneseDate` | 以 getDate/setDate 方式处理 JapaneseDate 数据 |
| `OffsetDateTimeAsZonedDateTimeTypeHandler` | `java.time.ZonedDateTime` | 以 getObject/setObject 方式处理 OffsetDateTime 数据，自动转换为 ZonedDateTime |
| `SqlTimestampAsInstantTypeHandler` | `java.time.Instant` | 以 getTimestamp/setTimestamp 方式处理 Instant 数据 |

### 自动时区转换

自动时区转换，时区的写入和读取会转换为 UTC。

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `SqlTimestampAsUTCOffsetDateTimeTypeHandler` | `java.time.OffsetDateTime` | 自动时区转换，使用 OffsetDateTime 类型读写 Timestamp 数据 |
| `SqlTimestampAsUTCOffsetTimeTypeHandler` | `java.time.OffsetTime` | 自动时区转换，使用 OffsetTime 类型读写 Timestamp 数据 |

## 特殊支持

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `JulianDayTypeHandler` | `java.time.LocalDate` | 使用儒略日数（Julian Day Number）存储日期，支持公元前日期。数据库存储为 BIGINT 类型 |
| `PgDateTypeHandler` | `java.time.LocalDate` | PostgreSQL DATE 类型专用，支持公元前日期（BC 后缀格式）。标注了 `@NoCache` |

### JulianDayTypeHandler

儒略日数是天文学中常用的日期表示方法，从公元前 4713 年 1 月 1 日开始连续计数。在数据库中以 `BIGINT` 类型存储，避免了不同历法系统间的转换问题。

适用场景：需要处理公元前日期，或跨数据库系统保证日期一致性。

### PgDateTypeHandler

PostgreSQL 支持使用 BC 后缀表示公元前日期（如 `0100-01-01 BC` 表示公元前 100 年）。Java `LocalDate` 使用 ISO 8601 年份表示法：

| Java Year | 含义 |
|---|---|
| Year 1 | 公元 1 年（1 AD） |
| Year 0 | 公元前 1 年（1 BC） |
| Year -1 | 公元前 2 年（2 BC） |
| Year -99 | 公元前 100 年（100 BC） |
