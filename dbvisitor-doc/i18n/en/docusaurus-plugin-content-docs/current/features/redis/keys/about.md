---
id: about
slug: /features/redis/commands/keys
sidebar_position: 0
hide_table_of_contents: true
title: Key Management
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [COPY](copy.md) | Copy a key, optionally to another logical database. |
| [DEL](del.md) | Delete one or more keys. |
| [UNLINK](unlink.md) | Remove keys and reclaim their memory asynchronously. |
| [DUMP](dump.md) | Read the Redis-serialized value of a key. |
| [EXISTS](exists.md) | Count existing keys among the supplied names. |
| [EXPIRE](expire.md) | Set a key lifetime in seconds. |
| [EXPIREAT](expireat.md) | Set key expiration using a Unix timestamp in seconds. |
| [EXPIRETIME](expiretime.md) | Read the key expiration timestamp in seconds. |
| [PEXPIRE](pexpire.md) | Set a key lifetime in milliseconds. |
| [PEXPIREAT](pexpireat.md) | Set key expiration using a Unix timestamp in milliseconds. |
| [PEXPIRETIME](pexpiretime.md) | Read the key expiration timestamp in milliseconds. |
| [KEYS](keys.md) | Find matching keys; the driver iterates with SCAN. |
| [OBJECT ENCODING](object-encoding.md) | Read the internal encoding name of a value. |
| [OBJECT FREQ](object-freq.md) | Read the access-frequency counter maintained by an LFU policy. |
| [OBJECT IDLETIME](object-idletime.md) | Read the time in seconds since a key was last accessed. |
| [OBJECT REFCOUNT](object-refcount.md) | Read the value object's reference count. |
| [PERSIST](persist.md) | Remove a key's expiration. |
| [TTL](ttl.md) | Read the remaining key lifetime in seconds. |
| [PTTL](pttl.md) | Read the remaining key lifetime in milliseconds. |
| [RANDOMKEY](randomkey.md) | Read a random key name from the current logical database. |
| [RENAME](rename.md) | Rename a key, overwriting an existing destination. |
| [RENAMENX](renamenx.md) | Rename a key only if the destination does not exist. |
| [SCAN](scan.md) | Read one batch of key names and its continuation cursor. |
| [TOUCH](touch.md) | Update the access information of keys. |
| [TYPE](type.md) | Read the data-structure name of a key. |
