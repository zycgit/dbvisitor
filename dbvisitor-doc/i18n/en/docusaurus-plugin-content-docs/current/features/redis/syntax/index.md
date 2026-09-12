---
id: index
slug: /features/redis/commands
sidebar_position: 0
title: Command Syntax
---
- Update count: Use executeUpdate / getUpdateCount for the command's count or mapped status, not a uniform relational affected-row count.
- Result set: Use executeQuery / getResultSet to get the result set.

These are JDBC result mappings, not read/write classifications: INCR and LPOP modify data while returning rows. The table lists commands implemented by the adapter and their result forms; execution also depends on the Redis version and standalone/Cluster mode.


<span id="hash" />

- [Hash Command Set](./hash.md)

<span id="keys" />

- [Keys Command Set](./keys.md)

<span id="list" />

- [List Command Set](./list.md)

<span id="server" />

- [Server Command Set](./server.md)

<span id="set" />

- [Set Command Set](./set.md)

<span id="storeset" />

- [SortedSet Command Set](./sorted-set.md)

<span id="string" />

- [String Command Set](./string.md)
