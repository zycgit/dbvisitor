---
id: about
slug: /features/redis/commands/list
sidebar_position: 0
hide_table_of_contents: true
title: List
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [LMOVE](lmove.md) | Move one element between specified ends of two lists. |
| [BLMOVE](blmove.md) | Wait for an element and move it to another list. |
| [LMPOP](lmpop.md) | Pop elements from the first nonempty list. |
| [BLMPOP](blmpop.md) | Wait for elements and pop from the first nonempty list. |
| [LPOP](lpop.md) | Pop elements from the left end. |
| [RPOP](rpop.md) | Pop elements from the right end. |
| [BLPOP](blpop.md) | Wait for an element and pop from the left end. |
| [BRPOP](brpop.md) | Wait for an element and pop from the right end. |
| [RPOPLPUSH](rpoplpush.md) | Move the source tail element to the destination head. |
| [BRPOPLPUSH](brpoplpush.md) | Wait and move the source tail element to the destination head. |
| [LINDEX](lindex.md) | Read one element by index. |
| [LINSERT](linsert.md) | Insert before or after the first matching pivot. |
| [LLEN](llen.md) | Read the list length. |
| [LPOS](lpos.md) | Find the positions of an element. |
| [LPUSH](lpush.md) | Push elements onto the left end in argument order. |
| [LPUSHX](lpushx.md) | Push onto the left end only if the list exists. |
| [RPUSH](rpush.md) | Push elements onto the right end in argument order. |
| [RPUSHX](rpushx.md) | Push onto the right end only if the list exists. |
| [LRANGE](lrange.md) | Read elements in an index range. |
| [LREM](lrem.md) | Remove occurrences of an element. |
| [LSET](lset.md) | Replace an element at an index. |
| [LTRIM](ltrim.md) | Keep only the elements in an index range. |
