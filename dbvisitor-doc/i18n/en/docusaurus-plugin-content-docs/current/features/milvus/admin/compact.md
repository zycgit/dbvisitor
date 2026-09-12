---
id: compact
sidebar_position: 7
title: COMPACT
---

:::info[Note]
SDK methods: `compact`.
:::

## Compaction tasks {#compaction}

```sql
COMPACT TABLE table_name;
COMPACT table_name WITH (is_clustering=false, is_l0=false, target_size=512);
```

`COMPACT` submits a native asynchronous compaction task and returns a one-row ResultSet with a BIGINT `COMPACTION_ID`. Use `executeQuery()` or `execute()`, not an update count or `getGeneratedKeys()`. The driver does not poll for completion, implicitly FLUSH, or treat compaction as commit. A submission failure without a known task ID does not prove the server never received the request; confirm task state before resubmitting.

Optional WITH settings map to the SDK: `is_clustering` and `is_l0` are booleans; `target_size` is a positive long integer in MB. Omitting them preserves SDK defaults. Support and effects of special compaction modes and target size depend on server version, collection configuration, and the SDK; the driver does not emulate them. Values accept `?`, bound using `setBoolean` / `setLong` respectively.
