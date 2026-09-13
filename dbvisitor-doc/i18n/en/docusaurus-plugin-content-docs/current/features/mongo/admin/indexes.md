---
id: indexes
sidebar_position: 3
title: Index Management
---

Provide `name` when creating an index and use that name when dropping it.

```text
db.user_info.createIndex({uid: 1}, {name: 'idx_uid', unique: true})
db.user_info.getIndexes()
db.user_info.dropIndex('idx_uid')
```

`unique: true` creates a unique index in MongoDB. An entity's `primary=true` annotation does not replace this step.
