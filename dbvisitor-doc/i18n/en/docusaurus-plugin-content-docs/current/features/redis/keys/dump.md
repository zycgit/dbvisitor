---
id: dump
sidebar_position: 4
title: DUMP
---

:::info[Note]
Official reference: [DUMP](https://redis.io/docs/latest/commands/dump/).
:::

Read the Redis-serialized value of a key.

## Syntax

```text
DUMP key
```

`VALUE` is binary `byte[]`, not the text returned by GET. Read it with `ResultSet.getBytes("VALUE")`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, BYTES type |

## Example

```text
SET demo:message hello
DUMP demo:message
```
