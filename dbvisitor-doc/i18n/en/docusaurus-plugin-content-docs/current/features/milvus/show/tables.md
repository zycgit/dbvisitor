---
id: tables
sidebar_position: 2
title: SHOW TABLES / TABLE / CREATE TABLE
---

:::info[Note]
SDK methods: `listCollectionsV2`, `describeCollection`.
:::

## Show Tables

```sql
SHOW TABLES;                        -- List all tables
SHOW TABLE table_name;              -- Inspect fields
SHOW CREATE TABLE table_name;       -- View detailed create table statement
```

SHOW TABLES lists collection names. SHOW TABLE returns one row per field, including its type, dimension, primary-key and AutoID flags, nullable flag, ARRAY element type/capacity, VARCHAR length, and partition/clustering-key flags. SHOW CREATE TABLE returns CREATE SCRIPT (VARCHAR), a CREATE TABLE statement; it does not include indexes or permissions.
