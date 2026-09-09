---
id: support
sidebar_position: 1
hide_table_of_contents: true
title: Data Source Support Matrix
description: Compares API, dialect, and proprietary capability support across relational and non-relational databases.
---

# Data Source Support Matrix

dbVisitor strives to use a unified API to operate all **relational databases** and **non-relational databases**. However, in practice, individual data sources still exhibit some differences due to their inherent characteristics.
dbVisitor handles these differences in two main aspects:
- **API Support**: whether a called API is supported on a given data source.
- **Database Dialect**: whether different commands or syntax are used when the same API operates on different databases via the [Builder API](../guides/api/lambda).

:::tip[Note]
If you want to change these differences, you can participate in the project and contribute your improvements.
:::

## Data Source Support Overview {#dialect}

dbVisitor designs 4 types of API under the unified kernel architecture: [Programmatic API](../guides/api/jdbc), [Declarative API](../guides/api/mapper), [Builder API](../guides/api/lambda), [Mapper File](../guides/api/file_mapper).
**JdbcTemplate**, **Annotations**, and **Mapper File** execute commands supported by each data source. They do not translate arbitrary relational SQL into non-relational commands.

dbVisitor has intelligent dialect inference capabilities, automatically identifying the target database type from the JDBC URL and configuring the optimal dialect — **manual configuration is usually unnecessary**.
If explicit specification is needed, **dialect aliases** (e.g., `mysql`) or **fully qualified dialect class names** are supported.

The following table summarizes API support and dialect feature differences across data sources. Column meanings:

