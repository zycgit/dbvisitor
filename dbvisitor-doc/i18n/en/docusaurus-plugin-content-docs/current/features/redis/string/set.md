---
id: set
sidebar_position: 1
title: SET
---

:::info[Note]
Official reference: [SET](https://redis.io/docs/latest/commands/set/).
:::

Write a string, optionally conditional on whether the key exists.

## Syntax

```text
SET key value [NX | XX] [GET] [EX seconds | PX milliseconds | EXAT unix-seconds | PXAT unix-milliseconds | KEEPTTL]
```

`NX` creates only; `XX` overwrites only. `GET` returns the previous value. `EX/PX` specify seconds/milliseconds to live; `EXAT/PXAT` specify Unix timestamps. `KEEPTTL` retains expiration. Keep options in the order shown.

Expiration arguments support 64-bit integers, as literals or bound Java `long` values. `PXAT` accepts Unix millisecond timestamps directly.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count / Result set | --/1 | Value: When not using GET, returns 0 (not set) or 1 (set successfully)<br/>ResultSet: When using GET, VALUE field, STRING type |

Binding a `byte[]` key or value uses binary SET. With `GET`, `VALUE` is a `byte[]` and can be read with `getBytes()`.

## Example

```text
SET demo:message hello EX 60
SET demo:message world XX GET KEEPTTL
```
