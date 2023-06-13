---
sidebar_position: 9
title: SqlParameterSource
description: 使用 dbVisitor ORM 工具向参数化 SQL 传递参数。
---

# SqlParameterSource

`SqlParameterSource` 是通过接口形式给动态 SQL 传参数，功效和 `数组/Map` 传参类似，不同的是编程性更强。

dbVisitor 内置了两个实现，分别为：

- `MapSqlParameterSource` 将一个 Map 转换为 `SqlParameterSource` 接口
- `BeanSqlParameterSource` 将一个 Bean 转换为 `SqlParameterSource` 接口

下面用相同的功效列举不同的查询写法

## 使用数组传参

```java
String querySql = "select * from test_user where age > ?";
Object[] queryArg = new Object[] { 40 };
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, queryArg);
```

## 使用Map传参

```java
String querySql = "select * from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, queryArg);
```

## 使用 MapSqlParameterSource 传参

使用 `MapSqlParameterSource` 传参，返回值为 `List/Map`

```java
String querySql = "select * from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
SqlParameterSource source = new MapSqlParameterSource(queryArg);
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, source);
```

如果返回值希望是 `List/DTO` 可以使用下面这个方法

```java
List<TestUser> result = jdbcTemplate.queryForList(querySql, source, TestUser.class);
```

## 使用 BeanSqlParameterSource 传参

使用 `BeanSqlParameterSource` 传参，返回值为 `List/Map`

```java
String querySql = "select * from test_user where age > :age";
TestUser argDTO = new TestUser();
argDTO.setAge(40);

BeanSqlParameterSource source = new BeanSqlParameterSource(argDTO);
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, source);
```

如果返回值希望是 `List/DTO` 可以使用下面这个方法

```java
List<TestUser> result = jdbcTemplate.queryForList(querySql, source, TestUser.class);
```
