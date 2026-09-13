---
id: info
sidebar_position: 7
title: INFO
---

:::info[Note]
Official reference: [INFO](https://redis.io/docs/latest/commands/info/).
:::

Read server information as metric rows.

## Syntax

```text
INFO [section]
```

Omit `section` for default information or select one group, such as `server`, `memory` or `stats`. `GROUP` is the group name, `NAME` the metric name and `VALUE` its raw text.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | GROUP, NAME and VALUE fields, all STRING. |

## Example

```text
INFO server
```
