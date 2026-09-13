---
id: touch
sidebar_position: 24
title: TOUCH
---

:::info[Note]
Official reference: [TOUCH](https://redis.io/docs/latest/commands/touch/).
:::

Update the access information of keys.

## Syntax

```text
TOUCH key [key ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of keys that were TOUCHed. |

## Example

```text
SET demo:message hello
TOUCH demo:message
```
