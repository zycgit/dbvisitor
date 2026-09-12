---
id: documents
sidebar_position: 2
title: 文档操作
---


| 命令 | 描述 | 示例 |
|---|---|---|
| `PUT .../_doc/...` | 创建或更新文档 | `PUT /my_index/_doc/1 { "user": "kimchy" }` |
| `POST .../_doc/...` | 创建文档 | `POST /my_index/_doc/ { "user": "kimchy" }` |
| `POST .../_create/...` | 创建文档（如果存在则失败） | `POST /my_index/_create/1 { "user": "kimchy" }` |
| `POST .../_update/...` | 更新文档 | `POST /my_index/_update/1 { "doc": { "age": 20 } }` |
| `POST .../_update_by_query` | 按查询更新 | `POST /my_index/_update_by_query { "script": ... }` |
| `DELETE ...` | 删除文档 | `DELETE /my_index/_doc/1` |
| `POST .../_delete_by_query` | 按查询删除 | `POST /my_index/_delete_by_query { "query": ... }` |
