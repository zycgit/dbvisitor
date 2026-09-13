---
id: documents
sidebar_position: 1
title: Document Writes and Deletes
---

:::info[Note]
REST endpoints: `_doc`, `_create` and `_update`.
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

See [Write Results](results.md) for counts and read-after-write visibility.
