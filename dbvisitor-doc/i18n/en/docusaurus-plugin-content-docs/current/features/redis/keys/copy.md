---
id: copy
sidebar_position: 1
title: COPY
---

:::info[Note]
Official reference: [COPY](https://redis.io/docs/latest/commands/COPY/).
:::

Copy a key, optionally to another logical database.

## Syntax

```text
COPY source destination [DB database] [REPLACE]
```

`DB` selects the destination logical database, defaulting to the current one. `REPLACE` allows overwriting an existing destination.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if successful; 0 if failed |

## Example

```text
SET demo:original hello
COPY demo:original demo:copy REPLACE
```
