---
id: flush
sidebar_position: 4
title: FLUSH
---

:::info[Note]
SDK methods: `flush`, `flushAll`, `getFlushAllState`.
:::

## Flush {#flush}

```sql
FLUSH table_name;
FLUSH first_table, second_table IN DATABASE archive WITH (wait_flushed_timeout_ms=60000);
FLUSH ALL TABLES;
FLUSH ALL TABLES IN DATABASE archive WITH (wait_flushed_timeout_ms=?);
```

`FLUSH table_name[, ...]` submits one SDK `FlushReq.collectionNames` list, waits, and returns update count 0. `FLUSH ALL TABLES` calls SDK `flushAll` and **also waits synchronously for completion**, returning one BIGINT `FLUSH_ALL_TS` row on success. Use `executeQuery()` or `execute()` to obtain it, not `getGeneratedKeys()`. It is not an asynchronous submission-only operation. `FLUSH all` still flushes the single collection named `all`, not the whole database.

Both forms default to the current connection database. `IN DATABASE db_name` explicitly selects another database without changing the connection catalog. Only `FLUSH ALL TABLES IN DATABASE *` requests all databases; use the same scope for observation, such as `SHOW FLUSH ALL ? IN DATABASE *`. Collection and database names must be non-empty SQL identifiers, not `?` bindings. Collection-list flush does not accept a database wildcard.

`WITH` accepts only `wait_flushed_timeout_ms`, bindable as a non-negative integer within the BIGINT range. The default preserves the existing 60000ms wait limit; explicit zero selects unlimited SDK waiting, so use it cautiously. This controls SDK completion waiting, not total JDBC call duration: submission RPCs, network behavior, and SDK retries also depend on connection settings. sync/timeout hints are not consumed, and `sync=false` cannot make the operation asynchronous.

**Database-scoped FlushAll completion waiting has a server limitation on 2.6.2**: `GetFlushAllState` filters the requested database but then checks checkpoints across every database, allowing unrelated databases to delay completion until timeout. See the [official 2.6.2 implementation](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/datacoord/services.go#L1519). Both JDBC and native SDK waiting are affected. Use explicit collection-list FLUSH on this baseline; an all-databases wildcard does not bypass this waiting limitation. Check native fixes before relying on other versions.

FLUSH is not JDBC commit or a cross-statement transaction boundary. Failure, timeout, or cancellation does not imply that no data was flushed. SDK waiting failures may leave no timestamp available to return. The driver does not fall back to collection-by-collection flushing, extra polling, or fabricated success, and does not resubmit outside SDK behavior. Frequent flushes are subject to server rate limits.
