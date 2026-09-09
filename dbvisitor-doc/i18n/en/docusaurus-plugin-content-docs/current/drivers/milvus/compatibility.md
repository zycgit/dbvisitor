---
id: compatibility
sidebar_position: 5
title: Release and Support Matrix
description: jdbc-milvus source and release versions, SDK dependency, server baseline, feature requirements and validation scope.
---

## Source and Published Versions {#release}

These Milvus pages describe the current repository implementation, not the behavior of every older release. Version information below was checked on 2026-09-08.

| Component | Version / status | Meaning |
| --- | --- | --- |
| Current source | `6.7.1-SNAPSHOT` | From root `gradle.properties`; newly documented capabilities refer to this source. |
| Build and runtime | Java 17+ | The current build targets Java 17, independently of the SDK's own Java minimum. |
| Milvus Java SDK | `2.6.22` | From `jdbc-milvus/build.gradle`; the driver directly uses V2 APIs. |
| jdbc-milvus on Maven Central | `6.7.0` | The only version listed in metadata at verification time; this does not promise current workspace features. |

Published artifacts are authoritative in [Maven Central metadata](https://repo.maven.apache.org/maven2/net/hasor/jdbc-milvus/maven-metadata.xml). Repository versions, Git tags, the website's global version label and Central artifacts are distinct; none substitutes for publication evidence. The SNAPSHOT is not labeled as published here.

For local source installation, Maven/Gradle coordinates and the `all` classifier, see [Install and Use](./usecase.mdx). For a release, verify its actual POM, dependencies and classifier and use the matching source-version documentation.

## Server Scope {#servers}

The minimum baseline is the project's version threshold, not a claim that every optional feature works there or that every later version has passed acceptance.

| Server / environment | Current scope | Validation boundary |
| --- | --- | --- |
| Milvus 2.6.2 | Minimum baseline; UPDATE uses native Partial Upsert | A local Docker environment is provided; full feature-matrix acceptance remains necessary. |
| Milvus 2.6.x, at least 2.6.2 | Current adapter target with SDK 2.6.22 | Not a claim that the entire range passed; verify the specific patch version and features. |
| Nullable vectors on Milvus 2.6.18+ | Modeled by the driver; server support starts at 2.6.18 | Cannot be accepted on baseline 2.6.2; vectors do not support IS NULL/IS NOT NULL predicates. |
| Milvus 2.5.x and earlier, 2.6.0/2.6.1 | Outside current support scope | No old-version API fallback is maintained. |
| Milvus 3.0.x | No support commitment established | Requires separate SDK, protocol and real-cluster evaluation; “2.6.2+” does not imply 3.0 compatibility. |
| Zilliz Cloud | Standard endpoint, token/API key and TLS settings are implemented | Actual cloud permissions, network access and Import still require live-cluster acceptance. |

Version references: [official SDK compatibility table](https://github.com/milvus-io/milvus-sdk-java#milvus-java-sdk), [Partial Upsert's 2.6.2 requirement](https://milvus.io/docs/v2.6.x/upsert-entities.md), and [nullable-vector release notes for 2.6.18](https://github.com/milvus-io/milvus/releases/tag/v2.6.18). The vendor SDK table is not a jdbc-milvus test report.

## Implemented Capabilities and Conditions {#features}

| Capability | Driver behavior | Conditions / limitations |
| --- | --- | --- |
| Scalar, KNN and range SELECT | Single requests for small results; on-demand pages for large or unbounded results | One query vector per distance expression; fetchSize controls a page, LIMIT/maxRows control total rows. |
| UPDATE / DELETE | Select primary keys page by page; UPDATE submits only keys and SET fields | No fixed total without LIMIT; non-transactional and partial success is possible. |
| Prepared expressions | WHERE values use SDK `filterTemplateValues` for queries, COUNT, deletes, UPDATE selection, hybrid candidates and iterators | Parameters represent values, not field names, operators or SQL fragments. LIMIT/OFFSET receive separate integer/range validation. |
| Float/Binary/FP16/BF16/Sparse | Schema-directed encoding, reads and search | Java input, metric and index must match; see [typed values](./usecase.mdx#typed-values). |
| nullable / DEFAULT / Array | Maps constraints, defaults and Array element/capacity to the SDK | DEFAULT is limited to specified non-key scalars; no nested Arrays or NULL elements; nullable vectors require a newer server. |
| Generated keys | INSERT/UPSERT with RETURN_GENERATED_KEYS exposes SDK IDs | Int64/VarChar and AutoID; IDs are not accumulated unless requested, while retaining all keys costs proportional memory. |
| Hybrid / rerank | Server-side RRF or Weighted fusion into one result set | Explicit outer LIMIT required; no Hybrid Iterator, and fetchSize does not bypass server search windows. |
| BM25 / TextEmbedding | Models analyzers and functions with server-generated outputs | TextEmbedding depends on the target version, provider/model, credentials and network, not just driver compilation. |
| Multi-row writes / Import | Pages VALUES or Iterable/Iterator inputs; REST job submission, status and failure inspection | Not JDBC batch; prepare and upload Import files first; throughput and large-scale imports require load tests. |
| TLS / mTLS / Cloud | SDK and Import REST share the JDBC host, port, authentication and certificates | No verification bypass or second-port probing; the endpoint must expose both gRPC and REST. See [deployment requirements](./params.md#tls). |

See the [syntax manual](./commands.md) for exact syntax and result columns. This table does not extend parser or server capabilities.

## JDBC and Non-Transactional Boundaries {#boundaries}

Transactions, savepoints, whole-operation rollback, JDBC addBatch/executeBatch, stored procedures, updatable ResultSets, JOIN/GROUP BY, arbitrary projections, column aliases and scalar ORDER BY are unsupported. Counting uses `COUNT FROM ...`.

Native Partial Upsert avoids driver-side rewriting of untouched fields, but does not provide cross-page atomicity, isolation or exactly-once execution. `maxRetry` covers recognized transient paged UPDATE/DELETE write failures only. Successful pages are not rolled back; a failed page may already have taken effect. See [paged writes](./commands.md#dml) for progress and cancellation distinctions.

Multiple SQL statements use `execute` / `getMoreResults`, not JDBC batch or a transaction. Paging failures may surface from `ResultSet.next()`; always close results. ORM, BI and migration tools requiring arbitrary relational SQL, complete DatabaseMetaData or transactions are not automatically compatible merely because the driver exposes JDBC.

## Validation and Release Checks {#validation}

Run offline regression from the repository root, with dependencies already cached:

```bash
./gradlew :jdbc-milvus:build :dbvisitor-driver:test --no-daemon --offline
```

Ordinary tests exclude `realdb`. Documentation tests compile Chinese/English Java examples, run getting-started, paging and multi-result fragments through interceptors, parse SQL examples and check the connection-property tables. These checks do not replace real-server acceptance.

The separate real-environment task for single-port plaintext/TLS/mTLS is:

```bash
./gradlew :jdbc-milvus:tlsTest --no-daemon --offline
```

First start the optional services described in the [Docker test environment](https://github.com/zycgit/dbvisitor/blob/main/dbvisitor-test/docker/README.md). Missing services fail this task rather than skipping it. Having this command does not establish a complete CI compatibility matrix.

Before publishing:

1. Align source/tag, version, published POM, classifier, SDK dependency and documentation; run the JDBC example independently using the published artifacts.
2. Record environments and results for each claimed Milvus version, distinguishing offline, live-cluster, TLS, Cloud and performance tests. Do not label unverified items as passed.
3. Validate extended field types, nullable vectors, Hybrid/functions, Import status and failure inspection; use real configurations for external providers and Cloud.
4. Verify artifact availability on Central before updating publication claims; a local build, documentation update or tag alone is not a release.
