---
id: compatibility
sidebar_position: 5
title: Versions and Supported Scope
description: Runtime and server requirements, feature conditions and JDBC usage boundaries for jdbc-milvus.
---

## Driver and Runtime {#release}

These pages apply to `jdbc-milvus 6.7.1-SNAPSHOT`, not older drivers. Java 17+ is required; the driver uses the V2 API of Milvus Java SDK `2.6.22`.

Build the SNAPSHOT from source and install it in your local repository; see [Installation and Connection](../../drivers/milvus/connection.mdx) for dependencies. For a published version, read its matching documentation. Available artifacts are listed in [Maven Central](https://repo.maven.apache.org/maven2/net/hasor/jdbc-milvus/maven-metadata.xml).

## Server Requirements {#servers}

| Server / environment | Requirements and limitations |
| --- | --- |
| Milvus 2.6.x | Minimum version 2.6.2; UPDATE depends on Partial Upsert introduced in that version. Optional features may require a newer patch version. |
| Nullable vector fields | Requires Milvus 2.6.18+; vectors do not support IS NULL / IS NOT NULL filters. |
| TRUNCATE TABLE | Native API introduced in Milvus 2.6.11; unavailable on 2.6.2. Successful behavior on newer servers has not been verified against a real service. |
| Milvus 2.5.x and earlier, 2.6.0 / 2.6.1 | Unsupported; no older-API fallback. |
| Milvus 3.0.x | Outside the scope of these pages. |
| Zilliz Cloud | Use the standard cluster endpoint, token/API key and TLS. Required permissions and network access must be available; Import also requires the corresponding REST API at the endpoint. |

Version references: [official SDK compatibility table](https://github.com/milvus-io/milvus-sdk-java#milvus-java-sdk), [Partial Upsert](https://milvus.io/docs/v2.6.x/upsert-entities.md), and [nullable vectors](https://github.com/milvus-io/milvus/releases/tag/v2.6.18).

## Feature Conditions {#features}

| Capability | Driver behavior | Conditions / limitations |
| --- | --- | --- |
| Scalar, KNN and range SELECT | Single requests for small results; on-demand paging for large or unbounded results | One query vector per distance expression; fetchSize is a page size, whereas LIMIT/maxRows limit the total. |
| UPDATE / DELETE | UPDATE selects keys page by page and submits only keys and SET fields; scalar DELETE without LIMIT submits the filter directly, while other DELETE operations select keys in pages before deleting | Full-collection operations without WHERE are supported; scalar DELETE internally uses the actual primary key IS NOT NULL without replacing a supplied WHERE. No fixed total is added without LIMIT; operations are non-transactional and may partially succeed. |
| Parameterized expressions | WHERE values use SDK filterTemplateValues for queries, COUNT, DELETE, UPDATE selection, Hybrid subqueries and paging | Parameters represent values, not field names, operators or SQL fragments. LIMIT/OFFSET undergo separate integer-range validation. Milvus 2.6.2 rejects template parameters on the right of LIKE; the driver does not interpolate them as a fallback. |
| dbVisitor result handling | JdbcTemplate custom/built-in RowMapper, RowCallbackHandler, and ResultSetExtractor, including Map, Bean, scalar, key/value-pair, and filtered mappings | Mapping processes SQL result columns without extending SQL syntax. Custom aggregation inside an Extractor runs on the client, not as server-side GROUP BY. Row-by-row callbacks do not imply an entirely unbuffered SDK request. |
| dbVisitor parameter sources | Object[], SqlArg/TypeHandler, PreparedStatementSetter, Map/Bean, named parameters, SqlArgSource, and and/in/set dynamic rules | dbVisitor processes parameter sources and dynamic rules before the driver executes native SQL; this does not extend server syntax. `${...}` is plain text substitution for trusted SQL fragments/identifiers, not safe binding for untrusted values. |
| Float/Binary/FP16/BF16/Int8Vector/Sparse | Schema-directed encoding, reading and search | Java inputs, metrics and indexes must match the field; see [typed values](./jdbc.mdx#typed-values). |
| nullable / DEFAULT / Array | Constraints, defaults and Array element types/capacity map to the SDK | DEFAULT supports specified non-key scalars only; Arrays cannot nest or contain NULL elements; nullable vectors require a newer server. |
| Generated Keys | INSERT/UPSERT with RETURN_GENERATED_KEYS exposes SDK-returned IDs | Supports Int64/VarChar and AutoID; keys are not accumulated unless requested, while requesting all keys requires corresponding memory. |
| Hybrid / rerank | The server fuses candidates through RRF or Weighted reranking into one result set | Ordinary Hybrid requires explicit outer LIMIT; grouping instead requires group_limit, while SQL LIMIT always counts rows. No Hybrid Iterator; fetchSize cannot bypass server search windows. |
| BM25 / TextEmbedding | Defines analyzers and functions with server-generated outputs | TextEmbedding needs server support and configured provider/model, credentials and network access. |
| Multi-row writes / Import | Pages VALUES or Iterable/Iterator writes; REST submission, status and failure inspection | Not JDBC Batch; prepare Import files in the storage configured for the server. JSON import and completion status are verified on 2.6.2 standalone with local storage; see [Import](./commands.md#import). |
| TLS / mTLS / Cloud | SDK and Import REST share the JDBC host, port, authentication and certificates | No verification bypass or second-port probing; Import requires an endpoint exposing both gRPC and REST. See [connection and deployment requirements](../../drivers/milvus/connection.mdx#tls). |

See the [syntax manual](./commands.md) for syntax and result columns. Index, function and Import operations also depend on server configuration, permissions and resource limits.

## SQL Coverage of the SDK {#sdk-coverage}

The following compares SQL entry points with capability families in Java SDK 2.6.22. An exposed entry point does not imply that every optional SDK request parameter is available. See the [syntax manual](./commands.md) for exact syntax, parameters, and result columns. This table does not measure dbVisitor Lambda or Mapper integration.

The adapter exposes vector-database business operations, not a complete replacement for the official SDK. SQL covers data access, vector/hybrid search, schema/indexes, loading/releasing, import and necessary administration. Client utilities, internal helpers and methods without a clear SQL business use case are not individually wrapped.

| SDK capability / API | SQL entry point | Coverage and limitations |
| --- | --- | --- |
| `insert` / `upsert` / `delete` | INSERT / UPSERT / UPDATE / DELETE | Exposed; UPDATE uses Partial Upsert, and UPSERT accepts the boolean `partial_update` hint to select partial updates. Paged writes are not cross-page transactions; see [Upsert](./commands.md#upsert). |
| `query` / `queryIterator` / `search` / `searchIteratorV2` | SELECT, COUNT, vector ORDER BY, LIMIT/OFFSET | On-demand paging, scalar filters, and single-vector search are exposed; SDK multi-query-vector batch results have no SQL entry point. |
| Query/Search ignoreGrowing and timezone | SELECT / COUNT … WITH (ignore_growing=…,timezone=…) | Dedicated SDK fields are retained in bounded requests and their iterators; unknown scalar options are rejected. SELECT/COUNT growing-segment exclusion is verified on 2.6.2. Passing timezone does not imply native temporal-type support. Hybrid configures timezone per candidate. See [query-level options](./commands.md#query-options). |
| `hybridSearch`, RRF / Weighted rankers | Hybrid SELECT | Fusion search is exposed; not every advanced SDK search option or ranker has a SQL entry point. |
| AnnSearchReq.filter / filterTemplateValues / timezone; HybridSearchReq.roundDecimal | HYBRID (vector WHERE condition LIMIT … WITH(timezone=…), …) … WITH(round_decimal=…) | Local scalar filters intersect the shared WHERE using SDK-bound values; timezone is per candidate and fused-score precision is outer. Independent filters, local OR scope boundaries and RRF score rounding are verified with dense FLAT/L2 on 2.6.2. See [Hybrid usage](./commands.md#hybrid). |
| Search / Hybrid groupByFieldName, groupSize, strictGroupSize | SELECT … WITH (group_by_field=…,group_limit=…,…) | Native group counts/offsets are separate from SQL/JDBC row windows. FLAT/L2, VARCHAR groups and RRF Hybrid are verified on 2.6.2; this is not relational aggregation. See [grouped search](./commands.md#grouping). |
| Collection / Index / Partition APIs | CREATE / ALTER / DROP / SHOW, LOAD / RELEASE | Basic lifecycle, online field addition, and selected property operations are exposed, not every request parameter. |
| `truncateCollection` | TRUNCATE TABLE … [IN DATABASE …] | Submits the native clearing request for one database/collection, returning update count 0 on success without scanning or recreating it. Introduced in 2.6.11 and unavailable on 2.6.2; SQL exposure does not establish successful clearing on newer servers. See [Truncate a Collection](./commands.md#truncate). |
| `listIndexes` / `describeIndex` | SHOW INDEXES / SHOW INDEX / SHOW PROGRESS OF INDEX | Lists names and describes each index, preserving four detail columns. Unnamed progress sums per-index work, not collection cardinality. Empty/multiple index listings and progress aggregation are verified on 2.6.2; see [Index Queries](./commands.md#index-metadata). |
| `createCollection` partition/clustering keys and creation options | CREATE TABLE … PARTITION KEY / CLUSTERING KEY … WITH (…) | Exposes isPartitionKey, isClusteringKey, numPartitions, numShards, description, and consistencyLevel; SHOW TABLE / SHOW CREATE preserve key flags. Server 2.6.2 tests cover INT64/VARCHAR partition keys, separate/shared clustering keys, partition counts, and filtered search/update/delete. Clustering-compaction performance is not validated. See [Collection Keys](./commands.md#collection-keys). |
| `loadCollection` / `loadPartitions` | LOAD TABLE … WITH (…) | numReplicas, refresh, loadFields, skipLoadDynamicField, and resourceGroups are exposed. Existing hints control SDK synchronous waiting and timeout. Multi-replica scheduling is not verified against a real service; see [loading](./commands.md#load) for 2.6.2 field-loading limitations. |
| `renameCollection` | ALTER TABLE … RENAME TO … [IN DATABASE …] | Local rename and targetDbName are exposed; 2.6.2 verifies data access, updates, and index usability across database round trips. No connection-database switch, data copying, or automatic database creation. See [renaming](./commands.md#rename). |
| `createDatabase` / `describeDatabase` / `alterDatabaseProperties` / `dropDatabaseProperties` / `listDatabases` / `dropDatabase` | CREATE / ALTER / SHOW / DROP DATABASE | Database and property management are exposed. |
| `createAlias` / `alterAlias` / `dropAlias` / `describeAlias` / `listAliases` | CREATE / ALTER / DROP / SHOW ALIAS | Alias lifecycle and queries are exposed. |
| `getCollectionStats` / `getPartitionStats` | SHOW STATS | Returns native statistics, not an immediately exact COUNT. |
| `describeReplicas` | SHOW REPLICAS FROM [TABLE] collection | Replica, node, shard and resource-group snapshots in the current database; all SDK 2.6.22 replica fields are retained. Single-replica queries and missing-collection errors are verified on 2.6.2, not multi-node scheduling or driver-side multiple-address routing. See [replica status](./commands.md#replicas). |
| `getServerVersionV2` / `checkHealth` | SHOW VERSION / SHOW HEALTH | Server version, build details, health reasons and quota states; see [diagnostics](./commands.md#diagnostics). |
| `getPersistentSegmentInfo` / `getQuerySegmentInfo` | SHOW PERSISTENT / QUERY SEGMENTS | Native segment snapshots and all SDK response fields, without entity queries or implicit loading/flush; see [columns and boundaries](./commands.md#diagnostics). |
| `flush` / `flushAll` / `getFlushAllState` | Collection-list FLUSH; FLUSH ALL TABLES; SHOW FLUSH ALL | Collection lists, database scope, and wait timeout map to SDK fields. The default wait limit is 60000ms and can be configured explicitly. flushAll also waits synchronously, returning a BIGINT timestamp on success; SHOW observes state once. On 2.6.2, database-scoped completion checks can wait on other databases and time out, reproducible with the native SDK. This is not complete database-wide flush acceptance; see [Flush semantics and version limits](./commands.md#flush). |
| `compact` / `getCompactionState` / `getCompactionPlans` | COMPACT, SHOW COMPACTION | COMPACT submits asynchronously and SHOW observes it; no automatic compaction wait or guaranteed storage savings. |
| User, role, `grantPrivilegeV2` / `revokePrivilegeV2`, and privilege-group APIs | USER / ROLE / PRIVILEGE / PRIVILEGE GROUP commands | Lifecycle, membership, and explicitly scoped grants are exposed. Grant-record checks are not production authorization acceptance. |
| `CreateUserReq.description` / `CreateRoleReq.description` / `DropRoleReq.forceDrop` | CREATE USER / ROLE … WITH (description=…); DROP ROLE … WITH (force_drop=…) | Dedicated SDK fields and parameter binding are exposed. On 2.6.2, forced deletion removes a role, its grants, and memberships while keeping the user. Creation descriptions read back as empty on this baseline, also through the native SDK; this is not description-persistence support. See [user and role management](./commands.md#user). |
| `updateUser` / `alterRole` | ALTER USER / ROLE … WITH (description=?) | SQL entry points exist, but the 2.6.2 environment does not support these description updates. Verify other versions separately. No password reset or role recreation fallback. |
| ResourceGroup APIs, `transferNode` / `transferReplica` | RESOURCE GROUP, TRANSFER NODES / REPLICAS | Configuration, queries, and migration requests are exposed. Zero-node configuration is verified; actual migration, label selection, and nonzero-node scheduling are not accepted as verified. |
| `runAnalyzer` | ANALYZE | Standard tokenization, multiple/empty texts, details, and hashes are verified. Field-context and named-analyzer parameters are exposed but not verified against a real service. |
| Collection schema vectors, nullable, Array, and functions | CREATE TABLE field and function definitions | Float/Binary/FP16/BF16/Int8Vector/Sparse, scalar nullable/default, Array, BM25, and TextEmbedding definitions are supported, not every new SDK type. Function DESCRIPTION is separate from WITH parameters; description round-trips and searches over BM25-generated vectors are verified on 2.6.2. |
| `addCollectionFunction` / `alterCollectionFunction` / `dropCollectionFunction` | ALTER TABLE … ADD / ALTER / DROP FUNCTION | Native requests are exposed; ALTER submits a complete replacement with the same function name. All three APIs return UNIMPLEMENTED on 2.6.2, without a collection-recreation fallback. Successful online changes on newer servers remain unverified; see [online function management](./commands.md#alter-functions). |
| Import REST APIs (not BulkWriter file generation) | IMPORT and task-query commands | Submission and status/failure observation are exposed. Files are neither generated nor uploaded; this is not full BulkWriter coverage. |

See [connection parameters](../../drivers/milvus/connection.mdx#tls) for TLS/mTLS and Cloud configuration. Successful SQL checks do not certify Cloud, certificate deployment, or throughput. Unlisted SDK methods and optional parameters must not be assumed supported, especially advanced search options, new data types, and other maintenance APIs.

## JDBC and Non-Transactional Boundaries {#boundaries}

Dates stored in VARCHAR remain strings, not native Milvus DATE/TIMESTAMP values. JDBC Date/Time/Timestamp bindings without Calendar have been verified through INSERT/UPSERT, filters, and corresponding getters, plus Timestamp UPDATE/NULL. Timestamp fractional seconds are preserved as text; java.util.Date retains millisecond precision. Legacy locale-formatted strings are not parsed or migrated automatically. Calendar, cross-time-zone behavior, and server-side date arithmetic are not verified; see [Parameter Types](./jdbc.mdx#parameter-types).

On Milvus 2.6.2, integer template comparisons wrapped in NOT, including double NOT, can trigger a QueryNode assertion reproducible with the native SDK. This does not mean all NOT expressions are unsupported: literal NOT and `NOT (field IS NULL)` have passed verification. The driver does not interpolate parameters or eliminate NOT to conceal the server issue. Lambda conditions share this boundary; see [Lambda Queries](./jdbc.mdx#lambda-query).

Transactions, savepoints, whole-operation rollback, JDBC addBatch/executeBatch, stored procedures and updatable ResultSets are unsupported. SQL does not support JOIN/GROUP BY, arbitrary projection expressions, column aliases or scalar ORDER BY. Counting uses `COUNT FROM ...` or a standalone `SELECT COUNT(*) FROM ...`.

The 2.6.2 baseline also rejects AND/OR combinations of field predicates and constant conditions, such as `id IN (1,2) AND 1=1`; see the [official parser](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/parser/planparserv2/parser_visitor.go#L1019). Both Query and Delete reproduce this with the native SDK; parentheses or `AND true` do not resolve it. The driver does not remove conditions, evaluate them on the client, or substitute an empty filter. A rejected deletion has been verified to preserve the existing rows and prevent later statements in the same SQL sequence from executing; this does not imply rollback for arbitrary interrupted writes.

Partial Upsert does not make the driver rewrite untouched fields, but concurrent updates to the same field may still overwrite each other. It provides no cross-page atomicity, isolation or exactly-once guarantee. `maxRetry` retries only recognized transient UPDATE Partial Upsert / DELETE write errors. Successful pages are not rolled back; a failed page may already have taken effect. See [paged writes](./commands.md#dml) for errors and cancellation behavior.

Access multiple SQL statements with `execute()` / `getMoreResults()`; this is not JDBC Batch or a transaction. Paging errors may surface from `ResultSet.next()`; close results promptly. ORM, BI and migration tools that depend on arbitrary relational SQL, complete DatabaseMetaData or transaction behavior must be checked against the JDBC methods actually supported.
