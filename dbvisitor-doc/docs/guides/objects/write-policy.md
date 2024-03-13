---
id: write_policy
sidebar_position: 8
title: 写入策略
description: 使用 dbVisitor ORM 工具操作数据库时使用不通的写入策略。
---

# 写入策略

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

## 列不允许更新

如若 `test_user` 表的 `create_time` 列不允许被更新，那么需要配置 `@Column` 注解 `update = false` 表示该列不参与更新。

```java {9}
@Table(table = "test_user", autoMapping = false)
public class TestUser {
    @Column(name = "id", primary = true)
    private Integer id;
    @Column("name")
    private String  name;
    @Column("age")
    private Integer age;
    @Column(name = "create_time", update = false) // 列不参与更新
    private Date    createTime;

    // getters and setters omitted
}
```

## Insert 忽略该列

对于某些列新增时想使用数据库的默认值忽略来自应用的设置则可采用 `insert` 属性

```java {9}
@Table(table = "test_user", autoMapping = false)
public class TestUser {
    @Column(name = "id", primary = true)
    private Integer id;
    @Column("name")
    private String  name;
    @Column("age")
    private Integer age;
    @Column(name = "create_time", insert = false) // 列不参与新增
    private Date    createTime;

    // getters and setters omitted
}
```