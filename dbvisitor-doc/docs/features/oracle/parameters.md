---
id: parameters
sidebar_position: 70
title: 参数与规则
---

## 空字符串参数 {#empty-strings}

位置参数、名称参数和 JDBC PreparedStatement 都能绑定字符串，但 Oracle 将 `VARCHAR2` 的空字符串按 NULL 处理。参数绑定方式不会改变这一行为；写入 `""` 后不能读回相同的空字符串。

```sql
CREATE TABLE text_example (id NUMBER(10) PRIMARY KEY, note VARCHAR2(100));
```

```java
jdbcTemplate.executeUpdate("INSERT INTO text_example (id, note) VALUES (?, ?)",
        new Object[] { 1, "" });
String note = jdbcTemplate.queryForObject(
        "SELECT note FROM text_example WHERE id = ?", new Object[] { 1 }, String.class);
// note 为 null
```

方法注解或 Mapper 文件使用 `#{note}` 传入 `""` 时也是如此。需要查询这些记录时使用 `IS NULL`；如果业务必须区分“未填写”和“空字符串”，使用额外的状态字段。

## 参数复用 {#parameter-reuse}

PreparedStatement 支持重新绑定参数后执行，但复用不会改变空字符串的存储含义。应用需要保留 `""` 与 null 的区别时，应在写入前采用上述状态字段方案。


空字符串查询和大量 ID 分批查询的用法见[条件构造器](builder.md#predicates)。
