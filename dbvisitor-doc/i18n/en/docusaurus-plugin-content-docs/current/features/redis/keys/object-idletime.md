---
id: object-idletime
sidebar_position: 15
title: OBJECT IDLETIME
---

:::info[Note]
Official reference: [OBJECT IDLETIME](https://redis.io/docs/latest/commands/object-idletime/).
:::

Read the time in seconds since a key was last accessed.

## Syntax

```text
OBJECT IDLETIME key
```

This command is unavailable with LFU eviction policies.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello
OBJECT IDLETIME demo:message
```
