---
id: databases
sidebar_position: 1
title: SHOW DATABASES / DATABASE
---

:::info[Note]
SDK methods: `listDatabases`, `describeDatabase`.
:::

## Show Databases

```sql
SHOW DATABASES;
SHOW DATABASE db_name;
```

SHOW DATABASES returns database names. SHOW DATABASE returns one row with `DATABASE` and `PROPERTIES`. PROPERTIES is JSON object text containing the properties returned by the server; read it using `ResultSet.getString("PROPERTIES")` and parse it as needed. Missing databases or permission errors produce SQLException, not fabricated empty properties. Allowed property names, values and dynamic-application rules are determined by the Milvus server.
