---
id: about
slug: /features/redis/commands/hash
sidebar_position: 0
hide_table_of_contents: true
title: Hash
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [HDEL](hdel.md) | Remove fields from a hash. |
| [HEXISTS](hexists.md) | Check whether a hash field exists. |
| [HEXPIRE](hexpire.md) | Set field lifetimes in seconds. |
| [HEXPIREAT](hexpireat.md) | Set field expiration using a Unix timestamp in seconds. |
| [HEXPIRETIME](hexpiretime.md) | Read field expiration timestamps in seconds. |
| [HPEXPIRE](hpexpire.md) | Set field lifetimes in milliseconds. |
| [HPEXPIREAT](hpexpireat.md) | Set field expiration using a Unix timestamp in milliseconds. |
| [HPEXPIRETIME](hpexpiretime.md) | Read field expiration timestamps in milliseconds. |
| [HGET](hget.md) | Read one hash field. |
| [HGETALL](hgetall.md) | Read every field and value of a hash. |
| [HINCRBY](hincrby.md) | Increase a hash field by an integer. |
| [HKEYS](hkeys.md) | List hash field names. |
| [HLEN](hlen.md) | Read the number of hash fields. |
| [HMGET](hmget.md) | Read multiple fields in the requested order. |
| [HSET](hset.md) | Add or overwrite hash fields. |
| [HMSET](hmset.md) | Write multiple hash fields. |
| [HSETNX](hsetnx.md) | Write a field only if it does not exist. |
| [HPERSIST](hpersist.md) | Remove field expiration. |
| [HTTL](httl.md) | Read remaining field lifetimes in seconds. |
| [HPTTL](hpttl.md) | Read remaining field lifetimes in milliseconds. |
| [HRANDFIELD](hrandfield.md) | Read random hash fields, optionally with their values. |
| [HSCAN](hscan.md) | Read one cursor batch of hash fields and values. |
| [HSTRLEN](hstrlen.md) | Read the byte length of a field value. |
| [HVALS](hvals.md) | Read all hash values. |
