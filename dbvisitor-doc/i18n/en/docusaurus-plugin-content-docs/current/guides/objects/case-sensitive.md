---
id: case_sensitive
sidebar_position: 5
title: 表/列名称大小写敏感
description: 使用 dbVisitor ORM 映射数据库表，处理大小写敏感性问题。
---

# 表/列名称大小写敏感

```sql title='有如下表'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

处理大小写敏感需要设置 @Table 注解的 `useDelimited = true`，让其在生成 SQL 的时候对每一个名称都使用限定符包裹起来；
然后设置 `autoMapping = false` 关闭属性的自动发现，改为通过 @Column 明确配置。
最后设置 `caseInsensitive = false` 将结果集列名大小写不敏感设置为敏感，默认是：true不敏感

:::tip
和大小写相关的属性有两个分别是 `useDelimited`、`caseInsensitive`

- 属性 `useDelimited`，决定在生成 SQL 语句时是否用限定符。
  例如表名：`test_user` 和 `test_user` 后者使用了限定符。
- 属性 `caseInsensitive`，决定在接收和处理查询结果集时候，是否对结果集上的列名保持大小写敏感性。
:::

```java {1,5,7}
@Table(table = "test_user", useDelimited = true,
       autoMapping = false, caseInsensitive = false)
public class TestUser {
    @Column(name = "id"， primary = true)
    private Integer id;
    @Column("age")
    private Integer age1;
    @Column("AGE")
    private String  age2;

    // getters and setters omitted
}
```
