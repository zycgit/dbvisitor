---
id: truncate-table
sidebar_position: 7
title: TRUNCATE TABLE
---

:::info[Note]
SDK methods: `truncateCollection`.
:::

## Truncate a Collection {#truncate}

```sql
TRUNCATE TABLE books;
TRUNCATE TABLE books IN DATABASE archive_db;
```

**This clears all data in the specified collection.** It directly calls official `truncateCollection`, preserving the schema, indexes, and aliases. It is neither a DELETE alias nor a DROP/CREATE reconstruction. WHERE, LIMIT, partition targets, and wildcards are not available. `fetchSize`, `setMaxRows`, and query hints do not limit the number of cleared rows. Names are plain SQL identifiers, not `?` value parameters. The current connection database is the default; explicit `IN DATABASE` does not change `Connection.getCatalog()`.

Use `executeUpdate()` or `execute()`. Success returns update count 0 as SDK acknowledgement, not zero deleted rows; there is no affected-row count, generated key, or ResultSet. JDBC transactions cannot roll this operation back. A timeout or disconnected client does not prove the server did not execute it. The driver adds no retries, entity scans, or alternate deletion strategy. Confirm the target and recovery plan before execution.

The native API was introduced in Milvus 2.6.11; see the [official release notes](https://milvus.io/docs/v2.6.x/release_notes.md#v2611) and [Java API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/truncateCollection.md). Milvus 2.6.2 returns native `UNIMPLEMENTED` and cannot execute this command. The driver provides no legacy fallback.
