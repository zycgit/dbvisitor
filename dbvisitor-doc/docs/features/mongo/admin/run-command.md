---
id: run-command
sidebar_position: 6
title: 数据库原生命令
---

`runCommand` 将文档参数交给 SDK `runCommand`，返回一次命令响应。

```text
db.runCommand({ping: 1})
```

也可通过 `?` 绑定 `org.bson.Document`。如果响应包含 cursor，驱动不会自动继续 getMore；文档查询使用 find，聚合使用 aggregate。这里不执行 JavaScript 程序。
