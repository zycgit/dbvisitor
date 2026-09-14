---
id: documents
sidebar_position: 1
title: Document Writes and Deletes
---

:::info[Note]
REST endpoints: `_doc`, `_create`, `_update` and `_bulk`.
:::

## Insert and Replace

```text
POST /user_info/_doc {"name":"mali"}
PUT /user_info/_doc/1001 {"name":"mali"}
POST /user_info/_create/1002 {"name":"alice"}
```

POST without an ID generates `_id`; PUT with an ID replaces the existing document; `_create` fails if the ID exists. See [Key Generation](../dbvisitor/generated-keys.mdx) for key assignment.

## Update Fields

```text
POST /user_info/_update/1001?refresh=true {"doc":{"name":"new name"}}
```

Only fields in `doc` are changed. Use PUT, not `_update`, to replace the whole document.

## Delete

```text
DELETE /user_info/_doc/1001?refresh=true
```

## Bulk Writes

`_bulk` accepts a JSON array of alternating actions and documents. The driver converts it to Elasticsearch NDJSON:

```text
POST /user_info/_bulk?refresh=true [
  {"index":{"_id":"1001"}}, {"name":"mali"},
  {"index":{"_id":"1002"}}, {"name":"alice"}
]
```

Elasticsearch 6 actions also require `"_type":"_doc"`. Delete actions have no following document; update actions are followed by `{"doc":{...}}`.

This is one native command, not JDBC `addBatch()`. If any action fails, a `BatchUpdateException` exposes per-action counts through `getUpdateCounts()` and details in its exception chain. Successful actions are not rolled back.

See [Write Results](results.md) for counts and read-after-write visibility.
