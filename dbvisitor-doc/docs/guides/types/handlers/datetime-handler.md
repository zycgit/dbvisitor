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

| 类型处理器                     | Java 类型                  | 作用                                           |
|---------------------------|--------------------------|----------------------------------------------|
| LocalDateTimeTypeHandler  | java.time.LocalDateTime  | 以 getObject/setObject 方式处理 LocalDateTime 数据  |
| LocalTimeTypeHandler      | java.time.LocalTime      | 以 getObject/setObject 方式处理 LocalTime 数据      |
| OffsetDateTimeTypeHandler | java.time.OffsetDateTime | 以 getObject/setObject 方式处理 OffsetDateTime 数据 |
| OffsetTimeTypeHandler     | java.time.OffsetTime     | 以 getObject/setObject 方式处理 OffsetTime 数据     |
| SqlDateTypeHandler        | java.sql.Date            | 以 getObject/setObject 方式处理 Date 数据           |
| SqlTimestampTypeHandler   | java.sql.Timestamp       | 以 getObject/setObject 方式处理 Timestamp 数据      |
| SqlTimeTypeHandler        | java.sql.Time            | 以 getObject/setObject 方式处理 Time 数据           |

## 使用 java.util.Date 类型

| 类型处理器                         | Java 类型        | 作用                                                 |
|-------------------------------|----------------|----------------------------------------------------|
| SqlDateAsDateHandler          | java.util.Date | 以 getDate/setDate 方式处理 java.util.Date 数据           |
| SqlTimeAsDateTypeHandler      | java.util.Date | 以 getTime/setTime 方式处理 java.util.Date 数据（日期部分会被丢弃） |
| SqlTimestampAsDateTypeHandler | java.util.Date | 以 getTimestamp/setTimestamp 方式处理 java.util.Date 数据 |

## 使用 java.time.Year 类型

| 类型处理器                         | Java 类型        | 作用                                                          |
|-------------------------------|----------------|-------------------------------------------------------------|
| IntegerAsYearTypeHandler      | java.time.Year | 读写一个数字表示年份                                                  |
| StringAsYearTypeHandler       | java.time.Year | 读写一个字符串形式的数字表示年份                                            |
| SqlTimestampAsYearTypeHandler | java.time.Year | 以 getTimestamp/setTimestamp 方式处理 Year 数据（月份/日期/时间 在读写时会被丢弃） |

## 使用 java.time.YearMonth 类型

| 类型处理器                              | Java 类型             | 作用                                                            |
|------------------------------------|---------------------|---------------------------------------------------------------|
| IntegerAsYearMonthTypeHandler      | java.time.YearMonth | 读写一个数字末尾 2 位表示月份，其余表示年份。不足 2 位数的按照月份处理。如果为 0 表示 0000-01       |
| StringAsYearMonthTypeHandler       | java.time.YearMonth | 格式为 “yyyy-MM” 的字符串                                            |
| SqlTimestampAsYearMonthTypeHandler | java.time.YearMonth | 以 getTimestamp/setTimestamp 方式处理 YearMonth 数据（日期/时间 在读写时会被丢弃） |

## 使用 java.time.Month 类型

| 类型处理器                          | Java 类型         | 作用                                                           |
|--------------------------------|-----------------|--------------------------------------------------------------|
| IntegerAsMonthTypeHandler      | java.time.Month | 读写一个 1 ~ 12 之间的数字表示一个月份                                      |
| StringAsMonthTypeHandler       | java.time.Month | 字符串形式的数字表示月份，值范围为 1 ~ 12                                     |
| SqlTimestampAsMonthTypeHandler | java.time.Month | 以 getTimestamp/setTimestamp 方式处理 Month 数据（年份/日期/时间 在读写时会被丢弃） |

## 使用 java.time.MonthDay 类型

| 类型处理器                             | Java 类型            | 作用                                                           |
|-----------------------------------|--------------------|--------------------------------------------------------------|
| IntegerAsMonthDayTypeHandler      | java.time.MonthDay | 读写 4 个长度的数字，其中前 2 位数字表示月份，后 2 位数字表示日期                        |
| StringAsMonthDayTypeHandler       | java.time.MonthDay | 格式为 “01-03” 的字符串                                             |
| SqlTimestampAsMonthDayTypeHandler | java.time.MonthDay | 以 getTimestamp/setTimestamp 方式处理 MonthDay 数据（年份/时间 在读写时会被丢弃） |

## 类型转换

| 类型处理器                                    | Java 类型                       | 作用                                                                                                            |
|------------------------------------------|-------------------------------|---------------------------------------------------------------------------------------------------------------|
| SqlDateAsDateHandler                     | java.util.Date                | 以 getDate/setDate 方式处理 java.util.Date 数据                                                                      |
| SqlTimeAsDateTypeHandler                 | java.util.Date                | 以 getTime/setTime 方式处理 java.util.Date 数据（日期部分会被丢弃）                                                            |
| SqlTimestampAsDateTypeHandler            | java.util.Date                | 以 getTimestamp/setTimestamp 方式处理 java.util.Date 数据                                                            |
| SqlTimestampAsLocalDateTimeTypeHandler   | java.time.LocalDateTime       | 以 getTimestamp/setTimestamp 方式处理 LocalDateTime 类型读写                                                           |
| SqlTimestampAsLocalDateTypeHandler       | java.time.LocalDate           | 以 getTimestamp/setTimestamp 方式处理 LocalDate 类型读写，缺失的时间信息使用 00:00 补充                                            |
| SqlTimestampAsLocalTimeTypeHandler       | java.time.LocalTime           | 以 getTimestamp/setTimestamp 方式处理 LocalTime 类型读写，缺失的时间信息使用 0000-01-01 补充                                       |
| LocalDateTimeAsLocalTimeTypeHandler      | java.time.LocalTime           | 以 getObject/setObject 方式处理 LocalDateTime 数据，Handler 会自动处理 LocalDateTime 到 LocalTime 的转换，缺失的日期使用 0000-01-01 补充 |
| JapaneseDateAsSqlDateTypeHandler         | java.time.chrono.JapaneseDate | 以 getDate/setDate 方式处理 JapaneseDate 数据                                                                        |
| LocalDateTimeAsLocalDateTypeHandler      | java.time.LocalDate           | 以 getObject/setObject 方式处理 LocalDateTime 数据，并自动处理 LocalDateTime 和 LocalDate 之间的类型转换，缺失的时间信息使用 00:00 补充。       |
| OffsetDateTimeAsZonedDateTimeTypeHandler | java.time.ZonedDateTime       | 以 getObject/setObject 方式处理 OffsetDateTime 数据，并自动处理 OffsetDateTime 和 ZonedDateTime 之间的类型转换                     |
| SqlTimestampAsInstantTypeHandler         | java.time.Instant             | 以 getTimestamp/setTimestamp 方式处理 Instant 数据                                                                   |

### 自动时区转换

自动时区转换，时区的写入和读取会转换为 UTC。

| 类型处理器                                      | Java 类型                  | 作用                                           |
|--------------------------------------------|--------------------------|----------------------------------------------|
| SqlTimestampAsUTCOffsetDateTimeTypeHandler | java.time.OffsetDateTime | 自动时区转换，使用时区 OffsetDateTime 类型读写 Timestamp 数据 |
| SqlTimestampAsUTCOffsetTimeTypeHandler     | java.time.OffsetTime     | 自动时区转换，使用时区 OffsetTime 类型读写 Timestamp 数据     |
