---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ClickHouse
description: ClickHouse usage with dbVisitor.
---

# ClickHouse

Use JdbcTemplate, Mapper, or the Builder API to query and insert ClickHouse data. For changes to MergeTree tables, see the mutation example below.

| Scenario | Usage |
| --- | --- |
| Key generation | Application-generated IDs |
| Pagination | LIMIT pagination |
| Insert conflicts | Duplicate records in MergeTree |
| Data changes | Submit, wait for, and inspect mutations |

- [Programmatic API](/docs/features/clickhouse/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](builder.md): Supported operations, datasource-specific behavior, and usage.
- [Waiting for Data Changes](./write.mdx): Wait for changes and inspect failures.
- [Batch Inserts](./batch-insert.mdx): Submit multiple events together.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Duplicate Records](./conflict.mdx): Read the latest version of a repeated ID.
- [Vector Operations](./vectors.md): Field mapping, search metrics, and supported queries.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Type Support](./types.md): Choose Java property types.
- [Transaction Support](transactions.md) — transaction API behavior and isolation settings.
