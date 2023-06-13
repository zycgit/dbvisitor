---
sidebar_position: 2
title: 执行SQL
description: 使用 dbVisitor ORM 工具执行原生 SQL。
---

# 执行SQL

作为例子要先准备一张表，并初始化一些数据

```sql
create table `test_user` (
`id`          int(11) auto_increment,
`name`        varchar(255),
`age`         int,
`create_time` datetime,
primary key (`id`)
);

insert into `test_user` values (1, 'mali', 26, now());
insert into `test_user` values (2, 'dative', 32, now());
insert into `test_user` values (3, 'jon wes', 41, now());
insert into `test_user` values (4, 'mary', 66, now());
insert into `test_user` values (5, 'matt', 25, now());
```

## 查询并返回 List/Map

查询 `age > 40` 的数据，并返回 `List/Map` 形式

```java
String querySql = "select * from test_user where age > 40";
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql);
```

```java title='数组传参'
String querySql = "select * from test_user where age > ?";
Object[] queryArg = new Object[] { 40 };
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, queryArg);
```

```java title='Map传参'
String querySql = "select * from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, queryArg);
```

```text title='执行结果（列名为标题）'
/--------------------------------------------\
| id | name    | age | create_time           |
|--------------------------------------------|
| 3  | jon wes | 41  | 2021-11-26 12:10:21.0 |
| 4  | mary    | 66  | 2021-11-26 12:10:21.0 |
\--------------------------------------------/
```

## 查询并返回 List/DTO

查询 `age > 40` 的数据，并返回 `TestUser` 结果集

```java
String querySql = "select * from test_user where age > 40";
List<TestUser> result = jdbcTemplate.queryForList(querySql, TestUser.class);
```

```java title='数组传参'
String querySql = "select * from test_user where age > ?";
Object[] queryArg = new Object[] { 40 };
List<TestUser> result = jdbcTemplate.queryForList(querySql, queryArg, TestUser.class);
```

```java title='Map传参'
String querySql = "select * from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
List<TestUser> result = jdbcTemplate.queryForList(querySql, queryArg, TestUser.class);
```

```text title='执行结果（对象的属性名为标题）'
/---------------------------------------------------\
| createTime                   | name    | id | age |
|---------------------------------------------------|
| Fri Nov 26 12:12:03 CST 2021 | jon wes | 3  | 41  |
| Fri Nov 26 12:12:03 CST 2021 | mary    | 4  | 66  |
\---------------------------------------------------/
```

## 查询并返回一条记录 Map结果

查询 `age > 40` 的数据，并返回 `1` 个结果，结果为 `Map` 形式。

:::caution
需要提示的是，执行的 SQL 语句一定要确保只会返回 `1` 个结果，否则会面临 dbVisitor 无法确认选择哪一个结果的问题。在这个情况下会引发异常。
:::

```java
String querySql = "select * from test_user where age > 40 order by age limit 1";
Map<String, Object> result = jdbcTemplate.queryForMap(querySql);
```

```java title='数组传参'
String querySql = "select * from test_user where age > ? order by age limit 1";
Object[] queryArg = new Object[] { 40 };
Map<String, Object> result = jdbcTemplate.queryForMap(querySql, queryArg);
```

```java title='Map传参'
String querySql = "select * from test_user where age > :age order by age limit 1";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
Map<String, Object> result = jdbcTemplate.queryForMap(querySql, queryArg);
```

```text title='执行结果（Map的 Key 是列名）'
/--------------------------------------------\
| id | name    | age | create_time           |
|--------------------------------------------|
| 3  | jon wes | 41  | 2021-11-26 12:20:12.0 |
\--------------------------------------------/
```

## 查询并返回一条记录 DTO结果

查询 `age > 40` 的数据，并返回 `1` 个结果，结果为 `TestUser` 对象

:::caution
需要提示的是，执行的 SQL 语句一定要确保只会返回 `1` 个结果，否则会面临 dbVisitor 无法确认选择哪一个结果的问题。在这个情况下会引发异常。
:::

```java
String querySql = "select * from test_user where age > 40 order by age limit 1";
TestUser result = jdbcTemplate.queryForMap(querySql, TestUser.class);
```

```java title='数组传参'
String querySql = "select * from test_user where age > ? order by age limit 1";
Object[] queryArg = new Object[] { 40 };
TestUser result = jdbcTemplate.queryForMap(querySql, queryArg, TestUser.class);
```

```java title='Map传参'
String querySql = "select * from test_user where age > :age order by age limit 1";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
TestUser result = jdbcTemplate.queryForMap(querySql, queryArg, TestUser.class);
```

```text title='执行结果（对象的属性名为标题）'
/---------------------------------------------------\
| createTime                   | name    | id | age |
|---------------------------------------------------|
| Fri Nov 26 12:12:03 CST 2021 | jon wes | 3  | 41  |
\---------------------------------------------------/
```

