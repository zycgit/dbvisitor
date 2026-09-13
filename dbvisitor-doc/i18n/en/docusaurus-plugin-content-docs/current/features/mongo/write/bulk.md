---
id: bulk
sidebar_position: 4
title: Bulk Write
---

:::info[Note]
SDK method: `bulkWrite`.
:::

```text
test.user_info.bulkWrite([
  {insertOne: {document: {uid: '1001', name: 'mali'}}},
  {updateOne: {filter: {uid: '1002'}, update: {$set: {name: 'alice'}}}},
  {deleteOne: {filter: {uid: '1003'}}}
])
```

One command combines multiple write operations. The returned count sums inserts, modifications and deletions, excluding the separate upsert count. This is not JDBC batch and does not guarantee all-document success or rollback.
