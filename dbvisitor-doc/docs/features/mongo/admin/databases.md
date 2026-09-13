---
id: databases
sidebar_position: 2
title: 数据库管理
---

先选择数据库，再读取集合信息或执行管理操作。

```text
use test;
db.getCollectionNames();
db.getCollectionInfos();
db.stats();
db.serverStatus();
db.version();
```

删除当前数据库使用 `db.dropDatabase()`，会删除其中的集合。创建集合和视图见[集合与视图](collections.md)，通用数据库请求见[数据库原生命令](run-command.md)。
