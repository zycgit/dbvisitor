---
id: results
sidebar_position: 8
title: Results and Native Capability Boundaries
---


| Request | JDBC result and limitation |
| --- | --- |
| `_search` | Reads hits.hits; `_ID` is the hit ID, and `_DOC` contains only `_source` JSON, not the full response. |
| `_count` | Returns a separate count result, not a total inferred from the search page. |
| `_msearch` | One result set per subquery, not JDBC batch. |
| Generic REST | Maps object fields to columns and array elements to rows, without implementing every endpoint-specific response protocol. |
| Document insertion | Returns an update count and optional generated keys. Indexing an existing document ID replaces it rather than raising a relational unique-key conflict. |

The search path does not expose top-level aggregations, hits.total or `_scroll_id`, or preserve hit `_score`, sort, highlight and fields metadata. Consequently, an aggregation search with `size: 0` cannot return its buckets through JDBC; `_DOC` cannot recover that information. Use the official client when these fields or scroll/search_after cursors are required.

Ordinary full-text searches and exact keyword filters remain available through Query DSL. Elasticsearch manages analyzers, Mapping, index aliases and templates; dbVisitor entity mapping does not create them. Generic REST writes may return ResultSet, so do not choose executeUpdate based only on the HTTP method. Use execute when the return type is uncertain.

### Write Visibility and Counts

A write acknowledgment does not imply immediate search visibility. Document index/update/delete APIs accept their respective refresh settings; do not carry their `refresh=wait_for` option over to `_update_by_query` or `_delete_by_query`. Connection property indexRefresh adds `refresh=true` to relevant writes, with explicit parameters taking precedence. This is not a transaction commit or multi-request atomicity guarantee. See [refresh semantics](https://www.elastic.co/docs/reference/elasticsearch/rest-apis/refresh-parameter).

For UPDATE/DELETE request families, the adapter returns `Statement.SUCCESS_NO_INFO` when no explicit refresh parameter is present and indexRefresh is disabled; this is not a negative affected-row count. When count extraction is enabled, document updates use result and by-query operations use updated/deleted. Even explicit refresh=false enables count extraction without implying search visibility.

By-query updates/deletes can partially succeed or encounter version conflicts. The returned count is not a complete task report. Do not enable asynchronous task options expecting automatic task-ID retrieval, polling or rollback through this result mapping.
