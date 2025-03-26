---
id: position
sidebar_position: 2
hide_table_of_contents: true
title: 6.1 位置参数
description: 当在 SQL 语句使用 “?” 标记时，可以将值绑定到相应索引（从 0 开始）的参数。
---

# 位置参数

当在 SQL 语句使用 “?” 标记时，可以将值绑定到相应索引（从 0 开始）的参数。

```sql
select * from users where id > ? and status = ?
```

## 基本用法

```java title='例1：使用数组传递参数'
Object[] args = new Object[] { 2, "Dave"};
jdbcTemplate.queryForList("select * from users where id > ? and name = ?", args);
```

```java title='例2：当只有一个参数时可省略数组'
jdbcTemplate.queryForList("select * from users where id > ?", 2);
```

## 名称化 {#pos_named}

每一个 “?” 位置参数都隐含了一个对应的名称。名称规则为 “arg + &lt;位置编号&gt;”，例如：arg0、arg1。

```java title='例1：使用名称化位置参数'
Object[] args = new Object[] { 2, "Dave"};
jdbcTemplate.queryForList("select * from users where id > :arg0 and name = :arg1", args);
```
- 有关名称化参数的其它写法可以参考 [名称参数](./named)。

## 参数选项

通过参数选项可以在在参数设置时提供更多控制，如：通过 TypeHandler 实现特殊类型的读写。

```java title='例3：使用 SqlArg 对参数进行封装，并指定 TypeHandler'
SqlArg[] args = new SqlArg[] {
      SqlArg.valueOf(2L, new LongTypeHandler()),     // LongTypeHandler，设置 Long 类型参数
      SqlArg.valueOf("Dave", new StringTypeHandler())// StringTypeHandler，设置 String 类型参数
};
jdbcTemplate.queryForList("select * from users where id > ? and name = ?", args);
```

:::info[有关更多参数选项的信息请到：]
- [参数选项](./options) 查看。
:::
