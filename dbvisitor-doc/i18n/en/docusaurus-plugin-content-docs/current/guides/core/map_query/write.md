---
id: write
sidebar_position: 4
title: Map Insert, Update, And Delete
description: Use MapInsert, MapUpdate, and MapDelete to write, update, and delete Map data.
---

# Map Insert, Update, And Delete

Map write and mutation operations are provided by `MapInsert`, `MapUpdate`, and `MapDelete`. Mapped Map Mode and Freedom Map Mode use the same API set, but interpret fields differently.

## Suitable For

- Data is already `Map<String, Object>` and should be written or updated directly.
- Batch imports, sync tasks, or dynamic field writes are required.
- Update and delete operations should keep builder conditions and unsafe-operation protection.

## Not Suitable For

- Fields are fixed and compile-time checks are preferred. Use Entity Mode.
- Mutation SQL is complex and requires multi-table update, subqueries, or database-specific syntax. Use [JdbcTemplate](../jdbc/about) or [Mapper Files](../file/about).
- Entity CRUD by primary key is enough. Start with [BaseMapper](../mapper/about#base-mapper).

## Operation Modes

| Operation | Mapped Map Mode | Freedom Map Mode |
| --- | --- | --- |
| Insert | `lambda.insert(User.class).asMap()` | `lambda.insertFreedom("users")` |
| Update | `lambda.update(User.class).asMap()` | `lambda.updateFreedom("users")` |
| Delete | `lambda.delete(User.class).asMap()` | `lambda.deleteFreedom("users")` |

```text title='Field processing difference'
Mapped Map Mode
Map key -> object-mapping filtering and conversion -> database column

Freedom Map Mode
Map key -> treated as column name -> database column
```

## Insert Maps

```java title='Mapped Map insert'
Map<String, Object> data = new HashMap<>();
data.put("id", 1001);
data.put("loginName", "alice");
data.put("email", "alice@example.com");

int rows = lambda.insert(User.class)
        .asMap()
        .applyMap(data)
        .executeSumResult();
```

```java title='Freedom Map insert'
Map<String, Object> data = new HashMap<>();
data.put("id", 1001);
data.put("login_name", "alice");
data.put("email", "alice@example.com");

int rows = lambda.insertFreedom("users")
        .applyMap(data)
        .executeSumResult();
```

Mapped Map Mode filters and converts fields through object mapping; Freedom Map Mode generates SQL from caller-provided column names.

## Update Maps

`updateToSample` is for sample-style updates and updates non-null fields that are eligible for update.

```java title='Sample-style update'
Map<String, Object> sample = new HashMap<>();
sample.put("loginName", "alice_new");
sample.put("email", null);

int rows = lambda.update(User.class)
        .asMap()
        .eq("id", 1001)
        .updateToSample(sample)
        .doUpdate();
```

`updateRow` is for row-style updates, where fields in the Map are processed as row data.

```java title='Row-style update'
Map<String, Object> row = new HashMap<>();
row.put("login_name", "alice_new");
row.put("email", "alice_new@example.com");

int rows = lambda.updateFreedom("users")
        .eq("id", 1001)
        .updateRow(row)
        .doUpdate();
```

## Choose An Update Method

| Method | Semantics | Common Use |
| --- | --- | --- |
| `updateToSample(map)` | Sample-style update; skips fields that do not participate | Partial update from submitted form data |
| `updateRow(map)` | Row-data update; generates update fields from row data | Sync a whole row or overwrite row values |
| `updateTo("field", value)` | Explicitly update one field | Small field set or conditional update |

If there is nothing to update, `there nothing to update.` is raised. UPDATE without WHERE is blocked unless `allowEmptyWhere()` is explicit.

## Delete Maps

Map delete does not require row data. It only needs delete conditions.

```java title='Mapped Map delete'
int rows = lambda.delete(User.class)
        .asMap()
        .eq("id", 1001)
        .doDelete();
```

```java title='Freedom Map delete'
int rows = lambda.deleteFreedom("users")
        .eq("id", 1001)
        .doDelete();
```

DELETE without WHERE is blocked unless `allowEmptyWhere()` is explicit.

## Batch Insert

Batch insert fits import and sync tasks.

```java title='Batch insert'
List<Map<String, Object>> rows = Arrays.asList(row1, row2, row3);

int total = lambda.insert(User.class)
        .asMap()
        .applyMap(rows)
        .executeSumResult();
```

The field set in batch data should stay consistent. Mapped Map Mode filters fields through object mapping; Freedom Map Mode generates columns from Map keys.

## Further Reading

- [Mapped Map Mode](./mapped), Map operations that reuse object mapping.
- [Freedom Map Mode](./freedom), Map operations without object mapping.
- [LambdaTemplate Update](../lambda/update), update semantics in Entity Mode.
