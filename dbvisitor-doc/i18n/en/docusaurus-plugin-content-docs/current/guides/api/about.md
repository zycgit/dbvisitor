---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 4.1 API Selection Guide
description: "Choose a dbVisitor API from three perspectives: whether to hand-write SQL, whether to use Mapper interfaces, and whether object mapping is needed."
---

# 4.1 API Selection Guide

dbVisitor's APIs can be chosen by asking three questions:

1. Do you want to write SQL directly?
2. Do you want to organize DAO code with Java interfaces?
3. Are you willing to set up object mapping so dbVisitor can generate SQL automatically?

## Quick Decision

| Your situation | Recommended entry | Notes |
|---|---|---|
| You already have SQL and just want lightweight execution | [Programmatic API](./jdbc) | Uses `JdbcTemplate` to execute SQL directly — closest to JDBC |
| You want DAO code as Java interfaces | [Mapper API](./mapper) | Organize methods with interfaces; SQL can come from Method Annotations, BaseMapper, or Mapper files |
| You don't want to hand-write SQL and prefer chainable conditions | [Fluent API](./lambda) | Builds SQL with `LambdaTemplate`, adapting to database dialects |
| Data is Map-based but you still want object mapping | [Mapped Map Mode](../core/map_query/mapped) | Reuses mapping metadata while using Map as the query and write carrier |
| Data is Map-based and no entity mapping is maintained | [Free Map Mode](../core/map_query/freedom) | Directly uses table names, column names, and Map operations |
| You need vector similarity search | [Vector Query](../core/vector_query/about) | Uses `orderBy*` for KNN and `vectorBy*` for range filtering |
| SQL is long with many dynamic fragments | [File Mapper](./file_mapper) | Keeps SQL in XML files for centralized maintenance |

All APIs can be mixed within the same DAO. For example, a single Mapper interface can use Method Annotations for short SQL, inherit BaseMapper for single-table CRUD, and reference Mapper files for long SQL — all at the same time.

## Object Mapping Requirement

| API | Requires object mapping | Notes |
|---|---|---|
| JdbcTemplate | No | Execute SQL directly; results can still be mapped to Beans |
| Mapper API Method Annotations | Not mandatory | SQL comes from annotations; result mapping available when returning Beans |
| Mapper File | Not mandatory | SQL comes from XML; `resultMap` or `entity` can be configured as needed |
| BaseMapper | Yes | Needs table, column, and primary-key mapping info to generate SQL |
| LambdaTemplate Entity mode | Yes | Needs entity classes and column mapping to build SQL |
| Map Query Mode (Mapped Map) | Yes | Reuses object mapping, but data carrier is `Map<String, Object>` |
| Vector Query | Usually | Needs mapped vector fields with TypeHandler configuration; ordering/filtering depends on data source dialect |
| Map Query Mode (Free Map) | No | Directly uses table names, column names, and Map operations |

## How They Relate

| Layer | API | Responsibility |
|---|---|---|
| SQL execution | [JdbcTemplate](./jdbc) | Execute SQL directly, handling parameters, results, batches, stored procedures |
| Mapper interface | [Mapper API](./mapper) | Organize DAO with Java interfaces. Supports Method Annotations, BaseMapper common CRUD, Mapper files |
| SQL building | [LambdaTemplate](./lambda) | Generate SQL with chainable APIs. Can be used standalone or entered from BaseMapper's builder methods |
| Map data carrier | [Map Query Mode](../core/map_query/about) | Queries, inserts, updates, deletes with Mapped Map or Free Map |
| Vector search | [Vector Query](../core/vector_query/about) | KNN ordering and range filtering on data sources that support vector search |

These APIs are not mutually exclusive. `JdbcTemplate` is more of a low-level execution tool, `Mapper API` is more of a DAO organization style, and `LambdaTemplate` is more of a SQL builder.

## Minimal Getting Started

```java title='With DataSource'
DataSource dataSource = ...;

JdbcTemplate jdbc = new JdbcTemplate(dataSource);
LambdaTemplate lambda = new LambdaTemplate(dataSource);

Configuration config = new Configuration();
Session session = config.newSession(dataSource);
```

```java title='With Connection'
Connection conn = ...;

JdbcTemplate jdbc = new JdbcTemplate(conn);
LambdaTemplate lambda = new LambdaTemplate(conn);

Configuration config = new Configuration();
Session session = config.newSession(conn);
```

Once you have the entry object, choose the specific API based on your scenario: use `JdbcTemplate` for hand-written SQL, `LambdaTemplate` for chainable construction, and `Session` to create Mapper interfaces when needed.

## Common Confusions

- Mapper API is not just Method Annotations; it is the interface-based access style. Method Annotations, BaseMapper, and Mapper files can all work with Mapper interfaces.
- BaseMapper is part of Mapper API, providing common CRUD capabilities. When complex queries are not a good fit, switch to Fluent API; complex SQL can go into annotations or Mapper files.
- LambdaTemplate's core value is building SQL and abstracting dialect differences — it does not replace every hand-written SQL use case. Complex JOINs, window functions, and database-specific syntax can still use JdbcTemplate or Mapper files.
- Map Query Mode includes two entry points: Mapped Map reuses object mapping, and Free Map directly uses table names, column names, and Map.
- Vector Query is a specialized capability built on top of Fluent API; availability depends on whether the database dialect and driver support vectors/KNN.
- Object mapping is a prerequisite for BaseMapper and LambdaTemplate Entity mode; if you only use JdbcTemplate to execute native SQL, object mapping is not required.

## Next Step

If you are integrating dbVisitor for the first time, start with [Using in Your Project](../yourproject/buildtools) to learn how to create `Session`, `JdbcTemplate`, or `LambdaTemplate`; then come back here to pick your API entry.
