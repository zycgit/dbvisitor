---
id: waitaof
sidebar_position: 3
title: WAITAOF
---

:::info[Note]
Official reference: [WAITAOF](https://redis.io/docs/latest/commands/waitaof/).
:::

Wait for preceding writes to reach local or replica AOF persistence.

## Syntax

```text
WAITAOF numlocal replicas timeout
```

Requires Redis 7.2+. `numlocal` is 0 or 1, `replicas` is the target replica count, and `timeout` is in milliseconds. Local persistence requires AOF. Returned counts report completion and do not roll back preceding writes.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | LOCAL field, LONG type<br/>REPLICAS field, LONG type |

## Example

```text
SET demo:message hello
WAITAOF 1 0 1000
```
