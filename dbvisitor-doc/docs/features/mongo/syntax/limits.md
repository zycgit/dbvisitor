---
id: limits
sidebar_position: 7
title: 限制与注意事项
---

- `db.createCollection(...)` 不支持 `viewOn` 选项。
- `createIndex(...)` 必须提供 `name` 选项，否则会报错。
- `runCommand(...)` 的第一个参数必须是 `Document` 对象。
- `find` 链式调用仅支持 `limit`、`skip`、`sort`、`hint`，其他方法（例如 `explain`）会被拒绝。
- 未选择数据库（URL 路径或 `use <db>`）时使用 `db` 会抛出 “No database selected”。
- `connectTimeout` 已声明但未应用到 MongoClient 设置中。

`runCommand({...})` 允许原生命令，不代表驱动解析完整 mongosh JavaScript。
