---
id: blmpop
sidebar_position: 4
title: BLMPOP
---

:::info[Note]
Official reference: [BLMPOP](https://redis.io/docs/latest/commands/blmpop/).
:::

Wait for elements and pop from the first nonempty list.

## Syntax

```text
BLMPOP timeout numkeys key [key ...] LEFT|RIGHT [COUNT count]
```

`numkeys` must match the key count. Only the first nonempty list in argument order is used. `COUNT` is the maximum number to pop, default 1.

`timeout` is in seconds. Use a nonnegative integer literal; `0` waits indefinitely. The example inserts elements before waiting.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | KEY field, STRING type (returns Key from Key,ValueList structure)<br/>ELEMENT field, STRING type |

:::caution[Caution]
When all source lists are empty (or a blocking call times out), the current driver can fail rather than reliably return an empty ResultSet. Use a Redis client when this case must be handled.
:::

## Example

```text
RPUSH demo:queue first second
BLMPOP 1 1 demo:queue LEFT COUNT 2
```
