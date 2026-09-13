---
id: databases
sidebar_position: 2
title: Database Management
---

Select a database before reading collection information or performing administration.

```text
use test;
db.getCollectionNames();
db.getCollectionInfos();
db.stats();
db.serverStatus();
db.version();
```

`db.dropDatabase()` deletes the current database and its collections. See [Collections and Views](collections.md) for creation, and [Database Commands](run-command.md) for generic requests.
