---
id: rpop
sidebar_position: 6
title: RPOP
---

:::info[Note]
Official reference: [RPOP](https://redis.io/docs/latest/commands/rpop/).
:::

Pop elements from the right end.

## Syntax

```text
RPOP key [count]
```

Omit `count` to pop one element; otherwise pop at most `count` elements.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
RPOP demo:queue 2
```
