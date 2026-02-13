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

```java title='例2：List 类型的集合传参数'
List<?> args = Arrays.asList(2, "Dave");
jdbcTemplate.queryForList("select * from users where id > ? and name = ?", args);
```

```java title='例3：当只有一个参数时可省略数组'
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

通过 `SqlArg` 类可以在参数设置时提供更多控制，如：指定 TypeHandler、Java 类型或 JDBC 类型。

```java title='例1：使用 SqlArg 指定 TypeHandler'
SqlArg[] args = new SqlArg[] {
      SqlArg.valueOf(2L, new LongTypeHandler()),     // LongTypeHandler，设置 Long 类型参数
      SqlArg.valueOf("Dave", new StringTypeHandler())// StringTypeHandler，设置 String 类型参数
};
jdbcTemplate.queryForList("select * from users where id > ? and name = ?", args);
```

`SqlArg` 提供了多个工厂方法用于创建不同类型的参数：

| 方法 | 说明 |
|------|------|
| `SqlArg.valueOf(Object)` | 创建 IN 参数 |
| `SqlArg.valueOf(Object, Class<?>)` | 创建带 javaType 的 IN 参数 |
| `SqlArg.valueOf(Object, TypeHandler<?>)` | 创建带 TypeHandler 的 IN 参数 |
| `SqlArg.valueOf(Object, int)` | 创建带 jdbcType 的 IN 参数 |
| `SqlArg.asOut(String, int)` | 创建 OUT 参数（指定 jdbcType） |
| `SqlArg.asOut(String, int, TypeHandler<?>)` | 创建 OUT 参数（指定 jdbcType + TypeHandler） |
| `SqlArg.asInOut(String, Object, int)` | 创建 INOUT 参数（指定 jdbcType） |
| `SqlArg.asInOut(String, Object, int, TypeHandler<?>)` | 创建 INOUT 参数（指定 jdbcType + TypeHandler） |

`SqlArg` 对象也可以直接放入 Map 中作为命名参数的值，框架会自动识别并使用其中的类型信息。

```java title='例2：Map 中混合使用 SqlArg 和普通值'
Map<String, Object> args = new HashMap<>();
args.put("id", SqlArg.valueOf(2L, new LongTypeHandler()));
args.put("name", "Dave");
jdbcTemplate.queryForList("select * from users where id > :id and name = :name", args);
```

:::info[有关更多参数选项的信息请到：]
- [参数选项](./options) 查看。
:::
