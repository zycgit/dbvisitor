---
id: indexes
sidebar_position: 3
title: 索引管理
---


| 命令 | 描述 | 示例 |
|---|---|---|
| `PUT /index` | 创建索引 | `PUT /new_index` |
| `DELETE /index` | 删除索引 | `DELETE /new_index` |
| `POST .../_open` | 打开索引 | `POST /my_index/_open` |
| `POST .../_close` | 关闭索引 | `POST /my_index/_close` |
| `PUT .../_mapping` | 设置 Mapping | `PUT /my_index/_mapping { "properties": ... }` |
| `PUT .../_settings` | 设置 Settings | `PUT /my_index/_settings { "index": ... }` |
| `POST /_aliases` | 别名管理 | `POST /_aliases { "actions": ... }` |
| `POST /_reindex` | 重建索引 | `POST /_reindex { "source": ..., "dest": ... }` |
| `POST .../_refresh` | 刷新索引 | `POST /my_index/_refresh` |
