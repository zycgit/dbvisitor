---
id: ltrim
sidebar_position: 22
title: LTRIM
---

:::info[Note]
Official reference: [LTRIM](https://redis.io/docs/latest/commands/ltrim/).
:::

Keep only the elements in an index range.

## Syntax

```text
LTRIM key start stop
```

`start/stop` are zero-based and inclusive; `-1` denotes the last element.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if successful, 0 otherwise. (Success when status is "OK") |

## Example

```text
RPUSH demo:queue first second third
LTRIM demo:queue 0 1
```
