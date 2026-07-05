---
id: lambda
sidebar_position: 5
hide_table_of_contents: true
title: 4.5 构造器 API
description: 构造器 API 基于 LambdaTemplate 通过链式方法生成 SQL，并处理数据库方言差异。
---

# 4.5 构造器 API

构造器 API 基于 [LambdaTemplate](../core/lambda/about)，用链式方法描述 INSERT、UPDATE、DELETE、QUERY 等操作，由 dbVisitor 生成 SQL。

## 适合场景

- 希望少写或不写 SQL。
- 查询条件组合较多，不想手工拼接 WHERE。
- 希望同一套 API 适配不同数据库方言。
- 需要在 BaseMapper 的预设 CRUD 之外表达更复杂条件。

## 不适合场景

- SQL 已经固定且很复杂，直接写 SQL 更清楚。
- 需要大量数据库专有语法、窗口函数、复杂 JOIN。
- 希望 SQL 完全由 DBA 或独立 XML 文件维护。

这些场景可使用 [JdbcTemplate](./jdbc) 或 [文件 Mapper](./file_mapper)。

## 三种模式

| 模式 | 是否需要对象映射 | 适合场景 |
| --- | --- | --- |
| Entity 模式 | 需要 | 有实体类，使用 `User::getName` 这类方法引用操作列。 |
| Map 模式 | 需要 | 有表映射，但数据载体使用 `Map`。 |
| Freedom 模式 | 不需要 | 没有实体映射，直接使用表名和字符串列名。 |

## 最小示例

```java title='已有 DataSource'
DataSource dataSource = ...;
LambdaTemplate lambda = new LambdaTemplate(dataSource);

List<User> users = lambda.query(User.class)
        .eq(User::getStatus, "ACTIVE")
        .ge(User::getAge, 18)
        .queryForList();
```

```java title='已有 Connection'
Connection conn = ...;
LambdaTemplate lambda = new LambdaTemplate(conn);

List<User> users = lambda.query(User.class)
        .eq(User::getStatus, "ACTIVE")
        .ge(User::getAge, 18)
        .queryForList();
```

```java title='Freedom 模式'
Map<String, Object> row = lambda.queryFreedom("users")
        .eq("id", 1)
        .queryForObject();
```

## 深入阅读

- [LambdaTemplate](../core/lambda/about)：构造器 API 的完整说明。
- [条件构造器](../core/lambda/where_builder)：WHERE 条件构造能力。
- [对象映射](../core/mapping/about)：Entity 模式依赖的表和列映射。
