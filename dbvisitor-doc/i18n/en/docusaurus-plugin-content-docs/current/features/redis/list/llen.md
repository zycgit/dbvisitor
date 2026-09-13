---
id: llen
sidebar_position: 13
title: LLEN
---

:::info[Note]
Official reference: [LLEN](https://redis.io/docs/latest/commands/llen/).
:::

Read the list length.

## Syntax

```text
LLEN key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
RPUSH demo:queue first second
LLEN demo:queue
```
