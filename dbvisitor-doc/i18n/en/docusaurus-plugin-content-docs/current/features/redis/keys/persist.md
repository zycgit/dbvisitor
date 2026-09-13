---
id: persist
sidebar_position: 17
title: PERSIST
---

:::info[Note]
Official reference: [PERSIST](https://redis.io/docs/latest/commands/persist/).
:::

Remove a key's expiration.

## Syntax

```text
PERSIST key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 when the key's expiry is removed; 0 when the key does not exist or has no expiry |

## Example

```text
SET demo:message hello EX 60
PERSIST demo:message
```
