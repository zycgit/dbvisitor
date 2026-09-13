---
id: lmpop
sidebar_position: 3
title: LMPOP
---

:::info[Note]
Official reference: [LMPOP](https://redis.io/docs/latest/commands/lmpop/).
:::

Pop elements from the first nonempty list.

## Syntax

```text
LMPOP numkeys key [key ...] LEFT|RIGHT [COUNT count]
```

`numkeys` must match the key count. Only the first nonempty list in argument order is used. `COUNT` is the maximum number to pop, default 1.

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
LMPOP 1 demo:queue LEFT COUNT 2
```
