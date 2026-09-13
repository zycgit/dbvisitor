---
id: delete
sidebar_position: 3
title: Delete Documents
---

:::info[Note]
SDK methods: `deleteOne` and `deleteMany`.
:::

```text
test.user_info.deleteOne({uid: ?})
test.user_info.deleteMany({status: ?})
```

`deleteOne` removes one matching document; `deleteMany` removes all matches. The result is the deleted count. An empty filter matches every document in the collection.
