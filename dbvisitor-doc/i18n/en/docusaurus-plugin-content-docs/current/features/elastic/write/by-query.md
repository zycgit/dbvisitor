---
id: by-query
sidebar_position: 2
title: Update and Delete by Query
---

:::info[Note]
REST endpoints: `_update_by_query` and `_delete_by_query`.
:::

```text
POST /user_info/_update_by_query?refresh=true {"query":{"term":{"uid": ?}},"script":{"source":"ctx._source.name = params.name","params":{"name": ?}}}
POST /user_info/_delete_by_query?refresh=true {"query":{"term":{"uid": ?}}}
```

Bind uid followed by the new name for the update, and uid for the delete. All matching documents are processed; matching business fields do not imply identical document `_id` values.

:::caution[Partial Success]
Writes by query may encounter version conflicts or partially succeed. Completed changes are not rolled back automatically. The returned count is not a full task report; asynchronous task options do not make the driver wait for task completion.
:::
