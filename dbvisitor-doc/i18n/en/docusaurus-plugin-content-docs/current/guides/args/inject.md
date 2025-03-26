---
id: inject
sidebar_position: 4
title: 6.3 SQL 注入
hide_table_of_contents: true
description: 语句中使用 ${...} 写法，可以对已名称化的参数进行取值，并将结果注入到 SQL 语句中。
---

# SQL 注入

:::warning[请注意]
无论何时 SQL 注入都属于 **危险** 操作，开发者需要自己保证注入是安全的。
:::

语句中使用 `${...}` 写法，可以对已名称化的参数进行取值，并将结果注入到 SQL 语句中。

```text title='例如：通过 SQL 注入实现参数化排序'
select * from users where id > #{id} order by ${order}
```

## 基本用法

```java
Map<String, Object> args = CollectionUtils.asMap(
        "id", 2,
        "order", "name desc"
);
jdbcTemplate.queryForList("select * from users where id > #{id} order by ${order}", args);
```