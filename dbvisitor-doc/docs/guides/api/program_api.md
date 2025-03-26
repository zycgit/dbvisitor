---
id: program_api
sidebar_position: 1
hide_table_of_contents: true
title: 4.1 编程式 API
description: 编程式 API 的特点是通过编程方式基于 JdbcTemplate 核心类实现对数据库的访问。
---

# 编程式 API

编程式 API 的特点是通过编程方式基于 [JdbcTemplate](../core/jdbc/about) 核心类实现对数据库的访问，使用编程式 API 最大特点是具有强大的灵活性。

:::tip[特点]
- 注重编程的灵活性，以编辑使用 SQL 查询字符串为主要场景。
:::

```java title='1. 创建 JdbcTemplate 对象'
DataSource dataSource = ...
JdbcTemplate jdbc = new JdbcTemplate(dataSource);

或者

Connection conn = ...
JdbcTemplate jdbc = new JdbcTemplate(conn);
```

```java title='2. 执行 SQL'
jdbc.execute("create table user_info (id int primary key, name varchar(50))");

// 使用原始 SQL 语句
jdbc.executeUpdate("insert into user_info (id,name) values (1, 'Bob')");

// 使用位置参数
jdbc.executeUpdate("insert into user_info (id,name) values (?,?)",
                   new Object[] { 2, "Alice" });

// 使用名称参数
Map<String, Object> queryArg = CollectionUtils.asMap("id", 3, "name", "David");
jdbc.executeUpdate("insert into user_info (id,name) values (:id, :name)",
                   queryArg);

// 查询结果映射到任何类型
List<User> users = jdbc.queryForList("select * from user_info", User.class);
```

:::info[有关编程式 API 的详细信息，请参阅：]
- [JdbcTemplate 类](../core/jdbc/about)
:::
