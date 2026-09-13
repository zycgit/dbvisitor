---
id: update
sidebar_position: 2
title: Update and Replace
---

:::info[Note]
SDK methods: `updateOne`, `updateMany` and `replaceOne`.
:::

```text
test.user_info.updateOne({uid: ?}, {$inc: {loginCount: ?}})
test.user_info.updateMany({status: ?}, {$set: {enabled: false}})
test.user_info.replaceOne({uid: ?}, {uid: ?, name: ?})
```

`updateOne` changes one matching document; `updateMany` changes all matches. The database executes `$set` and `$inc`. `replaceOne` replaces the document, removing omitted ordinary fields.

:::info[Update Count]
The result is the modified count, not the matched count. Setting an existing value can return 0. An upsert insertion cannot be inferred from this count either.
:::

The legacy `update(filter, update, options)` form updates one document by default; `multi: true` updates all matches.
