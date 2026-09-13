---
id: wait
sidebar_position: 2
title: WAIT
---

:::info[Note]
Official reference: [WAIT](https://redis.io/docs/latest/commands/wait/).
:::

Wait for a number of replicas to acknowledge preceding writes.

## Syntax

```text
WAIT replicas timeout
```

`replicas` is the required acknowledgment count; `timeout` is in milliseconds, with zero waiting indefinitely. The returned count can fall short on timeout; preceding writes are not rolled back.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | REPLICAS field, LONG type |

## Example

```text
SET demo:message hello
WAIT 1 1000
```
