---
id: echo
sidebar_position: 5
title: ECHO
---

:::info[Note]
Official reference: [ECHO](https://redis.io/docs/latest/commands/echo/).
:::

Echo the supplied text.

## Syntax

```text
ECHO message
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, STRING type |

## Example

```text
ECHO hello
```
