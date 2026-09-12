---
id: requests
sidebar_position: 5
title: 支持的指令
---

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

`_msearch` 请求体使用 JSON 数组，按请求头、查询体交替排列；驱动将其转换为 NDJSON。每个子查询返回一个 JDBC 结果集，应通过 `execute()` / `getMoreResults()` 逐个读取。它与 JDBC Batch 不同。
