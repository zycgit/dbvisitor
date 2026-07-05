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
Among them, **JdbcTemplate**, **Annotation-based**, and **Mapper File** are available on all data sources.

dbVisitor has intelligent dialect inference capabilities, automatically identifying the target database type from the JDBC URL and configuring the optimal dialect — **manual configuration is usually unnecessary**.
If explicit specification is needed, **dialect aliases** (e.g., `mysql`) or **fully qualified dialect class names** are supported.

The following table summarizes API support and dialect feature differences across data sources. Column meanings:
- **Builder API** — includes LambdaTemplate, BaseMapper, object mapping, and result set mapping; all four have consistent support
- **Write Conflicts** — all data sources support standard writes (Into); this column only notes additionally supported conflict strategies ([details](#insert-strategy))
- **Pagination** — whether the dialect implements the `PageSqlDialect` interface
- **Sequence** — whether the dialect implements the `SeqSqlDialect` interface
- **Vector** — whether the dialect implements the `VectorSqlDialect` interface ([details](#vector))
- **Null Ordering** — whether the dialect overrides the `orderByNulls` method

| Data Source | Config Key | Builder API | Batch | Stored Proc | Auto Key Backfill | Pagination | Write Conflict | Sequence | Vector | Null Ordering |
|-------|--------|:--------:|:-----:|:-----:|:----:|:--:|------|:--:|:--:|:----:|
| MySQL | mysql  | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update | | | ✅ |
| MariaDB | mariadb | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update | | | ✅ |
| PostgreSQL | postgresql | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update¹ | ✅ | ✅ | |
| KingbaseES | kingbase | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update¹ | ✅ | ✅ | |
| Oracle | oracle | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | | | |
| Dameng | dm     | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | ✅ | | |
| SQL Server | sqlserver/ jtds | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | | | |
| SQL Server (jTDS) | jtds   | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | | | |
| DB2 | db2    | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | | | |
| H2 | h2     | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | ✅ | | |
| Apache Derby | derby  | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| HSQL | hsql   | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| Hive | hive   | ✅ | ✅ | ✅ |  ✅   | ⚠️ | | | | |
| Apache Impala | impala | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| IBM Informix | informix | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| SQLite | sqlite | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| Xugu | xugu   | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| Redis | —      | ❌ | ❌ | ❌ |  ❌   | | | | | |
| MongoDB | mongo  | ✅ | ❌ | ❌ |  ✅   | ✅ | | | | |
| ElasticSearch 6 | elastic6 | ✅ | ❌ | ❌ |  ✅   | ✅ | | | | |
| ElasticSearch 7 | elastic7 | ✅ | ❌ | ❌ |  ✅   | ✅ | | | ✅ | |
| ElasticSearch 8 | elastic8 | ✅ | ❌ | ❌ |  ✅   | ✅ | | | ✅ | |
| Milvus | milvus | ✅ | ❌ | ❌ |  ❌   | ✅ | | | ✅ | |

> ✅ Supported &nbsp; ❌ Not Supported
>
> ¹ Requires primary key
>
> **⚠️ Hive**: Although `PageSqlDialect` is implemented, both `countSql` and `pageSql` throw `UnsupportedOperationException`, making pagination effectively unusable.

:::info[JDBC Feature Support]
For non-relational database drivers (Mongo, Elastic), dbVisitor implements the `Statement.RETURN_GENERATED_KEYS` feature.
This means that when using `JdbcTemplate` or `Statement` to execute insert operations, the generated `_id` can be automatically retrieved.
:::

### Non-Relational Data Source Guides

- **[Redis](./redis)** — Supports [140+ commands](../drivers/redis/commands), 5 data type operations; Builder API and object mapping not supported
- **[MongoDB](./mongo)** — Full CRUD support, ObjectId auto-mapping, paginated queries; batch and stored procedures not supported
- **[ElasticSearch](./elastic)** — Full CRUD support, REST DSL-based; batch and stored procedures not supported
- **[Milvus](./milvus)** — SQL-style syntax for vector databases, full CRUD support, KNN nearest neighbor search and range search; batch and stored procedures not supported

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
| Elastic 7  | script_score script     | l2norm, cosineSimilarity, dotProduct, l1norm       |
| Elastic 8  | Native kNN + script query  | L2, COSINE, IP (native); l2norm, etc. (script fallback)             |
| Milvus     | Native vector operators             | L2 (`<->`), Cosine (`<=>`), Inner Product (`<#>`), etc.                  |

## Custom Dialects {#custom-dialect}

If the built-in dialects do not meet your needs, you can customize a dialect by extending `AbstractDialect` and implementing the required interfaces. There are 5 dialect-related interfaces, with `SqlDialect` as the common base:

| Interface | Responsibility |
|------|------|
| `SqlDialect` | Base interface: manages keyword lists, generates table/column/sort column names |
| `ConditionSqlDialect` | Condition-related SQL generation (e.g., LIKE statements) |
| `InsertSqlDialect` | Advanced INSERT statement generation (e.g., [write conflict strategies](../guides/core/lambda/insert#conflict)) |
| `PageSqlDialect` | Pagination statement generation (`countSql` + `pageSql`) |
| `SeqSqlDialect` | Sequence query statement generation |

:::info[Tip]
Extend the `AbstractDialect` abstract class and implement the `PageSqlDialect` interface to customize pagination dialect.
- `countSql` — generates the SQL statement for counting
- `pageSql` — generates the paginated SQL statement
:::

```java title='Register a custom dialect'
SqlDialectRegister.registerDialectAlias(JdbcUtils.MYSQL, MyDialect.class);
```
