---
id: commands
sidebar_position: 3
title: 命令参考
description: jdbc-elastic 命令示例、完整 REST 方法范围、返回约定和 Hint。
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

## 支持的指令
命令语法参考 Elasticsearch 官方文档。

- 查询
  - `GET/POST /{index}/_search`（[Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html)）
  - `GET/POST /{index}/_count`（[Count API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-count.html)）
  - `GET/POST /{index}/_msearch`（[Multi search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html)）
  - `GET/POST /{index}/_mget`（[Multi get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html)）
  - `GET/POST /{index}/_explain/{id}`（[Explain API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-explain.html)）
  - `GET/POST /{index}/_source/{id}`（[Get source API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get-source.html)）

- 文档操作
  - `PUT/POST /{index}/_doc/{id}`（[Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html)）
  - `PUT/POST /{index}/_create/{id}`（[Create op](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html#docs-index-api-op_type)）
  - `POST /{index}/_update/{id}`（[Update API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html)）
  - `POST /{index}/_update_by_query`（[Update by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html)）
  - `DELETE /{index}/_doc/{id}`（[Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html)）
  - `POST /{index}/_delete_by_query`（[Delete by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html)）

- 索引操作
  - `GET /{index}/_mapping`（[Get mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html)）
  - `PUT/POST /{index}/_mapping`（[Put mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html)）
  - `GET /{index}/_settings`（[Get settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-settings.html)）
  - `PUT /{index}/_settings`（[Update settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html)）
  - `GET/POST /_aliases`（[Aliases API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html)）
  - `POST /{index}/_open` / `POST /{index}/_close`（[Open/Close index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html)）
  - `GET/POST /{index}/_refresh`（[Refresh API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-refresh.html)）
  - `POST /_reindex`（[Reindex API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html)）

- `_cat` 查询
  - `GET /_cat/indices`（[cat indices](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-indices.html)）
  - `GET /_cat/nodes`（[cat nodes](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-nodes.html)）
  - `GET /_cat/health`（[cat health](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-health.html)）

- 其他
  - `HEAD /{path}`（返回 `STATUS` 列，参考 [REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)）
  - 通用 REST：`GET/POST/PUT/DELETE /{path}`（[REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)）

## Hint 支持
Hint 必须位于命令开头，格式为 `/*+ name=value */`。

| Hint | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖搜索请求的 `size`。 | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | 覆盖搜索请求的 `from`。 | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | 将 `/_search` 转换为 `/_count`。 | `/*+ overwrite_find_as_count */ POST /idx/_search` |

## 限制与注意事项
- `_cat` 查询会自动追加 `format=json` 参数（若手动指定则必须为 json）。
- 仅支持 REST 风格命令语法，不支持 Elasticsearch SQL。
