---
id: flush
sidebar_position: 7
title: SHOW FLUSH ALL
---

:::info[Note]
SDK methods: `getFlushAllState`.
:::

```sql
SHOW FLUSH ALL ? IN DATABASE archive;
```

`SHOW FLUSH ALL timestamp` calls `getFlushAllState` once and returns one row with BIGINT `FLUSH_ALL_TS` and BOOLEAN `FLUSHED`. Supply a non-negative BIGINT literal or `setLong()` binding. Preserve the native integer token rather than converting it to a Java date or floating-point value. Observation neither triggers flush nor waits automatically. False means the native completion condition has not been met; absent SDK state remains NULL rather than fabricated success.

Use the same database scope as the FLUSH ALL request.
