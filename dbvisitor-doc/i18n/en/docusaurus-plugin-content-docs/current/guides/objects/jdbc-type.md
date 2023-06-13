---
sidebar_position: 9
title: JDBC Type
description: 使用 dbVisitor ORM 映射字段类型和 jdbcType。
---

# JDBC Type

每一个数据库类型都会有一个与其对应的 JdbcType、每个 Java 类型要想正确写入也要匹配正确的 JdbcType。
通过 `@Column` 注解的 `jdbcType` 属性可以设置这种映射关系。通常 dbVisitor 都会自动处理好它们，使用过程中无需干预。

若想了解 dbVisitor 对于 JavaType 和 JdbcType 映射关系，请看 **[类型](../types/type-handlers.md)** 相关章节。

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

将 ID 列的 `INT` 类型映射改为 `TINYINT`

```java {3}
@Table(mapUnderscoreToCamelCase = true)
public class TestUser {
    @Column(jdbcType = java.sql.Types.TINYINT) // 
    private Integer id;

    // getters and setters omitted
}
```
