---
id: ping
sidebar_position: 4
title: PING
---

:::info[Note]
Official reference: [PING](https://redis.io/docs/latest/commands/ping/).
:::

Check the connection, optionally echoing a message.

## Syntax

```text
PING [message]
```

Without an argument, returns `PONG`; otherwise returns the supplied text.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, STRING type |

## Example

```text
PING
PING hello
```
