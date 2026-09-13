---
id: get
sidebar_position: 2
title: GET
---

:::info[Note]
Official reference: [GET](https://redis.io/docs/latest/commands/get/).
:::

Read a string.

## Syntax

```text
GET key
```

A missing key returns one row with a null `VALUE`.

A string key returns text. Bind the key as `byte[]` to read binary data with `getBytes()`; see [Type support](../dbvisitor/types.md).

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE: String in text mode; byte[] in binary mode |

## Example

```text
SET demo:message hello
GET demo:message
```
