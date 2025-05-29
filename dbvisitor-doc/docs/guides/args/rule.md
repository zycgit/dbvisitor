---
id: rule
sidebar_position: 5
hide_table_of_contents: true
title: 6.4 规则传参
description: 语句中通过 @{...} 写法，可以借助规则机制，优雅的处理一些常见动态 SQL 场景。
---

# 规则传参

语句中通过 `@{...}` 写法，可以借助 [规则](../rules/about) 机制，优雅的处理一些常见动态 SQL 场景。

```sql title='例如当 name 不为空时，才作为参数添加到 SQL 语句中'
select * from users where id > :id @{and, name = :name}
```

## AND 规则

```sql title='AND 规则'
select * from users where id > :id @{and, name = :name}
```

- 当名称参数 `name` 不为空时，会在 where 语句的条件中追加 `and name = ?` 作为查询条件。
- 最终生成的语句：
  - `name` 属性为空：`select * from users where id > ?`
  - `name` 属性不为空：`select * from users where id > ? and name = ?`

## OR 规则

```sql title='OR 规则'
select * from users where id > :id @{or, name = :name}
```

- 当名称参数 `name` 不为空时，会在 where 语句的条件中追加 `or name = ?` 作为查询条件。
- 最终生成的语句：
  - `name` 属性为空：`select * from users where id > ?`
  - `name` 属性不为空：`select * from users where id > ? or name = ?`

## SET 规则

```sql title='SET 规则'
update users set modify_time = ? @{set, status = :status} where id > :id
```

- 当名称参数 `status` 不为空时，会在 set 语句中追加 `status = ?` 作为更新项。
- 最终生成的语句：
  - `status` 属性为空：`update users set modify_time = ? where id > ?`
  - `status` 属性不为空：`update users set modify_time = ?, status = ? where id > ?`

## IN 规则

```sql title='IN 规则'
select * from users where status = true @{in,and id in :ids}
```

- 例如：参数 `ids` 有 3 个元素时候。IN 规则会在 where 语句后面追加类似 `and id in (?, ?, ?)` 条件项。条件项中的参数数量由元素数量决定。
- 最终生成的语句：
  - `ids` 属性为空：`select * from users where status = true`
  - `ids` 属性不为空：`select * from users where status = true and id in (?, ?, ?)`

:::info[关于规则]
- dbVisitor 内置了丰富的规则可以完成不同需要。
- 更多详细资料请 [点击这里](../rules/about) 查看。
:::
