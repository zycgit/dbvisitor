---
id: commands
sidebar_position: 3
hide_table_of_contents: true
title: 支持的命令
description: jdbc-elastic 支持常用的 ElasticSearch REST API 命令，涵盖查询、文档操作、索引管理、集群信息等。
---

`jdbc-elastic` 通过解析原始 QueryDSL 命令，将其转换为底层的 REST 请求。支持的命令模式如下：

## 查询操作 (Search Operations)

| 命令 | 描述 | 示例 |
|---|---|---|
| `GET .../_search` | 执行搜索查询 | `GET /my_index/_search { "query": { "match_all": {} } }` |
| `POST .../_search` | 执行搜索查询 | `POST /my_index/_search { "query": { "term": { "user": "kimchy" } } }` |
| `GET .../_count` | 统计文档数量 | `GET /my_index/_count` |
| `GET .../_msearch` | 批量搜索 | `GET /_msearch` |
| `GET .../_mget` | 批量获取文档 | `GET /_mget` |
| `GET .../_explain` | 获取解释信息 | `GET /my_index/_explain/1` |
| `GET .../_source` | 获取文档源数据 | `GET /my_index/_source/1` |

## 文档操作 (Document Operations)

| 命令 | 描述 | 示例 |
|---|---|---|
| `PUT .../_doc/...` | 创建或更新文档 | `PUT /my_index/_doc/1 { "user": "kimchy" }` |
| `POST .../_doc/...` | 创建文档 | `POST /my_index/_doc/ { "user": "kimchy" }` |
| `POST .../_create/...` | 创建文档（如果存在则失败） | `POST /my_index/_create/1 { "user": "kimchy" }` |
| `POST .../_update/...` | 更新文档 | `POST /my_index/_update/1 { "doc": { "age": 20 } }` |
| `POST .../_update_by_query` | 按查询更新 | `POST /my_index/_update_by_query { "script": ... }` |
| `DELETE ...` | 删除文档 | `DELETE /my_index/_doc/1` |
| `POST .../_delete_by_query` | 按查询删除 | `POST /my_index/_delete_by_query { "query": ... }` |

## 索引管理 (Index Management)

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

## 集群信息 (Cluster Info)

| 命令 | 描述 | 示例 |
|---|---|---|
| `GET /_cat/...` | 获取集群信息 | `GET /_cat/nodes?v` |
| `GET /_cluster/...` | 获取集群状态 | `GET /_cluster/health` |

## 通用请求

`jdbc-elastic` 支持任意 `GET`, `POST`, `PUT`, `DELETE`, `HEAD` 请求。只要符合 ElasticSearch REST API 规范，都可以通过 JDBC 接口执行。

## Hint 支持

jdbc-elastic 支持通过 SQL Hint 方式来覆盖或增强查询行为。Hint 格式为 `/*+ hint_name=value */`，必须位于 SQL 语句的开头。

| Hint 名称 | 说明 | 示例 |
|---|---|---|
| `overwrite_find_limit` | 覆盖查询的 `size` 参数，用于分页或限制返回条数。 | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | 覆盖查询的 `from` 参数，用于分页跳过指定条数。 | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | 将查询转换为 Count 操作，忽略返回的文档内容，仅返回匹配数量。 | `/*+ overwrite_find_as_count */ POST /idx/_search` |
