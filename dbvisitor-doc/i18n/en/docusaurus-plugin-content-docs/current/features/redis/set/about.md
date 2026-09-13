---
id: about
slug: /features/redis/commands/set
sidebar_position: 0
hide_table_of_contents: true
title: Set
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [SADD](sadd.md) | Add unique members to a set. |
| [SCARD](scard.md) | Read the set cardinality. |
| [SDIFF](sdiff.md) | Read members of the first set absent from the other sets. |
| [SDIFFSTORE](sdiffstore.md) | Store a set difference at the destination. |
| [SINTER](sinter.md) | Read members common to all input sets. |
| [SINTERCARD](sintercard.md) | Count intersection members, optionally up to a limit. |
| [SINTERSTORE](sinterstore.md) | Store a set intersection at the destination. |
| [SISMEMBER](sismember.md) | Check whether a member belongs to a set. |
| [SMISMEMBER](smismember.md) | Check membership for several members in order. |
| [SMEMBERS](smembers.md) | Read all set members. |
| [SMOVE](smove.md) | Move a member between sets. |
| [SPOP](spop.md) | Remove and return random set members. |
| [SRANDMEMBER](srandmember.md) | Read random set members without removing them. |
| [SREM](srem.md) | Remove specified set members. |
| [SSCAN](sscan.md) | Read one cursor batch of set members. |
| [SUNION](sunion.md) | Read the union of input sets. |
| [SUNIONSTORE](sunionstore.md) | Store a set union at the destination. |
