---
id: keywords
sidebar_position: 6
title: 表/列名称含有关键字
description: 使用 dbVisitor ORM 工具处理表/列名称含有关键字的情况。
---

# 表/列名称含有关键字

比如有如下这样一张表，包含了一个叫 index 的列。

```sql
create table `param_index` (
    `id`    int(11),
    `name`  varchar(255),
    `index` int,
    primary key (`id`)
)
```

此时只需要设置 `@Table` 注解的 `useDelimited = true` 属性，让其在生成 SQL 的时候对每一个名称都使用限定符号包裹起来。即可正常处理，

:::tip
dbVisitor 已经可以自动识别并处理 达梦、MySql、Oracle、PostgreSql 四个数据库的关键字，因此无需 `useDelimited` 也可以处理名称关键字问题。

具体支持的关键字需要到 jar 包中 `META-INF/db-keywords/*.keywords` 相关文件中查看，dbVisitor 当匹配到关键字会自动为它加上 useDelimited。
:::