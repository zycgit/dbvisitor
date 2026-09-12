---
id: requests
sidebar_position: 5
title: Supported Commands
---

The command syntax follows Elasticsearch REST endpoints.

- Query
  - `GET/POST /{index}/_search` ([Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html))
  - `GET/POST /{index}/_count` ([Count API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-count.html))
  - `GET/POST /{index}/_msearch` ([Multi search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html))
  - `GET/POST /{index}/_mget` ([Multi get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html))
  - `GET/POST /{index}/_explain/{id}` ([Explain API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-explain.html))
  - `GET/POST /{index}/_source/{id}` ([Get source API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get-source.html))

- Document operations
  - `PUT/POST /{index}/_doc/{id}` ([Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html))
  - `PUT/POST /{index}/_create/{id}` ([Create op](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html#docs-index-api-op_type))
  - `POST /{index}/_update/{id}` ([Update API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html))
  - `POST /{index}/_update_by_query` ([Update by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html))
  - `DELETE /{index}/_doc/{id}` ([Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html))
  - `POST /{index}/_delete_by_query` ([Delete by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html))

- Index operations
  - `GET /{index}/_mapping` ([Get mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html))
  - `PUT/POST /{index}/_mapping` ([Put mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html))
  - `GET /{index}/_settings` ([Get settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-settings.html))
  - `PUT /{index}/_settings` ([Update settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html))
  - `GET/POST /_aliases` ([Aliases API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html))
  - `POST /{index}/_open` / `POST /{index}/_close` ([Open/Close index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html))
  - `GET/POST /{index}/_refresh` ([Refresh API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-refresh.html))
  - `POST /_reindex` ([Reindex API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html))

- `_cat` APIs
  - `GET /_cat/indices` ([cat indices](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-indices.html))
  - `GET /_cat/nodes` ([cat nodes](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-nodes.html))
  - `GET /_cat/health` ([cat health](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-health.html))

- Other
  - `HEAD /{path}` (returns `STATUS` column; see [REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html))
  - Generic REST: `GET/POST/PUT/DELETE /{path}` ([REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html))

The `_msearch` body is a JSON array with alternating request headers and query bodies; the driver converts it to NDJSON. Each subquery returns a JDBC result set. Read them using `execute()` / `getMoreResults()`. This is distinct from JDBC Batch.
