---
id: move
sidebar_position: 1
title: MOVE
---

:::info[Note]
Official reference: [MOVE](https://redis.io/docs/latest/commands/move/).
:::

Move a key to another logical database.

## Syntax

```text
MOVE key database
```

Logical database numbers start at zero. Redis Cluster does not support switching between logical databases.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if key was moved; 0 otherwise |

## Example

```text
SET demo:message hello
MOVE demo:message 1
```
