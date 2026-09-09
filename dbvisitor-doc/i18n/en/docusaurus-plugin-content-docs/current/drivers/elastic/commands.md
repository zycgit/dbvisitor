---
id: commands
sidebar_position: 3
title: Command Reference
description: jdbc-elastic command examples, REST method coverage, results and hints.
---

`jdbc-elastic` parses raw QueryDSL commands and converts them to underlying REST requests. The supported command patterns are as follows:

## Search Operations

| Command | Description | Example |
|---|---|---|
| `GET .../_search` | Execute search query | `GET /my_index/_search { "query": { "match_all": {} } }` |
| `POST .../_search` | Execute search query | `POST /my_index/_search { "query": { "term": { "user": "kimchy" } } }` |
| `GET .../_count` | Count documents | `GET /my_index/_count` |
| `GET .../_msearch` | Batch search | `GET /_msearch` |
| `GET .../_mget` | Batch get documents | `GET /_mget` |
| `GET .../_explain` | Get explanation info | `GET /my_index/_explain/1` |
| `GET .../_source` | Get document source data | `GET /my_index/_source/1` |

## Document Operations

| Command | Description | Example |
|---|---|---|
| `PUT .../_doc/...` | Create or update document | `PUT /my_index/_doc/1 { "user": "kimchy" }` |
| `POST .../_doc/...` | Create document | `POST /my_index/_doc/ { "user": "kimchy" }` |
| `POST .../_create/...` | Create document (fails if exists) | `POST /my_index/_create/1 { "user": "kimchy" }` |
| `POST .../_update/...` | Update document | `POST /my_index/_update/1 { "doc": { "age": 20 } }` |
| `POST .../_update_by_query` | Update by query | `POST /my_index/_update_by_query { "script": ... }` |
| `DELETE ...` | Delete document | `DELETE /my_index/_doc/1` |
| `POST .../_delete_by_query` | Delete by query | `POST /my_index/_delete_by_query { "query": ... }` |

## Index Management

| Command | Description | Example |
|---|---|---|
| `PUT /index` | Create index | `PUT /new_index` |
| `DELETE /index` | Delete index | `DELETE /new_index` |
| `POST .../_open` | Open index | `POST /my_index/_open` |
| `POST .../_close` | Close index | `POST /my_index/_close` |
| `PUT .../_mapping` | Set Mapping | `PUT /my_index/_mapping { "properties": ... }` |
| `PUT .../_settings` | Set Settings | `PUT /my_index/_settings { "index": ... }` |
| `POST /_aliases` | Alias management | `POST /_aliases { "actions": ... }` |
| `POST /_reindex` | Reindex | `POST /_reindex { "source": ..., "dest": ... }` |
| `POST .../_refresh` | Refresh index | `POST /my_index/_refresh` |

## Cluster Info

| Command | Description | Example |
|---|---|---|
| `GET /_cat/...` | Get cluster info | `GET /_cat/nodes?v` |
| `GET /_cluster/...` | Get cluster status | `GET /_cluster/health` |

## Supported Commands
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

## Hint Support
Hints must appear at the beginning of the command text. Format: `/*+ name=value */`.

| Hint | Description | Example |
| --- | --- | --- |
| `overwrite_find_limit` | Overrides the `size` in search requests. | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | Overrides the `from` in search requests. | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | Converts `/_search` to `/_count`. | `/*+ overwrite_find_as_count */ POST /idx/_search` |

## Limitations
- `_cat` queries automatically append `format=json` (if specified, it must be json).
- Only REST-style command grammar is supported; Elasticsearch SQL is not supported.
