---
id: types
sidebar_position: 1
title: 类型支持
description: 达梦数据库的数据类型与 Java 属性类型建议
---

# 类型支持

下表给出达梦常用字段的 Java 属性类型建议；可空字段使用包装类型。不会根据 `NUMBER` 精度自动选择 Java 类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)。

## 常用类型

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| BIT | Boolean | `0` 为假，非零值为真。 |
| TINYINT | Byte | 有符号整数，范围为 -128 至 127。 |
| BYTE | Byte | 精度为 3、标度为 0 的整数类型。 |
| SMALLINT | Short | 有符号整数，范围为 -32768 至 32767。 |
| INTEGER | Integer | 有符号 32 位整数。 |
| INT | Integer | `INTEGER` 的同义类型。 |
| BIGINT | Long | 有符号 64 位整数。 |
| NUMERIC(p, s) | BigDecimal | 精度最高为 38；写入受字段精度和标度限制。 |
| DECIMAL(p, s) | BigDecimal | 与 `NUMERIC` 语义相近。 |
| DEC(p, s) | BigDecimal | `DECIMAL` 的同义类型。 |
| NUMBER(p, s) | BigDecimal | 与 `NUMERIC` 语义相同。 |
| REAL | Float | 单精度近似数值。 |
| FLOAT(p) | Float / Double | `p` 不大于 24 时按单精度存储；大于 24 时按双精度存储。Java 属性应按字段定义选择。 |
| DOUBLE | Double | 双精度近似数值。 |
| DOUBLE PRECISION | Double | 双精度近似数值。 |
| CHAR(n) | String | 定长字符串；不足长度时由数据库补空格。 |
| CHARACTER(n) | String | `CHAR` 的同义类型。 |
| VARCHAR(n) | String | 变长字符串。 |
| VARCHAR2(n) | String | 与 `VARCHAR` 用法相同。 |
| TEXT | String | 大文本。保存 JSON 文本时，实体配置见 [JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| LONG | String | `TEXT` 的同义类型。 |
| LONGVARCHAR | String | `TEXT` 的同义类型。 |
| CLOB | String | 大文本。保存 JSON 文本时，实体配置见 [JSON 字段映射](../../guides/core/mapping/json-field.md)。 |
| BINARY(n) | byte[] | 定长二进制内容。 |
| VARBINARY(n) | byte[] | 变长二进制内容。 |
| RAW(n) | byte[] | `VARBINARY` 的同义类型。 |
| IMAGE | byte[] | 二进制大对象，也可保存非图像二进制内容。 |
| LONGVARBINARY | byte[] | `IMAGE` 的同义类型。 |
| BLOB | byte[] | 二进制大对象。 |
| DATE | java.sql.Date | 只包含年月日。 |
| TIME | java.sql.Time | 只包含时分秒；未声明精度时小数秒精度为 0。 |
| TIME(p) | String | `p` 为 1 至 6 且必须保留小数秒时，使用字符串或达梦 JDBC 扩展对象；`java.sql.Time` 不能完整表达这部分精度。 |
| TIMESTAMP(p) | java.sql.Timestamp | 包含日期和时间；达梦的小数秒精度最高为 9。`DATETIME` 是同义写法。 |
| ROWID | String | 18 位字符值；用于保存达梦行标识。 |

`NUMERIC`、`DECIMAL`、`DEC` 和 `NUMBER` 的无小数字段也可以映射为整数类型，但必须由业务保证数值没有小数部分且不超出目标 Java 类型范围。需要覆盖完整字段范围时，优先使用 `BigDecimal`。

## 时间间隔类型

达梦 JDBC 为时间间隔提供了扩展对象；官方驱动同时允许通过 `setString` 和 `getString` 读写。若实体只需保留完整字面值，可使用 `String`：

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| INTERVAL YEAR | String | 年间隔；字符串必须符合达梦间隔字面值格式。 |
| INTERVAL MONTH | String | 月间隔；字符串必须符合达梦间隔字面值格式。 |
| INTERVAL YEAR TO MONTH | String | 年月至少两部分的间隔。 |
| INTERVAL DAY | String | 日间隔。 |
| INTERVAL DAY TO HOUR | String | 日到小时间隔。 |
| INTERVAL DAY TO MINUTE | String | 日到分钟间隔。 |
| INTERVAL DAY TO SECOND | String | 日到秒间隔，可包含小数秒。 |
| INTERVAL HOUR | String | 小时间隔。 |
| INTERVAL HOUR TO MINUTE | String | 小时到分钟间隔。 |
| INTERVAL HOUR TO SECOND | String | 小时到秒间隔，可包含小数秒。 |
| INTERVAL MINUTE | String | 分钟间隔。 |
| INTERVAL MINUTE TO SECOND | String | 分钟到秒间隔，可包含小数秒。 |
| INTERVAL SECOND | String | 秒间隔，可包含小数秒。 |

需要按年、月、日、时、分、秒分别访问时，应在自定义映射中使用达梦 JDBC 扩展对象；dbVisitor 不会把这些专有对象自动转换成 `java.time.Duration` 或 `Period`。

## 示例：声明实体属性

```java
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import net.hasor.dbvisitor.mapping.Column;

@Column("quantity")
private Short quantity;       // SMALLINT

@Column("amount")
private BigDecimal amount;    // NUMBER(10, 2)

@Column("business_date")
private Date businessDate;    // DATE

@Column("created_at")
private Timestamp createdAt;  // TIMESTAMP(6)

@Column("payload")
private byte[] payload;       // BLOB
```

## 使用边界

- `BFILE` 是数据库服务器上的只读文件，不是 `BLOB`。
- 时区时间类型和 `XMLTYPE` 应使用自定义映射或 `RowMapper`。
- 精确小数使用 `NUMBER` 与 `BigDecimal`，不要使用浮点类型。
- `TIMESTAMP` 使用 `Timestamp`；`TIME(p)` 需要保留小数秒时使用字符串或达梦 JDBC 扩展对象。