## 查询并返回汇总数据

查询 `age > 40` 的数据总数

```java
String querySql = "select count(*) from test_user where age > 40";
int result = jdbcTemplate.queryForInt(querySql);
```

```java title='数组传参'
String querySql = "select count(*) from test_user where age > ?";
Object[] queryArg = new Object[] { 40 };
int result = jdbcTemplate.queryForInt(querySql, queryArg);
```

```java title='Map传参'
String querySql = "select count(*) from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
int result = jdbcTemplate.queryForInt(querySql, queryArg);
```

```text title='执行结果'
2
```

:::tip
- 如果 int 的数据类型无法承载结果值，可以选择 `queryForLong` 系列方法
- 若 long 依然不够，可以选用 `queryForObject("sql", BigInteger.class)` 方式。
:::


## 查询并返回一列值 List/String

查询 `age > 40` 的数据，并返回 `List/String` 形式

```java
String querySql = "select name from test_user where age > 40";
List<String> result = jdbcTemplate.queryForList(querySql, String.class);
```

```java title='数组传参'
String querySql = "select name from test_user where age > ?";
Object[] queryArg = new Object[] { 40 };
List<String> result = jdbcTemplate.queryForList(querySql, queryArg, String.class);
```

```java title='Map传参'
String querySql = "select name from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
List<String> result = jdbcTemplate.queryForList(querySql, queryArg, String.class);
```

```text title='执行结果，是列名为标题'
/---------\
| jon wes |
| mary    |
\---------/
```

## 查询并返回一列值

查询 `age > 40` 的数据，并返回 `String` 形式

:::caution
需要提示的是，执行的 SQL 语句一定要确保只会返回 `1` 行 `1` 列，否则会面临 dbVisitor 无法确认选择哪一个结果的问题。在这个情况下会引发异常。
:::

```java
String querySql = "select name from test_user where age > 40 order by age limit 1";
String result = jdbcTemplate.queryForObject(querySql, String.class);
```

```java title='数组传参'
String querySql = "select name from test_user where age > ? order by age limit 1";
Object[] queryArg = new Object[] { 40 };
String result = jdbcTemplate.queryForObject(querySql, queryArg, String.class);
```

```java title='Map传参'
String querySql = "select name from test_user where age > :age order by age limit 1";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
String result = jdbcTemplate.queryForObject(querySql, queryArg, String.class);
```

```text title='执行结果'
jon wes
```

:::tip
对于 `queryForObject(querySql, queryArg, String.class)` 这样的方法可以用简化的 `queryForString` 来替代
:::

## 执行 update 语句

将 id 为 1 的数据 name 字段更新为 mala，并返回影响行数

```java
String querySql = "update test_user set name = 'mala' where id = 1";
int result = jdbcTemplate.executeUpdate(querySql);
```

```java title='数组传参'
String querySql = "select count(*) from test_user where age > ?";
Object[] queryArg = new Object[] { 40 };
int result = jdbcTemplate.executeUpdate(querySql, queryArg);
```

```java title='Map传参'
String querySql = "select count(*) from test_user where age > :age";
Map<String, Object> queryArg = Collections.singletonMap("age", 40);
int result = jdbcTemplate.executeUpdate(querySql, queryArg);
```

```text title='执行结果'
1
```

## 执行 delete 语句

将 id 为 1 的数据删掉，并返回影响行数

```java
String querySql = "delete from test_user where id = 1";
int result = jdbcTemplate.executeUpdate(querySql);
```

```java title='数组传参'
String querySql = "delete from test_user where id = ?";
Object[] queryArg = new Object[] { 1 };
int result = jdbcTemplate.executeUpdate(querySql, queryArg);
```

```java title='Map传参'
String querySql = "delete from test_user where id = :id";
Map<String, Object> queryArg = Collections.singletonMap("id", 1);
int result = jdbcTemplate.executeUpdate(querySql, queryArg);
```

```text title='执行结果'
1
```

## 执行 insert 语句

使用 SQL 的方式新增一条数据，并返回影响行数

```java
String querySql = ""insert into `test_user` values (10, 'david', 26, now())"";
int result = jdbcTemplate.executeUpdate(querySql);
```

```java title='数组传参'
String querySql = "insert into `test_user` values (?,?,?,?)";
Object[] queryArg = new Object[] { 10, "'david'", 26, new Date() };
int result = jdbcTemplate.executeUpdate(querySql, queryArg);
```

```java title='Map传参'
String querySql = "insert into `test_user` values (:id , :name , :age , :create )";
Map<String, Object> queryArg = new HashMap<>();
queryArg.put("id", 10);
queryArg.put("name", "david");
queryArg.put("age", 26);
queryArg.put("create", new Date());
int result = jdbcTemplate.executeUpdate(querySql, queryArg);
```

```text title='执行结果'
1
```
