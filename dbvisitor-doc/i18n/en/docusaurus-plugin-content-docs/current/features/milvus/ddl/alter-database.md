---
id: alter-database
sidebar_position: 2
title: ALTER DATABASE
---

:::info[Note]
SDK methods: `alterDatabaseProperties`, `dropDatabaseProperties`.
:::

## Alter Database Properties

```text
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
ALTER DATABASE db_name DROP PROPERTIES ("key", ...);
```

SET values can be bound with `?`; property names cannot. DROP removes only the listed properties, not the database.
