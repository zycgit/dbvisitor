---
id: create-database
slug: /features/milvus/sql/databases
sidebar_position: 1
title: CREATE DATABASE
---

:::info[Note]
SDK methods: `createDatabase`.
:::

## Create Database

```text
CREATE DATABASE [IF NOT EXISTS] db_name;
CREATE DATABASE [IF NOT EXISTS] db_name WITH ("key" = "value", ...);
```

WITH values support `?` binding and are passed as strings to the native CreateDatabaseReq. IF NOT EXISTS leaves an existing database's properties unchanged; use ALTER DATABASE to modify them.

<span id="database" />
