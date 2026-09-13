---
id: rpushx
sidebar_position: 18
title: RPUSHX
---

:::info[Note]
Official reference: [RPUSHX](https://redis.io/docs/latest/commands/rpushx/).
:::

Push onto the right end only if the list exists.

## Syntax

```text
RPUSHX key element [element ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Length of the list after the PUSH operation. |

## Example

```text
RPUSH demo:queue first
RPUSHX demo:queue second
```
