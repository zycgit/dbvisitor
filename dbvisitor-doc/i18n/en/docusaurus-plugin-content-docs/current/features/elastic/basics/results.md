---
id: results
sidebar_position: 3
title: Command Results
---

| Request | Return format |
| --- | --- |
| `_search`, `_mget` | Document result sets; see [Reading Results](../dbvisitor/results.mdx). |
| `_count` | Count result set. |
| `_msearch` | One result set per search. |
| Document writes | Update count and optional generated keys. |
| Generic REST | Top-level fields for a JSON object, or rows for array elements. |
| HEAD | `STATUS` column. |

Do not choose a return type solely from GET/POST. If uncertain, use `execute()` and inspect the result set or update count.

Search results do not retain `_scroll_id`, sort, highlight, fields, `_score` or top-level aggregations. Use the official client when these are required.
