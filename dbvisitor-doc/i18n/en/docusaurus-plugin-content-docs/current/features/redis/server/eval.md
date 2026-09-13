---
id: eval
sidebar_position: 8
title: EVAL
---

:::info[Note]
Official reference: [EVAL](https://redis.io/docs/latest/commands/eval/).
:::

Execute a Lua script on Redis.

## Syntax

```text
EVAL script numkeys [key ...] [arg ...]
```

`numkeys` counts the following key arguments, exposed as `KEYS` in Lua. Remaining arguments are available as `ARGV`. Declare every accessed key explicitly.

## Results

One row with a `VALUE` column. `getObject()` preserves the SDK reply: numbers, strings, null, or lists. Nested lists are not expanded into table columns.

Binding a script, key or argument as `byte[]` uses the binary API. Binary string replies remain `byte[]` and can be read with `getBytes()`.

## Example

```java
Integer sum = jdbc.queryForObject(
        "EVAL 'return tonumber(ARGV[1]) + tonumber(ARGV[2])' 0 ? ?",
        new Object[] { 10, 5 }, Integer.class);
```

Bind business values through parameters; do not concatenate them into Lua source. Script errors propagate as JDBC exceptions. Script execution is atomic, but writes already performed by a failing script are not rolled back.
