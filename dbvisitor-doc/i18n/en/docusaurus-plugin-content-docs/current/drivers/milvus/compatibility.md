---
id: compatibility
sidebar_position: 5
title: Versions and Supported Scope
description: Runtime and server requirements, feature conditions and JDBC usage boundaries for jdbc-milvus.
---

## Driver and Runtime {#release}

These pages apply to `jdbc-milvus 6.7.1-SNAPSHOT`, not older drivers. Java 17+ is required; the driver uses the V2 API of Milvus Java SDK `2.6.22`.

Build the SNAPSHOT from source and install it in your local repository; see [Install and Use](./usecase.mdx) for dependencies. For a published version, read its matching documentation. Available artifacts are listed in [Maven Central](https://repo.maven.apache.org/maven2/net/hasor/jdbc-milvus/maven-metadata.xml).

## Server Requirements {#servers}

| Server / environment | Requirements and limitations |
| --- | --- |
| Milvus 2.6.x | Minimum version 2.6.2; UPDATE depends on Partial Upsert introduced in that version. Optional features may require a newer patch version. |
| Nullable vector fields | Requires Milvus 2.6.18+; vectors do not support IS NULL / IS NOT NULL filters. |
| Milvus 2.5.x and earlier, 2.6.0 / 2.6.1 | Unsupported; no older-API fallback. |
| Milvus 3.0.x | Outside the scope of these pages. |
| Zilliz Cloud | Use the standard cluster endpoint, token/API key and TLS. Required permissions and network access must be available; Import also requires the corresponding REST API at the endpoint. |

Version references: [official SDK compatibility table](https://github.com/milvus-io/milvus-sdk-java#milvus-java-sdk), [Partial Upsert](https://milvus.io/docs/v2.6.x/upsert-entities.md), and [nullable vectors](https://github.com/milvus-io/milvus/releases/tag/v2.6.18).

## Feature Conditions {#features}

| Capability | Driver behavior | Conditions / limitations |
| --- | --- | --- |
| Scalar, KNN and range SELECT | Single requests for small results; on-demand paging for large or unbounded results | One query vector per distance expression; fetchSize is a page size, whereas LIMIT/maxRows limit the total. |
| UPDATE / DELETE | UPDATE selects keys page by page and submits only keys and SET fields; scalar DELETE without LIMIT submits the filter directly, while other DELETE operations select keys in pages before deleting | No fixed total is added without LIMIT; operations are non-transactional and may partially succeed. |
| Parameterized expressions | WHERE values use SDK filterTemplateValues for queries, COUNT, DELETE, UPDATE selection, Hybrid subqueries and paging | Parameters represent values, not field names, operators or SQL fragments. LIMIT/OFFSET undergo separate integer-range validation. |
| Float/Binary/FP16/BF16/Sparse | Schema-directed encoding, reading and search | Java inputs, metrics and indexes must match the field; see [typed values](./usecase.mdx#typed-values). |
| nullable / DEFAULT / Array | Constraints, defaults and Array element types/capacity map to the SDK | DEFAULT supports specified non-key scalars only; Arrays cannot nest or contain NULL elements; nullable vectors require a newer server. |
| Generated Keys | INSERT/UPSERT with RETURN_GENERATED_KEYS exposes SDK-returned IDs | Supports Int64/VarChar and AutoID; keys are not accumulated unless requested, while requesting all keys requires corresponding memory. |
| Hybrid / rerank | The server fuses candidates through RRF or Weighted reranking into one result set | Explicit outer LIMIT required; no Hybrid Iterator, and fetchSize cannot bypass server search windows. |
| BM25 / TextEmbedding | Defines analyzers and functions with server-generated outputs | TextEmbedding needs server support and configured provider/model, credentials and network access. |
| Multi-row writes / Import | Pages VALUES or Iterable/Iterator writes; REST submission, status and failure inspection | Not JDBC Batch; Import files must be prepared and uploaded to object storage accessible to Milvus. |
| TLS / mTLS / Cloud | SDK and Import REST share the JDBC host, port, authentication and certificates | No verification bypass or second-port probing; Import requires an endpoint exposing both gRPC and REST. See [connection and deployment requirements](./params.md#tls). |

See the [syntax manual](./commands.md) for syntax and result columns. Index, function and Import operations also depend on server configuration, permissions and resource limits.

## JDBC and Non-Transactional Boundaries {#boundaries}

Transactions, savepoints, whole-operation rollback, JDBC addBatch/executeBatch, stored procedures and updatable ResultSets are unsupported. SQL does not support JOIN/GROUP BY, arbitrary projection expressions, column aliases or scalar ORDER BY. Counting uses `COUNT FROM ...`.

Partial Upsert does not make the driver rewrite untouched fields, but concurrent updates to the same field may still overwrite each other. It provides no cross-page atomicity, isolation or exactly-once guarantee. `maxRetry` retries only recognized transient UPDATE Partial Upsert / DELETE write errors. Successful pages are not rolled back; a failed page may already have taken effect. See [paged writes](./commands.md#dml) for errors and cancellation behavior.

Access multiple SQL statements with `execute()` / `getMoreResults()`; this is not JDBC Batch or a transaction. Paging errors may surface from `ResultSet.next()`; close results promptly. ORM, BI and migration tools that depend on arbitrary relational SQL, complete DatabaseMetaData or transaction behavior must be checked against the JDBC methods actually supported.
