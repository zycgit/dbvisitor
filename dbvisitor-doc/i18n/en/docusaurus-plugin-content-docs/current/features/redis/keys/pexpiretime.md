---
id: pexpiretime
sidebar_position: 11
title: PEXPIRETIME
---

:::info[Note]
Official reference: [PEXPIRETIME](https://redis.io/docs/latest/commands/pexpiretime/).
:::

Read the key expiration timestamp in milliseconds.

## Syntax

```text
PEXPIRETIME key
```

`-1` means no expiration; `-2` means the key is missing.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello EX 60
PEXPIRETIME demo:message
```
