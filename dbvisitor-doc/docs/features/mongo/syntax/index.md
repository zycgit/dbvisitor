---
id: index
slug: /features/mongo/commands
sidebar_position: 0
title: 命令语法
---

`jdbc-mongo` 通过解析原始 Command 命令将其转换为底层的 API 调用。以下是支持的命令列表。

:::tip[命令增强]
对于原始的 MongoDB 命令中，`db.` 前缀的增强
- 您可以在原始命令中省略 `db.` 前缀，直接使用集合名称，例如 `coll1.find()` 此时表示查询当前数据库中的 coll1 集合。
- 您可以使用 `<数据库名>.<集合名>.` 指定操作的具体集合，例如 `myDb.coll1.find()` 此时表示查询 myDb 数据库中的 coll1 集合。
- 您可以使用 `use 数据库名` 切换当前数据库。
:::


<span id="collection" />

- [集合操作](./collections.md)

<span id="database" />

- [数据库管理](./databases.md)

<span id="index" />

- [索引管理](./indexes.md)

<span id="user" />

- [用户管理](./users.md)

<span id="other" />

- [其他命令](./other.md)

- [Hint 支持](./hints.md)

- [限制与注意事项](./limits.md)

<span id="native-operations" />

- [原生操作与返回语义](./operations.md)
