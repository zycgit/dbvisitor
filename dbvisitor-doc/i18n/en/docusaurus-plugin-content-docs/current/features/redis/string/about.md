---
id: about
slug: /features/redis/commands/string
sidebar_position: 0
hide_table_of_contents: true
title: String
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [SET](set.md) | Write a string, optionally conditional on whether the key exists. |
| [GET](get.md) | Read a string. |
| [INCR](incr.md) | Increment an integer by one, starting from zero for a missing key. |
| [INCRBY](incrby.md) | Increase a counter by an integer. |
| [DECR](decr.md) | Decrement an integer by one, starting from zero for a missing key. |
| [DECRBY](decrby.md) | Decrease a counter by an integer. |
| [APPEND](append.md) | Append text to a string. |
| [GETDEL](getdel.md) | Read a string and delete its key. |
| [GETEX](getex.md) | Read a string while setting or removing its expiration. |
| [GETRANGE](getrange.md) | Read a substring by byte offsets. |
| [GETSET](getset.md) | Replace a string and return its previous value. |
| [MGET](mget.md) | Read multiple string keys. |
| [MSET](mset.md) | Write multiple key-value pairs in one command. |
| [MSETNX](msetnx.md) | Write all pairs only when none of the target keys exists. |
| [PSETEX](psetex.md) | Write a string with an expiration in milliseconds. |
| [SETEX](setex.md) | Write a string with an expiration in seconds. |
| [SETNX](setnx.md) | Write a string only if the key does not exist. |
| [SETRANGE](setrange.md) | Overwrite a string starting at a byte offset. |
| [STRLEN](strlen.md) | Read the byte length of a string. |
| [SUBSTR](substr.md) | Read a substring by byte offsets; this is the old name of GETRANGE. |
