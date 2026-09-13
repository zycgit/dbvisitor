---
id: pttl
sidebar_position: 19
title: PTTL
---

:::info[Note]
Official reference: [PTTL](https://redis.io/docs/latest/commands/pttl/).
:::

Read the remaining key lifetime in milliseconds.

## Syntax

```text
PTTL key
```

`-1` means no expiration; `-2` means the key is missing.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello PX 60000
PTTL demo:message
```
