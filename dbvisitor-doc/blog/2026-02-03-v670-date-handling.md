---
slug: v670-date-handling
title: v6.7.0 解读：公元前日期处理的两种方案
authors: [ZhaoYongChun]
tags: [dbVisitor, TypeHandler]
---

公元前日期在数据库中的处理是一个被长期忽视的难题。Java 的 ISO 8601 年份表示法与数据库的日期系统存在根本性差异，导致跨系统传递公元前日期时经常出现"偏移一年"或"偏移一天"的诡异 Bug。

dbVisitor 6.7.0 新增了 `JulianDayTypeHandler` 和 `PgDateTypeHandler` 两个处理器，分别从"跨库通用"和"PostgreSQL 原生"两个角度提供日期映射方式；数据库日期范围和处理器边界仍需考虑。

<!--truncate-->

## 问题根源：年份表示法的歧义

Java 的 `LocalDate` 使用 ISO 8601 标准，`Year 0` 表示公元前 1 年：

| Java Year | 含义 | PostgreSQL 表示 |
|-----------|------|-----------|
| 1 | 公元 1 年 (1 AD) | `0001-01-01` |
| 0 | 公元前 1 年 (1 BC) | `0001-01-01 BC` |
| -1 | 公元前 2 年 (2 BC) | `0002-01-01 BC` |
| -99 | 公元前 100 年 (100 BC) | `0100-01-01 BC` |

转换公式：**BC 年份 = |Java Year| + 1**

`LocalDate` 使用延伸格里高利历，而传统 `java.sql.Date` 相关转换涉及旧式历法处理，不能认为两者在所有历史日期上等价。不同 JDBC 驱动对公元前日期的处理也各不相同，有些甚至直接抛异常。

## 方案一：JulianDayTypeHandler — 跨数据库通用方案

儒略日数（Julian Day Number）是天文学中使用的连续日期计数系统，以固定纪元连续计数。使用它仍需约定历法和日界，本处理器将 ISO LocalDate 映射为整数日数。

**原理**：将 `LocalDate` 转换为一个 `BIGINT` 整数存入数据库，读取时逆向还原。

```java
// 存储：公元前 100 年 → 儒略日数 1684901
LocalDate bcDate = LocalDate.of(-99, 1, 1);

Map<String, Object> params = new HashMap<>();
params.put("id", 1);
params.put("date", bcDate);

jdbcTemplate.executeUpdate(
    "INSERT INTO events (id, julian_day) VALUES (#{id}, #{date, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})",
    params
);

// 读取：儒略日数 1684901 → 公元前 100 年
LocalDate loaded = jdbcTemplate.queryForObject(
    "SELECT julian_day FROM events WHERE id = ?",
    new Object[] { 1 },
    (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day")
);

// 预期 loaded.equals(bcDate)，其中 Year -99 表示 100 BC。
```

**算法核心**（Richards 2012）：

```java
// LocalDate → Julian Day Number
int a = (14 - month) / 12;
int y2 = year + 4800 - a;
int m2 = month + 12 * a - 3;
long jdn = day + (153 * m2 + 2) / 5 + 365 * y2 + y2 / 4 - y2 / 100 + y2 / 400 - 32045;
```

**适用场景**：
- 需要跨数据库（MySQL、PostgreSQL、Oracle、SQLite 等）保持一致性
- 历史学、天文学数据
- 数据库需支持 `BIGINT`，不依赖原生 DATE
- 当前转换使用整数运算，不应据此承诺覆盖 `LocalDate` 的全部年份范围。应用应限制业务日期范围并验证往返转换。

## 方案二：PgDateTypeHandler — PostgreSQL 原生方案

如果你的项目锁定 PostgreSQL，可以利用其原生的 BC 后缀格式，直接使用 `DATE` 类型存储。

```java
LocalDate bcDate = LocalDate.of(-99, 1, 1);

Map<String, Object> params = new HashMap<>();
params.put("id", 1);
params.put("date", bcDate);

jdbcTemplate.executeUpdate(
    "INSERT INTO events (id, event_date) VALUES (#{id}, #{date, typeHandler=net.hasor.dbvisitor.types.handler.time.PgDateTypeHandler})",
    params
);

// 数据库中存储为: 0100-01-01 BC
// 读取时自动转换回 LocalDate.of(-99, 1, 1)
```

**优势**：
- 使用数据库原生 `DATE` 类型，支持 SQL 中直接查询和比较（如 `WHERE event_date < '0500-01-01 BC'`）
- 无需额外的类型转换层

**注意事项**：
- 不要将该处理器视为全部历史日期的无损映射保证，尤其是 BC 闰日及原生 DATE 范围边界。需要覆盖这些日期时，应先验证驱动和处理器的往返转换。
- 仅适用于 PostgreSQL

## 方案对比

| 维度 | JulianDayTypeHandler | PgDateTypeHandler |
|------|---------------------|------------------|
| **数据库支持** | 所有（存为 BIGINT） | 仅 PostgreSQL |
| **存储类型** | `BIGINT` | `DATE` |
| **SQL 中日期比较** | 数值比较（可行但不直观） | 原生日期比较 |
| **精度** | 天级（无时间） | 天级（无时间） |
| **日期边界** | 需验证整数转换范围 | 需验证 BC 闰日和原生 DATE 范围 |
| **迁移成本** | 低（通用整数列） | 中（依赖 PG） |

**选择建议**：跨库项目或对一致性要求高的场景用 `JulianDayTypeHandler`；PostgreSQL 专属项目且需要在 SQL 中操作日期的场景用 `PgDateTypeHandler`。
