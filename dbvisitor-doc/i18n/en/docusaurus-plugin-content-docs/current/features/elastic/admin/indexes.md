---
id: indexes
sidebar_position: 1
title: Indexes and Mappings
---

Manage indexes through REST requests; entity annotations do not create them automatically.

## Create Indexes and Fields

```text
PUT /user_info {"mappings":{"properties":{"uid":{"type":"keyword"},"name":{"type":"keyword"}}}}
GET /user_info/_mapping
PUT /user_info/_mapping {"properties":{"age":{"type":"integer"}}}
```

These are ES 7 typeless mappings. ES 6 requires its corresponding type paths and mapping structure.

## Settings, Aliases and Refresh

```text
GET /user_info/_settings
PUT /user_info/_settings {"index":{"number_of_replicas":1}}
POST /_aliases {"actions":[{"add":{"index":"user_info","alias":"users"}}]}
POST /user_info/_refresh
```

## Close, Open and Drop

```text
POST /user_info/_close
POST /user_info/_open
DELETE /user_info
```

Dropping an index deletes its documents. To reindex into another index:

```text
POST /_reindex {"source":{"index":"user_info"},"dest":{"index":"user_info_copy"}}
```
