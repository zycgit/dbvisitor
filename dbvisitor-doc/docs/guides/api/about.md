---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 4.1 API 选择指南
description: 从是否手写 SQL、是否使用 Mapper 接口、是否需要对象映射三个角度选择 dbVisitor API。
---

# 4.1 API 选择指南

dbVisitor 的 API 可以按三个问题来选择：

1. 是否要直接编写 SQL？
2. 是否希望用 Java 接口组织 DAO？
3. 是否愿意建立对象映射，让 dbVisitor 自动生成 SQL？

## 先看结论

| 你的情况 | 建议入口 | 说明 |
|---------|---------|------|
| 已经有 SQL，只想轻量执行 | [编程式 API](./jdbc) | 直接使用 `JdbcTemplate` 执行 SQL，最接近 JDBC |
| 希望 DAO 是 Java 接口 | [Mapper API](./mapper) | 用接口组织方法，SQL 可以来自注解、BaseMapper 或 Mapper 文件 |
| 不想手写 SQL，希望链式拼条件 | [构造器 API](./lambda) | 基于 `LambdaTemplate` 构造 SQL，适配数据库方言 |
| 数据是 Map，但仍希望使用对象映射 | [映射 Map 模式](../core/map_query/mapped) | 复用映射信息，使用 Map 作为查询和写入载体 |
| 数据是 Map，且不维护实体映射 | [自由 Map 模式](../core/map_query/freedom) | 直接使用表名、列名和 Map 操作 |
| 需要向量相似性检索 | [向量查询](../core/vector_query/about) | 使用 `orderBy*` 做 KNN，使用 `vectorBy*` 做范围过滤 |
| SQL 很长，动态片段多 | [文件 Mapper](./file_mapper) | 把 SQL 放到 XML 文件中集中维护 |

所有 API 可以在同一个 DAO 中混合使用。例如一个 Mapper 接口可以同时用方法注解写短 SQL、继承 BaseMapper 做单表 CRUD、通过 Mapper 文件维护长 SQL。

## 是否需要对象映射

| API | 是否需要对象映射 | 说明 |
|-----|:---:|------|
| JdbcTemplate | 不需要 | 可以直接执行 SQL，也可以把结果映射到 Bean |
| Mapper API 方法注解 | 不强制 | SQL 由注解提供；返回 Bean 时可使用结果映射 |
| Mapper 文件 | 不强制 | SQL 由 XML 提供；可按需要配置 `resultMap` 或 `entity` |
| BaseMapper | 需要 | 依赖表、列、主键等对象映射信息自动生成 SQL |
| LambdaTemplate Entity 模式 | 需要 | 依赖实体类和列映射构造 SQL |
| Map 查询模式（映射 Map） | 需要 | 复用对象映射，但数据载体是 `Map<String, Object>` |
| 向量查询 | 通常需要 | 需要映射向量字段并配置 TypeHandler；排序/过滤能力取决于数据源方言 |
| Map 查询模式（自由 Map） | 不需要 | 直接使用表名、列名和 Map 操作 |

## API 之间的关系

| 层次 | API | 职责 |
|-----|-----|------|
| SQL 执行层 | [JdbcTemplate](./jdbc) | 直接执行 SQL，处理参数、结果、批量、存储过程 |
| Mapper 接口层 | [Mapper API](./mapper) | 用 Java 接口组织 DAO。支持方法注解、BaseMapper 通用 CRUD、Mapper 文件 |
| SQL 构造层 | [LambdaTemplate](./lambda) | 用链式 API 生成 SQL。可独立使用，也可从 BaseMapper 的构造器方法进入 |
| Map 数据载体 | [Map 查询模式](../core/map_query/about) | 使用映射 Map 或自由 Map 完成查询、写入、更新、删除 |
| 向量检索层 | [向量查询](../core/vector_query/about) | 在支持向量的数据源上做 KNN 排序和范围过滤 |

这几种 API 不是互斥关系。`JdbcTemplate` 更像底层执行工具，`Mapper API` 更像 DAO 组织方式，`LambdaTemplate` 更像 SQL 构造器。

## 最小起步方式

```java title='已有 DataSource'
DataSource dataSource = ...;

JdbcTemplate jdbc = new JdbcTemplate(dataSource);
LambdaTemplate lambda = new LambdaTemplate(dataSource);

Configuration config = new Configuration();
Session session = config.newSession(dataSource);
```

```java title='已有 Connection'
Connection conn = ...;

JdbcTemplate jdbc = new JdbcTemplate(conn);
LambdaTemplate lambda = new LambdaTemplate(conn);

Configuration config = new Configuration();
Session session = config.newSession(conn);
```

拿到入口对象后，再根据场景选择具体 API：直接写 SQL 用 `JdbcTemplate`，链式构造用 `LambdaTemplate`，需要 Mapper 接口时通过 `Session` 创建。

## 容易混淆的边界

- Mapper API 不是只指方法注解；它是接口化访问方式。方法注解、BaseMapper、Mapper 文件都可以和 Mapper 接口配合。
- BaseMapper 是 Mapper API 的一部分，提供通用 CRUD 能力。不适合表达复杂查询时可切换到构造器 API，复杂 SQL 可以放到注解或 Mapper 文件。
- LambdaTemplate 的核心价值是构造 SQL 和屏蔽方言差异，不是替代所有手写 SQL 场景。复杂 JOIN、窗口函数、数据库专有语法仍可用 JdbcTemplate 或 Mapper 文件。
- Map 查询模式包含映射 Map 和自由 Map 两种入口：映射 Map 复用对象映射，自由 Map 直接使用表名、列名和 Map。
- 向量查询是构造器 API 之上的专题能力，是否可用取决于数据库方言和驱动是否支持向量/KNN。
- 对象映射是 BaseMapper 和 LambdaTemplate Entity 模式的前置能力；如果只使用 JdbcTemplate 执行原生 SQL，可以不建立对象映射。

## 下一步阅读

首次接入时，建议先阅读 [在项目中使用](../yourproject/buildtools)，确认如何创建 `Session`、`JdbcTemplate` 或 `LambdaTemplate`；再选择具体 API 入口。
