---
id: camel_case
sidebar_position: 2
title: 驼峰转下划线
description: 使用 dbVisitor ORM 映射数据库表，处理名称驼峰转换。
---

# 驼峰转下划线

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

上述 pojo 的命名不太符合常规 Java 驼峰的命名规范，可以通过 `mapUnderscoreToCamelCase` 配置将驼峰命名转换为下划线命名。

```java {1}
@Table(mapUnderscoreToCamelCase = true)
public class TestUser {
    private Integer id;
    private String  name;
    private Integer age;
    private Date    createTime;

    // getters and setters omitted
}
```

- 类名 `TestUser` 会被转换为 `test_user` 表名
- 属性 `createTime` 会被转换为 `create_time` 列名