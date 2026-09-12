---
id: search
sidebar_position: 1
title: 查询操作
---


| 命令 | 描述 | 示例 |
|---|---|---|
| `GET .../_search` | 执行搜索查询 | `GET /my_index/_search { "query": { "match_all": {} } }` |
| `POST .../_search` | 执行搜索查询 | `POST /my_index/_search { "query": { "term": { "user": "kimchy" } } }` |
| `GET .../_count` | 统计文档数量 | `GET /my_index/_count` |
| `GET .../_msearch` | 批量搜索 | `POST /my_index/_msearch [{}, {"query":{"match_all":{}}}]` |
| `GET .../_mget` | 批量获取文档 | `GET /my_index/_mget {"ids":["1","2"]}` |
| `GET .../_explain` | 获取解释信息 | `GET /my_index/_explain/1 {"query":{"match_all":{}}}` |
| `GET .../_source` | 获取文档源数据 | `GET /my_index/_source/1` |
