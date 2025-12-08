---
id: commands
sidebar_position: 3
title: 支持的命令
description: jdbc-mongo 支持常用的 MongoDB 命令，涵盖数据库管理、集合操作、索引管理、用户管理等。
---

`jdbc-mongo` 支持通过 SQL 风格的语法执行 MongoDB 命令。以下是支持的命令列表。

:::tip[命令增强]
对于原始的 MongoDB 命令中，`db.` 前缀的增强
- 您可以在原始命令中省略 `db.` 前缀，直接使用集合名称，例如 `coll1.find()` 此时表示查询当前数据库中的 coll1 集合。
- 您可以使用 `<数据库名>.<集合名>.` 指定操作的具体集合，例如 `myDb.coll1.find()` 此时表示查询 myDb 数据库中的 coll1 集合。
- 您可以使用 `use 数据库名` 切换当前数据库。
:::

## 集合操作 (Collection Operations) {#collection}

| 命令 | 描述 | 官方文档 |
|---|---|---|
| `find` | 查询文档，支持 `limit`, `skip`, `sort`, `explain`, `hint` | [find](https://www.mongodb.com/docs/manual/reference/command/find/) |
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
- `show dbs`: 显示所有数据库。
- `show collections`: 显示当前数据库的所有集合。
- `show tables`: 同 `show collections`。
