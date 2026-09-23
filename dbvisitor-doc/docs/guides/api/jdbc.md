---
id: jdbc
sidebar_position: 2
hide_table_of_contents: true
title: 4.2 编程式 API
description: 编程式 API 基于 JdbcTemplate 执行驱动支持的 SQL、DSL 或原生命令，统一参数绑定与结果处理。
---

# 4.2 编程式 API

编程式 API 基于 [JdbcTemplate](../core/jdbc/about)，通过 JDBC 执行目标驱动支持的 **SQL、DSL 或原生命令**。它是应用适配层中直接控制命令内容的入口，统一参数绑定、执行调用与结果映射方式。

同一个 `JdbcTemplate` API 可以执行关系型 SQL，也可通过对应驱动执行 MongoDB 命令、Elasticsearch REST/Query DSL、Redis 命令和 Milvus SQL 风格命令。命令语法与能力由目标数据源和驱动决定，具体用法见[数据源差异](../../features/overview.md)。

## 适合场景

- 已经有明确的 SQL、DSL 或原生命令，希望复用参数绑定与结果映射。
- 查询较复杂，例如关系型 JOIN、子查询、窗口函数，或 NoSQL 专有查询与聚合。
- 需要直接控制批量执行、存储过程、回调方法、连接级操作。
- 不希望为了当前操作建立对象映射。

## 不适合场景

- 大量单表 CRUD，重复 SQL 会比较多；优先考虑 [Mapper API（BaseMapper）](./mapper)。
- 希望 DAO 以接口形式组织；优先考虑 [Mapper API](./mapper)。
- 希望通过链式条件生成 SQL 或目标命令；在方言支持时优先考虑 [构造器 API](./lambda)。

## 最小示例

下面以关系型 SQL 演示参数绑定和结果映射。

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
