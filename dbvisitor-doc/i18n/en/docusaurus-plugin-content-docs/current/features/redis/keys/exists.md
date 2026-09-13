---
id: exists
sidebar_position: 5
title: EXISTS
---

:::info[Note]
Official reference: [EXISTS](https://redis.io/docs/latest/commands/exists/).
:::

Count existing keys among the supplied names.

## Syntax

```text
EXISTS key [key ...]
```

Repeated names of existing keys are counted repeatedly.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello
EXISTS demo:message demo:missing
```
