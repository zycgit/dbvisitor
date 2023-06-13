---
sidebar_position: 5
title: 多语句/多返回值
description: dbVisitor ORM 工具可以支持多条语句同时执行，预计多个返回值的情况。
---

# 多语句/多返回值

通过多语句能力可以让应用发起更加复杂的 SQL 脚本查询，并一起将它们发送给数据库然后接收所有的返回值。例如：

下面这个查询 SQL 先是将查询参数存入 MySQL 变量 `userName`，然后在通过查询语句引用这个变量。

```java
String querySql = "set @userName = convert(? USING utf8); " + 
                  "select * from test_user where name = @userName;";
Object[] queryArg = new Object[] { "dative" };

List<Object> resultList = jdbcTemplate.multipleExecute(querySql, queryArg);
```

由于是两条 SQL 语句，因此 `resultList` 的结果有两个

- 第一个元素是 `set` 语句的执行结果
- 第二个元素是 `select` 语句的执行结果
