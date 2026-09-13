---
id: collections
sidebar_position: 1
title: 集合与视图
---

使用数据库方法创建集合或视图，使用集合方法重命名或删除。

```text
db.createCollection('user_info')
db.createView('active_users', 'user_info', [{$match: {enabled: true}}])
db.user_info.renameCollection('users')
db.users.stats()
db.users.drop()
```

创建视图使用 `createView`，不要在 `createCollection` 中配置 `viewOn`。删除集合会删除其文档和索引。
