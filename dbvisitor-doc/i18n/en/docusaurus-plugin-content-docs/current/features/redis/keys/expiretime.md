---
id: expiretime
sidebar_position: 8
title: EXPIRETIME
---

:::info[Note]
Official reference: [EXPIRETIME](https://redis.io/docs/latest/commands/expiretime/).
:::

Read the key expiration timestamp in seconds.

## Syntax

```text
EXPIRETIME key
```

`-1` means no expiration; `-2` means the key is missing.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello EX 60
EXPIRETIME demo:message
```
