---
id: mapper
sidebar_position: 20
title: Mapper API
---

## Method Annotations {#annotations}

Method annotations execute ClickHouse SQL directly. Invalid commands raise errors, but duplicate primary keys do not, and UPDATE / DELETE counts are not guaranteed to reflect matched rows.

For example, a zero return value is not a reliable existence check after updating a missing ID; query the target record instead. See [Waiting for Data Changes](write.mdx#wait-for-writes).

## Mapper Reads and Writes {#operations}

BaseMapper supports inserts, queries, updates, deletes, sample queries, and writes using Map arguments. See [Builder API](builder.md#writes) for mutation behavior.

Handle these validations in your application:

- Duplicate keys: MergeTree primary keys do not enforce uniqueness, including multi-row inserts.
- String length: `String` does not enforce a `VARCHAR(n)` length limit; validate required lengths before writing.
- Mutation counts: driver return values are not actual matched-row counts; read back records when needed.

## Key Strategies {#keys}

Assign IDs before insertion, or query a generated value first and then insert it. ClickHouse does not provide auto-increment key retrieval here; do not rely on `useGeneratedKeys` or current-result-set key retrieval. See [Key Generation](generated-keys.mdx).

## Pagination {#pagination}

Method annotations, BaseMapper, and file-mapper calls by statement ID support pagination. Page numbers start at 0; the total count and current page are queried separately. See [Pagination](pagination.mdx) for generated commands and usage notes.