- **Builder API** — whether LambdaTemplate / BaseMapper has a built-in dialect; individual operations remain subject to that dialect. Object and result-set mapping are independent capabilities
- **Write Conflicts** — all data sources support standard writes (Into); this column only notes additionally supported conflict strategies ([details](#insert-strategy))
- **Pagination** — whether the built-in dialect provides pagination SQL; Hive only has an unusable placeholder implementation
- **Sequence** — whether the dialect implements the `SeqSqlDialect` interface
- **Vector** — whether the dialect implements the `VectorSqlDialect` interface ([details](#vector))
- **Null Ordering** — whether the builder translates `OrderNullsStrategy`, not whether the database itself supports null ordering

| Data Source | Config Key | Builder API | Pagination | Write Conflicts | Sequence | Vector | Null Ordering |
|-------|--------|:--------:|:--:|------|:--:|:--:|:----:|
| MySQL | mysql  | ✅ | ✅ | Ignore Update | | | ✅ |
| MariaDB | mariadb | ✅ | ✅ | Ignore Update | | | ✅ |
| PostgreSQL | postgresql | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | |
| KingbaseES | kingbase | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | |
| Oracle | oracle | ✅ | ✅ | Ignore¹ Update¹ | | | |
| Dameng | dm     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | | |
| SQL Server | sqlserver | ✅ | ✅ | Ignore¹ Update¹ | | | |
| SQL Server (jTDS) | jtds   | ✅ | ✅ | Ignore¹ Update¹ | | | |
| DB2 | db2    | ✅ | ✅ | Ignore¹ Update¹ | ✅ | | |
| H2 | h2     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | | |
| Apache Derby | derby  | ✅ | ✅ | | | | |
| HSQL | hsql   | ✅ | ✅ | | | | |
| Hive | hive   | ✅ | ⚠️ | | | | |
| Apache Impala | impala | ✅ | ✅ | | | | |
| IBM Informix | informix | ✅ | ✅ | | | | |
| SQLite | sqlite | ✅ | ✅ | | | | |
| Xugu | xugu   | ✅ | ✅ | | | | |
| ClickHouse | clickhouse | ✅ | ✅ | | | | |
| Redis | —      | ❌ | | | | | |
| MongoDB | mongo  | ✅ | ✅ | | | | |
| ElasticSearch 6 | elastic6 | ✅ | ✅ | | | | |
| ElasticSearch 7 | elastic7 | ✅ | ✅ | | | ✅ | |
| ElasticSearch 8 | elastic8 | ✅ | ✅ | | | ✅ | |
| Milvus | milvus | ✅ | ✅ | | | ✅ | |

> ✅ Supported &nbsp; ❌ Not Supported
>
> ¹ Requires primary key

> **⚠️ Hive**: Although `PageSqlDialect` is implemented, both `countSql` and `pageSql` throw `UnsupportedOperationException`, making pagination effectively unusable.

JDBC Batch and multi-statement execution are different capabilities: multi-statement execution submits statements in one `Statement.execute("SQL1; SQL2")` call, advances between results with `getMoreResults()`, and reads result sets or update counts with `getResultSet()` / `getUpdateCount()`. This does not imply JDBC Batch support. Multiple VALUES in one INSERT, paged writes and Import are not JDBC Batch either.

### JDBC Capabilities

Relational JDBC Batch, stored procedures and generated keys depend on the selected JDBC driver, server version and statement, not the existence of a dialect interface. SQLite, for example, does not support stored procedures. Oracle's row-by-row dialect strategy does not mean Oracle JDBC lacks Batch support.

| dbVisitor adapter | JDBC Batch | Stored procedures | getGeneratedKeys |
| --- | --- | --- | --- |
| jdbc-redis | Unsupported | Unsupported | Empty result |
| jdbc-mongo | Unsupported | Unsupported | Insert returns `_id` |
| jdbc-elastic | Unsupported | Unsupported | Document writes return `_id` |
| jdbc-milvus | Unsupported | Unsupported | INSERT/UPSERT returns the collection primary key |

A higher-level bulk API that executes individual calls does not provide driver-level `addBatch()` / `executeBatch()` support. Request generated keys with `Statement.RETURN_GENERATED_KEYS`; these adapters do not support JDBC overloads taking arrays of column names or indexes.

:::info[JDBC Feature Support]
For non-relational database drivers (Mongo, Elastic, Milvus), dbVisitor implements the `Statement.RETURN_GENERATED_KEYS` feature.
Supported JDBC insert/key-retrieval calls expose server-returned IDs. Milvus uses the collection's primary field name, not a fixed `_id` column.
:::

When requesting keys in Mapper annotations/XML, omit `keyColumn` to avoid the unsupported column-name array overload. A single key can be assigned by position using `keyProperty`. Lambda/BaseMapper entity backfill with `KeyType.Auto` uses that array overload and cannot be directly applied to these adapters; use the JDBC flag overload or the Mapper configuration above. See [Milvus version requirements](../drivers/milvus/compatibility.md).

### Non-Relational Data Source Guides

- **[Redis](./redis/about.md)** — Supports [140+ commands](../drivers/redis/commands), 5 data type operations; no Builder API; map results with RowMapper or a JSON TypeHandler
- **[MongoDB](./mongo/about.md)** — Full CRUD support, ObjectId auto-mapping, paginated queries; JDBC Batch and stored procedures not supported
- **[ElasticSearch](./elastic/about.md)** — Full CRUD support, REST DSL-based; JDBC Batch and stored procedures not supported
- **[Milvus](./milvus/about.md)** — SQL subset, multi-statement execution, vector/Hybrid search, paged Partial UPDATE, generated keys, multi-row writes and Import; JDBC Batch, transactions and stored procedures are unsupported.

---

### Write Strategy Details {#insert-strategy}

For databases that support write conflict strategies, each dialect uses different underlying implementations:

| Dialect         | Ignore Implementation                                    | Update Implementation                                              | PK Required |
|------------|----------------------------------------------|---------------------------------------------------------|:------:|
| MySQL      | `INSERT IGNORE INTO ...`                     | `INSERT INTO ... ON DUPLICATE KEY UPDATE`               | No |
| PostgreSQL | `INSERT INTO ... ON CONFLICT DO NOTHING`     | `INSERT INTO ... ON CONFLICT(pk) DO UPDATE SET ...`     | Update only |
| Oracle     | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | Yes |
| SQL Server  | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | Yes |
| DB2         | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | Yes |
| H2          | `MERGE INTO ...`                              | `MERGE INTO ...`                                      | Yes |
| Dameng         | `INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX */ INTO ...` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | Yes |
| ClickHouse  | Not supported | Not supported | — |

### Generated Key Backfill Strategy {#generated-key-strategy}

Lambda / BaseMapper generates INSERT SQL via dbVisitor, so dialects can choose execution methods based on whether return columns are needed and whether a duplicate key strategy is used.

| Dialect | No Backfill Columns | Into + Backfill Columns | Ignore + Backfill Columns | Update + Backfill Columns |
| --- | --- | --- | --- | --- |
| MySQL | `JdbcBatch` | `JdbcBatchGeneratedKeys` | `OneByOne` | `OneByOne` |
| PostgreSQL | `JdbcBatch` | `MultiValuesResultSet` | `OneByOne` | `MultiValuesResultSet` |
| SQL Server | `JdbcBatch` | `MultiValuesResultSet` | `OneByOne` | `OneByOne` |
| Oracle | `OneByOne` | `OneByOne` | `OneByOne` | `OneByOne` |
| Dameng | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |
| DB2 | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |
| H2 | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |
| ClickHouse | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |

`MultiValuesResultSet` means the SQL itself returns the current `ResultSet`, e.g., PostgreSQL `RETURNING` or SQL Server `OUTPUT INSERTED`; `JdbcBatchGeneratedKeys` means using JDBC batch generated keys; `OneByOne` means executing row by row to ensure backfill semantics.

### Vector Query Support Details {#vector}

Different database vector dialects support different distance metric functions and query methods:

| Dialect         | Query Method                | Supported Distance Metrics                                         |
|------------|---------------------|------------------------------------------------|
| PostgreSQL | pgvector operators        | L2 (`<->`), Cosine (`<=>`), Inner Product (`<#>`), etc.                  |
| Elastic 7  | Script sort / script filter     | l2norm, cosineSimilarity, dotProduct, l1norm       |
| Elastic 8  | Native kNN + script query  | L2, COSINE, IP (native); l2norm, etc. (script fallback)             |
| Milvus     | Native vector operators             | L2 (`<->`), Cosine (`<=>`), Inner Product (`<#>`), etc.                  |

## Custom Dialects {#custom-dialect}

If the built-in dialects do not meet your needs, you can customize a dialect by extending `AbstractDialect` and implementing the required interfaces. The main dialect interfaces are listed below, with `SqlDialect` as their common base:

| Interface | Responsibility |
|------|------|
| `SqlDialect` | Base interface: manages keyword lists, generates table/column/sort column names |
| `ConditionSqlDialect` | Condition-related SQL generation (e.g., LIKE statements) |
| `InsertSqlDialect` | Advanced INSERT statement generation (e.g., [write conflict strategies](../guides/core/lambda/insert#conflict)) |
| `PageSqlDialect` | Pagination statement generation (`countSql` + `pageSql`) |
| `SeqSqlDialect` | Sequence query statement generation |
| `VectorSqlDialect` | Vector ordering and range conditions |

:::info[Tip]
Extend the `AbstractDialect` abstract class and implement the `PageSqlDialect` interface to customize pagination dialect.
- `countSql` — generates the SQL statement for counting
- `pageSql` — generates the paginated SQL statement
:::

```java title='Register a custom dialect'
SqlDialectRegister.registerDialectAlias(JdbcHelper.MYSQL, MyDialect.class);
```
