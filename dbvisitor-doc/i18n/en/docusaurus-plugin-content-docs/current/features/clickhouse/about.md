---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ClickHouse
description: ClickHouse usage with dbVisitor.
---

# ClickHouse

Use JdbcTemplate, Mapper, or the Fluent API to query and insert ClickHouse data. For changes to MergeTree tables, see the mutation example below.

| Scenario | Usage |
| --- | --- |
| Key generation | Application-generated IDs |
| Pagination | LIMIT pagination |
| Insert conflicts | Duplicate records in MergeTree |
| Data changes | Submit, wait for, and inspect mutations |

- [Type Support](./types.md): Choose Java property types.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Duplicate Records](./conflict.mdx): Read the latest version of a repeated ID.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Batch Inserts](./batch-insert.mdx): Submit multiple events together.
- [Waiting for Data Changes](./write.mdx): Wait for changes and inspect failures.
