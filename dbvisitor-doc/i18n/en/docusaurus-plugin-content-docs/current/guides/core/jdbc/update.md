---
id: update
sidebar_position: 3
title: Updates
description: Execute SQL statements with no result set, such as INSERT, UPDATE, DELETE, or DDL.
---

# Updates

`executeUpdate` executes SQL statements with no result set, such as INSERT, UPDATE, DELETE, or DDL.

## Start by Scenario

| Goal | Entry |
| --- | --- |
| Execute INSERT, UPDATE, or DELETE | `executeUpdate` |
| Execute simple DDL | `executeUpdate` or [Scripts](./execute) |
| Write many argument sets in batch | [Batches](./batch) |
| Execute SQL files or multiple scripts | [Scripts](./execute) |
| Need advanced argument binding | [Arguments](../../args/about) |

## Execute INSERT

```java title='No arguments'
int res = jdbc.executeUpdate("insert into users (id, name) values(2, 'Alice')");
```

```java title='Positional arguments'
Object[] args = new Object[] { 2, "Alice" };
int res = jdbc.executeUpdate("insert into users (id, name) values(?, ?)", args);
```

```java title='Named arguments'
Map<String, Object> args = new HashMap<>();
args.put("id", 2);
args.put("name", "Alice");

int res = jdbc.executeUpdate("insert into users (id, name) values(:id, :name)", args);
```

## Execute UPDATE

```java title='Positional arguments'
Object[] args = new Object[] { "Alice", 2 };
int res = jdbc.executeUpdate("update users set name = ? where id = ?", args);
```

```java title='Named arguments'
Map<String, Object> args = CollectionUtils.asMap("id", 2, "name", "Alice");
int res = jdbc.executeUpdate("update users set name = :name where id = :id", args);
```

## Execute DELETE

```java title='Positional arguments'
int res = jdbc.executeUpdate("delete from users where id = ?", new Object[] { 2 });
```

## Execute DDL

```java
int res = jdbc.executeUpdate("create table user_back(id bigint, name varchar(120));");
```

:::tip[Tip]
For SQL files, multiple scripts, or script resources that do not need arguments, read [Scripts](./execute).
:::

## Arguments

`executeUpdate` supports no arguments, positional arguments, named arguments, and custom argument-setting logic. The following examples cover common usage; see [Arguments](../../args/about) for the full feature set.

## Return Value

`JdbcTemplate` executes SQL through `PreparedStatement` or `Statement`. The SQL must be DML or a statement with no result set.

- DML (INSERT/UPDATE/DELETE): return value = affected rows.
- Statements with no return content (e.g., DDL): return value = `0`.

:::info
Although JDBC specifies the behavior above, actual drivers may behave differently. Refer to your JDBC driver documentation for exact semantics.
:::

## Related

- [Batches](./batch): batch writes with many argument sets or SQL statements.
- [Scripts](./execute): execute SQL script files or multiple scripts.
- [Arguments](../../args/about): positional arguments, named arguments, and custom argument sources.
