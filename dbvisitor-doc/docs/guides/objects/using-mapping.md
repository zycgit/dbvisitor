---
id: map_column
sidebar_position: 3
title: 使用 @Column 映射列
description: 使用 dbVisitor ORM 工具的 @Column 注解进行字段映射。
---

# 使用 @Column 映射列

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

在明确映射关系中可以精确的指定表名和列名。将 `autoMapping` 设置为关闭然后为每表和每一个列配置映射的名字。

```java {3,5,7,9,11}
// table       = "test_user" 表名
// autoMapping = false       关闭属性字段自动映射(默认true)，全部通过 @Column 配置
@Table(table = "test_user", autoMapping = false) 
public class TestUser {
    @Column(name = "id"， primary = true)
    private Integer id;
    @Column("name")
    private String  name;
    @Column("age")
    private Integer age;
    @Column("create_time")
    private Date    createTime;

    // getters and setters omitted
}
```