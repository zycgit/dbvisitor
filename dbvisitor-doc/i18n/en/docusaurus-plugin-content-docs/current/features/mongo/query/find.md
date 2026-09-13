---
id: find
sidebar_position: 1
title: Document Queries
---

:::info[Note]
SDK method: `find`; `findOne` reads a single document.
:::

```text
test.user_info.find({age: {$gte: ?}}, {name: 1, age: 1}).sort({_id: 1}).skip(0).limit(10)
test.user_info.findOne({uid: ?})
```

The first argument is the filter and the second is the projection. Supported chained methods are `sort`, `skip`, `limit` and `hint`, not arbitrary mongosh method chains.

## Combined Conditions

```text
test.user_info.find({$or: [{name: ?}, {uid: ?}]})
test.user_info.find({name: {$regex: ?}})
```

`$regex` accepts a regular expression, not a SQL LIKE pattern. Escape regex metacharacters when matching plain text.

Document fields are expanded by default; `_JSON` is also available. See [Reading Results](../dbvisitor/results.mdx) for entity and scalar access.
