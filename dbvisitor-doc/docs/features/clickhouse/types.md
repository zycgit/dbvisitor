---
id: types
sidebar_position: 80
title: 类型支持
description: ClickHouse 类型支持
---

# 类型支持

下表给出常用字段的 Java 属性类型建议；可空字段使用包装类型。Java/JDBC 类型见 [Java/JDBC 类型关系](../../guides/types/java-jdbc.md)；枚举见[枚举映射](../../guides/types/enum-handler.md)。

## 类型映射

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| Nullable(Int16) | Short | 覆盖此有符号整数类型的范围。 |
| Nullable(Int32) | Integer | 覆盖此有符号整数类型的范围。 |
| Nullable(Int64) | Long | 覆盖此有符号整数类型的范围。 |
| Nullable(Float32) | Float | 近似数值；不用于要求十进制精确计算的金额。 |
| Nullable(Float64) | Double | 近似数值；不用于要求十进制精确计算的金额。 |
| Nullable(Decimal(10, 2)) | BigDecimal | 保留十进制数值；写入受列精度与小数位限制。 |
| Nullable(Decimal(20, 0)) | BigInteger / BigDecimal | 无小数的整数业务可用 BigInteger；统一十进制模型可用 BigDecimal。 |
| Nullable(Bool) | Boolean | 使用布尔语义，不作为普通整数属性。 |
| Nullable(String) | String | 文本内容；没有 VARCHAR(n) 式的长度约束。 |
| Nullable(Date) | java.sql.Date | 仅日期；不用于保留完整时间戳。 |
| Nullable(String) | java.sql.Time | 仅时间；字段内容须使用可解析的时间格式。 |
| Nullable(DateTime64(3)) | java.sql.Timestamp | 保留日期和时间，精度以数据库列定义为准。 |
| Nullable(String) | Map / List / Bean | 保存 JSON 文本，实体属性配置见[JSON 字段映射](../../guides/core/mapping/json-field.md)。 |

## 文本长度 {#text-length}

`String` 不按列声明限制文本长度。即使写成 `VARCHAR(100)`，ClickHouse 也会忽略其中的 `100`，不会仅因写入 101 个字符而拒绝操作。[String 类型说明](https://clickhouse.com/docs/reference/data-types/string)

dbVisitor 不额外校验字符串长度。若业务要求名称最多 100 个字符，应在写入前校验，不能依赖构造器 API 或 BaseMapper 插入时抛出超长异常。

## 使用限制

- `DateTime64(3)` 的时区和精度由字段定义及 ClickHouse JDBC 驱动决定。
- `String` 文本映射不适合保存任意二进制内容。

## 数组类型 {#array-values}

支持非空数组读写，但不支持整个数组为 NULL；没有元素时传空数组，见[空数组与 NULL](#array-null)。

| 数据库字段类型 | Java 类型 | 说明 |
| --- | --- | --- |
| Array(Int32) | Integer[] | 有序整数集合。 |
| Array(Float32) | Float[] | 有序浮点数集合。 |
| Array(String) | String[] | 有序字符串集合。 |

数组支持参数绑定、读取、整体修改和多条写入。例如：

```java
@Table("array_values")
public class ArrayValues {
    @Column(value = "id", primary = true)
    private Integer id;
    @Column(value = "values", jdbcType = Types.ARRAY)
    private Integer[] values;
    // 省略 getter 和 setter
}
```

对应字段声明为 `id Int32`、`values Array(Int32)`，读写方式见[数组映射](../../guides/types/array-handler.md)。

### 空数组与 NULL {#array-null}

没有元素时使用空数组。整个 Array 值不能为 NULL，不能声明 `Nullable(Array(Int32))`。`Array(Nullable(Int32))` 允许元素为空，但不代表整个数组可空。

写入包含 null 的元素时，还要显式指定 JDBC 数组的元素类型。使用普通 `INTEGER` 绑定时，驱动会把 null 转成 0；`Nullable(Int32)` 才能保留它：

```java
ArrayTypeHandler handler = new ArrayTypeHandler() {
    @Override
    protected String resolveTypeName(Class<?> type) {
        return type == Integer.class ? "Nullable(Int32)" : super.resolveTypeName(type);
    }
};
Integer[] values = { null, -1, 0, 7, null };
jdbc.executeUpdate("INSERT INTO array_values (id, values) VALUES (?, ?)",
        new Object[] { 1, new SqlArg(values, Types.ARRAY, handler) });
```

上例的 `values` 列应声明为 `Array(Nullable(Int32))`。字符串数组同理使用 `Nullable(String)`。

## 向量类型 {#vector-types}

向量字段映射与读写见[向量操作](vectors.md#vector-mapping)。
