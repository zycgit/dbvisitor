---
id: freedom
sidebar_position: 3
title: Freedom Map Mode
description: Build SQL directly from table names, column names, and Map data without entity classes or object mapping.
---

# Freedom Map Mode

Freedom Map Mode does not need entity classes and does not read object mapping. It is a peer Map API entrance alongside Mapped Map Mode; the difference is that table and column names are provided directly by the caller.

## Suitable For

- There is no entity class, or temporary tables, dynamic tables, or sync tasks should not have entity mappings.
- Table names and column names come from task metadata, configuration, or runtime rules.
- The target is a non-traditional tabular source such as MongoDB, ElasticSearch, or Milvus, while the unified builder API is still preferred.
- Map keys should be organized by database column names rather than Java property names.

## Not Suitable For

- Entity-field TypeHandler, write policy, primary-key configuration, or field filtering should be reused.
- Field names need compile-time checks or object-mapping validation.
- Table or column names come directly from user input and cannot be allowlisted.

## Mode Relationship

```text title='Freedom Map Mode'
Caller provides table name
lambda.queryFreedom("users")
        |
        v
Caller provides column names
eq("login_name", "alice")
        |
        v
SQL identifiers enter the statement directly
```

Freedom Map Mode reuses the builder API without entity mapping; conditions, sorting, grouping and paging still depend on the dialect and data source. Redis does not support this builder; use native commands.

## Entrance

```java title='Freedom Map entrances'
MapQuery query = lambda.queryFreedom("users");
MapInsert insert = lambda.insertFreedom("users");
MapUpdate update = lambda.updateFreedom("users");
MapDelete delete = lambda.deleteFreedom("users");
```

Use the three-argument entrance when catalog or schema must be explicit:

```java
MapQuery query = lambda.queryFreedom("catalog_name", "schema_name", "users");
```

## Query By Database Columns

Field names are normally interpreted as database column names. Freedom Map Mode does not check them against entity mapping and does not automatically filter unknown columns.

```java title='Query by database columns'
List<Map<String, Object>> rows = lambda.queryFreedom("users")
        .eq("login_name", "alice")
        .select("id", "login_name", "email")
        .queryForList();
```

SQL shape:

```sql
SELECT id, login_name, email FROM users WHERE login_name = ?
```

If camel-case-to-underscore conversion is enabled, Map keys are converted according to that option; otherwise keys are used as provided.

## Insert, Update, And Delete

```java title='Freedom insert'
Map<String, Object> row = new HashMap<>();
row.put("id", 1001);
row.put("login_name", "alice");
row.put("email", "alice@example.com");

int rows = lambda.insertFreedom("users")
        .applyMap(row)
        .executeSumResult();
```

```java title='Freedom update'
Map<String, Object> row = new HashMap<>();
row.put("login_name", "alice_new");

int rows = lambda.updateFreedom("users")
        .eq("id", 1001)
        .updateTo("login_name", row.get("login_name"))
        .doUpdate();
```

```java title='Freedom delete'
int rows = lambda.deleteFreedom("users")
        .eq("id", 1001)
        .doDelete();
```

See [Map Insert, Update, And Delete](./write) for more write and update differences.

## Identifier Safety

SQL values are still bound as parameters; table and column names are SQL identifiers and cannot be bound like values.

```text title='Parameter binding boundary'
Value:      eq("id", 1001)       -> bound as a parameter
Column:     eq("id", 1001)       -> id is a SQL identifier
Table:      queryFreedom("users") -> users is a SQL identifier
```

If table or column names come from external input, validate them with an allowlist first.

## Boundaries

- Any Map key may become a column name.
- If the column name is wrong, the database usually reports the error when SQL executes.
- Type handling mainly follows Map values and runtime types; entity-field TypeHandler configuration is not used.
- UPDATE and DELETE without WHERE are blocked unless `allowEmptyWhere()` is explicit.

## Further Reading

- [Mapped Map Mode](./mapped), Map entrance that reuses object mapping.
- [Map Insert, Update, And Delete](./write), Map mutation operations.
- [Where Builder](../lambda/where_builder), scalar predicates and dynamic conditions.
