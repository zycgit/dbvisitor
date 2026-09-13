---
id: search
sidebar_position: 1
title: Search and Count
---

:::info[Note]
REST endpoints: `_search` and `_count`.
:::

## Search

```text
POST /user_info/_search {"query":{"term":{"uid":"1001"}},"size":10}
```

Map `uid` as keyword. Results contain `_ID` and `_DOC`, with document fields expanded by default. Read a specific field by column name; see [Reading Results](../dbvisitor/results.mdx).

## Count

```text
POST /user_info/_count {"query":{"term":{"uid":"1001"}}}
```

Use `_count` for the total, not the number of rows in a search page. See [Pagination](../dbvisitor/pagination.mdx) for dbVisitor Page usage.

:::info[Result Scope]
Search returns document hits, not the full response containing `_score`, `highlight` or `aggregations`.
:::
