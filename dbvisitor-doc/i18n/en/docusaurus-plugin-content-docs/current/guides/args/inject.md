---
id: inject
sidebar_position: 4
title: 6.3 SQL Text Substitution
description: Use ${...} to fetch named arguments and inject the result into SQL text.
---

# SQL Text Substitution

:::warning[Please note]
`${...}` substitutes text directly; it is not PreparedStatement binding. For table names, columns and ordering, map application allowlist choices to fixed SQL fragments rather than accepting raw user input.
:::

Use `${...}` to fetch named arguments and inject the result into SQL text.

```text title='Example: Choose a sort fragment from an allowlist'
select * from users where id > #{id} order by ${order}
```

## Basic Usage

```java
Map<String, Object> args = CollectionUtils.asMap(
        "id", 2,
        "order", "name desc"
);
jdbcTemplate.queryForList("select * from users where id > #{id} order by ${order}", args);
```

## Common Scenarios

`${...}` is suitable for scenarios where the SQL structure itself needs to change dynamically, for example:

```text title='Dynamic table name'
select * from ${tableName} where id = #{id}
```

```text title='Dynamic column names'
select ${columns} from users where id = #{id}
```

```text title='Dynamic ordering'
select * from users order by ${orderBy}
```

## Difference from `#{...}`

| Syntax | Behavior | Safety |
|--------|----------|--------|
| `#{...}` | Generates a `?` placeholder and binds the argument value via PreparedStatement | Separates values from SQL structure; the application must still control SQL templates and OGNL expressions |
| `${...}` | Evaluates via OGNL and splices the result directly into the SQL string | **Unsafe**, SQL injection risk |

:::tip[Principle]
Prefer `#{...}`. Only use `${...}` when the SQL structure (table name, column name, ordering, etc.) needs to change dynamically.
:::
