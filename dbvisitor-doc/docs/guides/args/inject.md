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

## 常见场景

`${...}` 适用于 SQL 结构本身需要动态变化的场景，例如：

```text title='动态表名'
select * from ${tableName} where id = #{id}
```

```text title='动态列名'
select ${columns} from users where id = #{id}
```

```text title='动态排序'
select * from users order by ${orderBy}
```

## 与 `#{...}` 的区别

| 写法 | 行为 | 安全性 |
|------|------|--------|
| `#{...}` | 生成 `?` 占位符，通过 PreparedStatement 绑定参数值 | **安全**，防止 SQL 注入 |
| `${...}` | 通过 OGNL 求值后将结果直接拼入 SQL 字符串 | **不安全**，存在 SQL 注入风险 |

:::tip[原则]
优先使用 `#{...}`，只在 SQL 结构（表名、列名、排序等）需要动态变化时才使用 `${...}`。
:::