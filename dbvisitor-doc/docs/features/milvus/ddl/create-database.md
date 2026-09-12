---
id: create-database
slug: /features/milvus/sql/databases
sidebar_position: 1
title: CREATE DATABASE
---

:::info[说明]
对应 SDK 方法：`createDatabase`。
:::

## 创建数据库

```text
CREATE DATABASE [IF NOT EXISTS] db_name;
CREATE DATABASE [IF NOT EXISTS] db_name WITH ("key" = "value", ...);
```

WITH 的值可用 `?` 绑定，属性作为字符串传给原生 CreateDatabaseReq。IF NOT EXISTS 遇到已有数据库不修改其属性，修改时使用 ALTER DATABASE。

<span id="database" />
