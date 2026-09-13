---
id: lindex
sidebar_position: 11
title: LINDEX
---

:::info[Note]
Official reference: [LINDEX](https://redis.io/docs/latest/commands/lindex/).
:::

Read one element by index.

## Syntax

```text
LINDEX key index
```

Indices start at zero; negative indices count from the end, with `-1` denoting the last element.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
LINDEX demo:queue 0
```
