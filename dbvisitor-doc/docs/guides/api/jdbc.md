---
id: jdbc
sidebar_position: 2
hide_table_of_contents: true
title: 4.2 编程式 API
description: 编程式 API 基于 JdbcTemplate 直接执行 SQL，适合需要完全控制 SQL 的场景。
---

# 4.2 编程式 API

编程式 API 基于 [JdbcTemplate](../core/jdbc/about)，以 **SQL 字符串** 为中心访问数据库。它是 dbVisitor 中最底层、最自由的关系型数据库入口。

## 适合场景

- 已经有明确 SQL，希望轻量执行。
- SQL 较复杂，例如 JOIN、子查询、窗口函数、数据库专有语法。
- 需要直接控制批量执行、存储过程、回调方法、连接级操作。
- 不希望为了当前操作建立对象映射。

## 不适合场景

- 大量单表 CRUD，重复 SQL 会比较多；优先考虑 [Mapper API（BaseMapper）](./mapper)。
- 希望 DAO 以接口形式组织；优先考虑 [Mapper API](./mapper)。
- 希望通过链式条件生成 SQL；优先考虑 [构造器 API](./lambda)。

## 最小示例

```java title='已有 DataSource'
DataSource dataSource = ...;
JdbcTemplate jdbc = new JdbcTemplate(dataSource);

List<User> users = jdbc.queryForList(
        "select * from users where age >= :age",
        CollectionUtils.asMap("age", 18),
        User.class);
```

```java title='已有 Connection'
Connection conn = ...;
JdbcTemplate jdbc = new JdbcTemplate(conn);

List<User> users = jdbc.queryForList(
        "select * from users where age >= :age",
        CollectionUtils.asMap("age", 18),
        User.class);
```

## 深入阅读

- [JdbcTemplate](../core/jdbc/about)：完整能力入口。
- [参数传递](../args/about)：位置参数、名称参数、接口传参。
- [结果接收](../result/about)：RowMapper、Map、ResultSetExtractor 等结果处理方式。
