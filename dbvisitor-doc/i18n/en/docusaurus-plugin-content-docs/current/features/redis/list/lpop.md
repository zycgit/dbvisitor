---
id: lpop
sidebar_position: 5
title: LPOP
---

:::info[Note]
Official reference: [LPOP](https://redis.io/docs/latest/commands/lpop/).
:::

Pop elements from the left end.

## Syntax

```text
LPOP key [count]
```

Omit `count` to pop one element; otherwise pop at most `count` elements.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
LPOP demo:queue 2
```
