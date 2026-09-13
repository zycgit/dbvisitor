---
id: about
slug: /features/redis/commands/server
sidebar_position: 0
hide_table_of_contents: true
title: Server Commands
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [EVAL](eval.md) | Execute a server-side Lua script. |
| [MOVE](move.md) | Move a key to another logical database. |
| [WAIT](wait.md) | Wait for a number of replicas to acknowledge preceding writes. |
| [WAITAOF](waitaof.md) | Wait for preceding writes to reach local or replica AOF persistence. |
| [PING](ping.md) | Check the connection, optionally echoing a message. |
| [ECHO](echo.md) | Echo the supplied text. |
| [SELECT](select.md) | Switch the logical database of the current connection. |
| [INFO](info.md) | Read server information as metric rows. |
