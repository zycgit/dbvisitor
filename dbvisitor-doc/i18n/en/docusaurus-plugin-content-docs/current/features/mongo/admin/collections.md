---
id: collections
sidebar_position: 1
title: Collections and Views
---

Use database methods to create collections or views, and collection methods to rename or drop them.

```text
db.createCollection('user_info')
db.createView('active_users', 'user_info', [{$match: {enabled: true}}])
db.user_info.renameCollection('users')
db.users.stats()
db.users.drop()
```

Use `createView` for a view; do not pass `viewOn` to `createCollection`. Dropping a collection removes its documents and indexes.
