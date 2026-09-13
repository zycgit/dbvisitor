---
id: object-freq
sidebar_position: 14
title: OBJECT FREQ
---

:::info[Note]
Official reference: [OBJECT FREQ](https://redis.io/docs/latest/commands/object-freq/).
:::

Read the access-frequency counter maintained by an LFU policy.

## Syntax

```text
OBJECT FREQ key
```

The example requires an existing key and an LFU eviction policy; otherwise the command is unavailable. Do not change production eviction settings to run it.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
OBJECT FREQ demo:message
```
