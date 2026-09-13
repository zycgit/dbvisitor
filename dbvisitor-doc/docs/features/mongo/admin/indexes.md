---
id: indexes
sidebar_position: 3
title: 索引管理
---

创建索引必须提供 `name`，删除时使用该名称。

```text
db.user_info.createIndex({uid: 1}, {name: 'idx_uid', unique: true})
db.user_info.getIndexes()
db.user_info.dropIndex('idx_uid')
```

`unique: true` 由 MongoDB 建立唯一索引；实体的 `primary=true` 不会代替这一步。
