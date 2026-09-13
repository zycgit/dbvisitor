---
id: insert
sidebar_position: 1
title: Insert Documents
---

:::info[Note]
SDK methods: `insertOne` and `insertMany`.
:::

```text
test.user_info.insertOne({uid: ?, name: ?})
test.user_info.insertMany([{uid: '1001', name: 'mali'}, {uid: '1002', name: 'alice'}])
```

`insert` accepts a document or a document array. An ID is generated when `_id` is omitted; see [Key Generation](../dbvisitor/generated-keys.mdx). `insertMany` is a native multi-document write, not JDBC batch.
