---
id: macro_rule
sidebar_position: 4
hide_table_of_contents: true
title: 7.4 SQL 注入规则
description: 该类规则会改变发往数据库的最终 SQL 语句。比如：排序方式的参数化或者分库分表中表名称的计算。
---

# SQL 注入规则

该类规则会改变发往数据库的最终 SQL 语句。比如：排序方式的参数化或者分库分表中表名称的计算。

| 规则                                                    | 描述                                                                 |
|-------------------------------------------------------|--------------------------------------------------------------------|
| [`@{macro, name}`](./macro_rule#macro)                | MACRO 规则，会执行名称为 `name` 的 SQL 宏，在 SQL 宏中可以含有预先定义好的 SQL。             |
| [`@{ifmacro, testExpr, name}`](./macro_rule#macro)    | IFMACRO 规则是 MACRO 的增强版，当 `testExpr` 条件表达式为真时才寻找并执行对应的 SQL 宏。       |
| [`@{iftext, testExpr, content}`](./macro_rule#iftext) | IFTEXT 规则，当 `testExpr` 表达式结果为真时，`content` 代表的内容将会原封不动的加入到 SQL 语句中。 |

## MACRO、IFMACRO 规则 {#macro}

MACRO 规则，会将预先定义的 SQL 宏内容包含进最终要执行的 SQL 中。如果引用了一个不存在的 SQL 宏在执行时将会报错。

```java title='1. SQL 宏注册'
DynamicContext registry = jdbcTemplate.getRegistry();
registry.addMacro("includeSeq", "and seq = :seq");
```

```sql title='2. 查询语句中包含 SQL 宏'
select * from users where
    status = :status
    @{macro, includeSeq}
```

```sql title='3. 最终被执行的语句是'
select * from users where status = :status and seq = :seq
```

IFMACRO 规则是 MACRO 的增强版，只有当 `testExpr` 条件表达式为真时才寻找并执行对应的 SQL 宏。

```sql title='使用 IFMACRO 规则'
select * from users where
    status = :status
    @{ifmacro, status > 2, includeSeq}
```

- 当 status 参数属性值大于 2 时才会引用 SQL 宏。

## IFTEXT 规则 {#iftext}

IFTEXT 规则的作用是当 `testExpr` 表达式结果为真时，`content` 代表的内容将会原封不动的加入到 SQL 语句中。

```sql title='1. 用法'
select * from users where
    status = :status
    @{iftext, status > 2, and age = 36 }
```

```sql title='2. 当 status > 2 时，被执行的语句是'
select * from users where status = :status and age = 36
```

```sql title='3. 当 status <= 2 时，被执行的语句是'
select * from users where status = :status
```

:::info
需要注意 IFTEXT 规则的 `content` 中所包含的内容将会以文本的形式直接放入最终执行的 SQL 语句中，如果 content 中含有参数需要考虑使用其它规则。
:::
