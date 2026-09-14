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
The result is the modified count plus newly upserted documents, not the matched count. Setting an existing value returns 0; an upsert that inserts a document returns 1.
:::

The legacy `update(filter, update, options)` form updates one document by default; `multi: true` updates all matches.
