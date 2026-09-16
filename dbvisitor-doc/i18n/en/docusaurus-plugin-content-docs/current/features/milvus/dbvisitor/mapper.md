---
id: mapper
slug: /features/milvus/mapper
sidebar_position: 20
title: Mapper API
---

## Method Annotations {#annotations}

Annotations use the driver's Milvus SQL and support bean, Map, scalar, and list results, plus `COUNT(*)`. `DISTINCT` and general SQL aggregate projections are unsupported. See [Query Operations](query.mdx#query-apis).

Annotations can write and delete data, but ordinary INSERT does not reject duplicates with a primary-key exception. Deletion counts for supplied primary keys may include missing IDs, so the return value alone cannot confirm that records were deleted.

## Mapper Reads and Writes {#operations}

BaseMapper supports inserts, primary-key and sample queries, updates, replacement, upsert, and writes using Map arguments. Updates use Partial Update rather than overwriting unchanged fields with a complete entity.

Ordinary INSERT, including multi-row INSERT, does not guarantee duplicate-key errors. Deleting a missing key does not guarantee a zero count either. Query separately when existence matters; see [Data Writes](write.mdx#delete-count).

## Key Strategies {#keys}

Supply IDs to collections with AutoID disabled, or retrieve AutoID-generated keys. Method annotations use `useGeneratedKeys` with the default source, not `generatedKeySource="resultSet"`.

Milvus has one database primary-key field and no composite primary-key constraint. Combining entity conditions does not establish composite uniqueness. It also has no database sequence for `selectKey`. See [Key Generation](generated-keys.mdx).

## Pagination {#pagination}

Method annotations, BaseMapper, and file calls support pagination. Ordinary fields cannot define sort order; use vector-distance ordering when ordered pages are required. See [Pagination](pagination.mdx) for counts and vector-window limits.

## Execution Options {#options}

Use the default or `FORWARD_ONLY` result-set type. `SCROLL_INSENSITIVE` and `SCROLL_SENSITIVE` are unsupported; requery or collect results into a List for repeated reading.

`fetchSize` controls retrieval batch size, not page size or a total-row limit. See the core API's [Execution Options](../../../guides/core/mapper/annotation_query.mdx#options).

## Referencing File Mappers {#file-mapper}

Calls by statement ID can query, write, and read return values using Milvus SQL. DELETE retains the driver's count semantics: deleting a supplied but missing primary key does not guarantee zero. See [Data Writes](write.mdx#delete-count).
