---
id: document
sidebar_position: 3
title: Document Source and Query Explanation
---

:::info[Note]
REST endpoints: `_source` and `_explain`.
:::

## Read Document Source by ID

```text
GET /user_info/_source/1001
```

The ID is the document `_id`, not the business field uid.

## Explain a Match

```text
GET /user_info/_explain/1001 {"query":{"term":{"name":"mali"}}}
```

Explains whether a specific document matches the query; it is not a search-hit list.
