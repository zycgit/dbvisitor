---
id: select
sidebar_position: 6
title: SELECT
---

:::info[Note]
Official reference: [SELECT](https://redis.io/docs/latest/commands/select/).
:::

Switch the logical database of the current connection.

## Syntax

```text
SELECT database
```

Logical database numbers start at zero. Redis Cluster does not support switching between logical databases.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Returns 1 on success; throws exception otherwise |

## Example

```text
SELECT 1
SELECT 0
```
