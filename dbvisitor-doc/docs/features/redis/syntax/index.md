---
id: index
slug: /features/redis/commands
sidebar_position: 0
title: 命令语法
---

- 更新数(值)：使用 executeUpdate / getUpdateCount 读取命令对应的数量或状态，不统一等同于关系型影响行数。
- 结果集：使用 executeQuery / getResultSet 获取结果集。

返回形式是适配器的 JDBC 映射，不是读写分类：例如 INCR、LPOP 会修改数据并返回结果集。下表列出适配器实现的命令及返回形式；命令能否执行还受 Redis 版本和单机／Cluster 模式限制。


<span id="hash" />

- [Hash 命令集](./hash.md)

<span id="keys" />

- [Keys 命令集](./keys.md)

<span id="list" />

- [List 命令集](./list.md)

<span id="server" />

- [Server 命令集](./server.md)

<span id="set" />

- [Set 命令集](./set.md)

<span id="storeset" />

- [Sorted Set 命令集](./sorted-set.md)

<span id="string" />

- [String 命令集](./string.md)
