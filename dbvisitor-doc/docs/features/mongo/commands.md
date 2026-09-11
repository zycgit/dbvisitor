---
id: commands
sidebar_position: 3
title: 命令参考
description: jdbc-mongo 命令、链式查询、Hint 与限制。
---

`jdbc-mongo` 通过解析原始 Command 命令将其转换为底层的 API 调用。以下是支持的命令列表。

:::tip[命令增强]
对于原始的 MongoDB 命令中，`db.` 前缀的增强
- 您可以在原始命令中省略 `db.` 前缀，直接使用集合名称，例如 `coll1.find()` 此时表示查询当前数据库中的 coll1 集合。
- 您可以使用 `<数据库名>.<集合名>.` 指定操作的具体集合，例如 `myDb.coll1.find()` 此时表示查询 myDb 数据库中的 coll1 集合。
- 您可以使用 `use 数据库名` 切换当前数据库。
:::

## 集合操作 (Collection Operations) {#collection}

| 命令 | 描述 | 官方文档 |
|---|---|---|
| `find` | 查询文档，支持 `limit`, `skip`, `sort`, `hint` | [find](https://www.mongodb.com/docs/manual/reference/command/find/) |
| `findOne` | 查询单个文档 | [findOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.findOne/) |
| `insert` | 插入文档 | [insert](https://www.mongodb.com/docs/manual/reference/command/insert/) |
| `insertOne` | 插入单个文档 | [insertOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.insertOne/) |
| `insertMany` | 插入多个文档 | [insertMany](https://www.mongodb.com/docs/manual/reference/method/db.collection.insertMany/) |
| `update` | 更新文档 | [update](https://www.mongodb.com/docs/manual/reference/command/update/) |
| `updateOne` | 更新单个文档 | [updateOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.updateOne/) |
| `updateMany` | 更新多个文档 | [updateMany](https://www.mongodb.com/docs/manual/reference/method/db.collection.updateMany/) |
| `replaceOne` | 替换单个文档 | [replaceOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.replaceOne/) |
| `remove` | 删除文档 | [delete](https://www.mongodb.com/docs/manual/reference/command/delete/) |
| `deleteOne` | 删除单个文档 | [deleteOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.deleteOne/) |
| `deleteMany` | 删除多个文档 | [deleteMany](https://www.mongodb.com/docs/manual/reference/method/db.collection.deleteMany/) |
| `count` | 统计文档数量 | [count](https://www.mongodb.com/docs/manual/reference/command/count/) |
| `distinct` | 获取唯一值 | [distinct](https://www.mongodb.com/docs/manual/reference/command/distinct/) |
| `aggregate` | 聚合操作 | [aggregate](https://www.mongodb.com/docs/manual/reference/command/aggregate/) |
| `bulkWrite` | 批量写入操作 | [bulkWrite](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/) |
| `renameCollection` | 重命名集合 | [renameCollection](https://www.mongodb.com/docs/manual/reference/command/renameCollection/) |
| `drop` | 删除集合 | [drop](https://www.mongodb.com/docs/manual/reference/command/drop/) |
| `stats` | 获取集合统计信息 | [collStats](https://www.mongodb.com/docs/manual/reference/command/collStats/) |

## 数据库管理 (Database Management) {#database}

| 命令 | 描述 | 官方文档 |
|---|---|---|
| `createCollection` | 创建集合 | [create](https://www.mongodb.com/docs/manual/reference/command/create/) |
| `createView` | 创建视图 | [createView](https://www.mongodb.com/docs/manual/reference/command/create/) |
| `dropDatabase` | 删除当前数据库 | [dropDatabase](https://www.mongodb.com/docs/manual/reference/command/dropDatabase/) |
| `getCollectionNames` | 获取集合名称列表 | [listCollections](https://www.mongodb.com/docs/manual/reference/command/listCollections/) |
| `getCollectionInfos` | 获取集合信息 | [listCollections](https://www.mongodb.com/docs/manual/reference/command/listCollections/) |
| `runCommand` | 运行任意数据库命令 | [runCommand](https://www.mongodb.com/docs/manual/reference/command/runCommand/) |
| `serverStatus` | 获取服务器状态 | [serverStatus](https://www.mongodb.com/docs/manual/reference/command/serverStatus/) |
| `stats` | 获取数据库统计信息 | [dbStats](https://www.mongodb.com/docs/manual/reference/command/dbStats/) |
| `version` | 获取服务器版本 | [buildInfo](https://www.mongodb.com/docs/manual/reference/command/buildInfo/) |

## 索引管理 (Index Management) {#index}

| 命令 | 描述 | 官方文档 |
|---|---|---|
| `createIndex` | 创建索引 | [createIndexes](https://www.mongodb.com/docs/manual/reference/command/createIndexes/) |
| `dropIndex` | 删除索引 | [dropIndexes](https://www.mongodb.com/docs/manual/reference/command/dropIndexes/) |
| `getIndexes` | 获取索引列表 | [listIndexes](https://www.mongodb.com/docs/manual/reference/command/listIndexes/) |

## 用户管理 (User Management) {#user}

| 命令 | 描述 | 官方文档 |
|---|---|---|
| `createUser` | 创建用户 | [createUser](https://www.mongodb.com/docs/manual/reference/command/createUser/) |
| `dropUser` | 删除用户 | [dropUser](https://www.mongodb.com/docs/manual/reference/command/dropUser/) |
| `updateUser` | 更新用户 | [updateUser](https://www.mongodb.com/docs/manual/reference/command/updateUser/) |
| `changeUserPassword` | 修改用户密码 | [updateUser](https://www.mongodb.com/docs/manual/reference/command/updateUser/) |
| `grantRolesToUser` | 授予用户角色 | [grantRolesToUser](https://www.mongodb.com/docs/manual/reference/command/grantRolesToUser/) |
| `revokeRolesFromUser` | 撤销用户角色 | [revokeRolesFromUser](https://www.mongodb.com/docs/manual/reference/command/revokeRolesFromUser/) |

## 其他命令 {#other}

- `use <database>`: 切换当前数据库。
- `show dbs` / `show databases`: 显示所有数据库。
- `show collections`: 显示当前数据库的所有集合。
- `show tables`: 同 `show collections`。
- `show users`: 显示用户。
- `show roles`: 显示角色。
- `show profile`: 查询性能分析记录。

## Hint 支持
Hint 必须位于命令开头，格式为 `/*+ name=value */`，支持多个 Hint 块。

支持的 Hint：

| Hint | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖 `find` / `findOne` 的 `limit`。 | `/*+ overwrite_find_limit=10 */ db.mycol.find({})` |
| `overwrite_find_skip` | 覆盖 `find` / `findOne` 的 `skip`。 | `/*+ overwrite_find_skip=20 */ db.mycol.find({})` |
| `overwrite_find_as_count` | 将 `find` 转换为 `countDocuments` 结果。 | `/*+ overwrite_find_as_count */ db.mycol.find({})` |

## 限制与注意事项
- `db.createCollection(...)` 不支持 `viewOn` 选项。
- `createIndex(...)` 必须提供 `name` 选项，否则会报错。
- `runCommand(...)` 的第一个参数必须是 `Document` 对象。
- `find` 链式调用仅支持 `limit`、`skip`、`sort`、`hint`，其他方法（例如 `explain`）会被拒绝。
- 未选择数据库（URL 路径或 `use <db>`）时使用 `db` 会抛出 “No database selected”。
- `connectTimeout` 已声明但未应用到 MongoClient 设置中。

`runCommand({...})` 允许原生命令，不代表驱动解析完整 mongosh JavaScript。
