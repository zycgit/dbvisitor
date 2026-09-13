---
id: keys
sidebar_position: 12
title: KEYS
---

:::info[Note]
Official reference: [KEYS](https://redis.io/docs/latest/commands/keys/).
:::

Find matching keys; the driver iterates with SCAN.

## Syntax

```text
KEYS pattern
```

`pattern` uses Redis glob matching, such as `demo:*`. `fetchSize` supplies the SCAN COUNT hint; `maxRows` caps total returned rows. Traversal is unordered and concurrent changes can produce duplicates.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | KEY field, STRING type (Note: the driver uses the scan command instead of the keys command) |

## Example

```text
SET demo:message hello
KEYS demo:*
```
