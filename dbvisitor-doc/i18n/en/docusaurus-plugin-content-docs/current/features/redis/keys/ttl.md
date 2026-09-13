---
id: ttl
sidebar_position: 18
title: TTL
---

:::info[Note]
Official reference: [TTL](https://redis.io/docs/latest/commands/ttl/).
:::

Read the remaining key lifetime in seconds.

## Syntax

```text
TTL key
```

`-1` means no expiration; `-2` means the key is missing.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello EX 60
TTL demo:message
```
