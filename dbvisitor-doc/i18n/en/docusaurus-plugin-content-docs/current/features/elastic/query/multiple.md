---
id: multiple
sidebar_position: 2
title: Multi-search and Multi-get
---

:::info[Note]
REST endpoints: `_msearch` and `_mget`.
:::

## Multi-search

```text
POST /user_info/_msearch [{},{"query":{"term":{"uid":"1001"}}},{},{"query":{"term":{"uid":"1002"}}}]
```

Use a JSON array alternating request headers and query bodies; the driver converts it to NDJSON. Each search produces a JDBC result set. Read them with `execute()` and `getMoreResults()`; this is not JDBC batch.

## Multi-get

```text
GET /user_info/_mget {"ids":["1001","1002"]}
```

These IDs are document `_id` values, not business fields inside `_source`.
