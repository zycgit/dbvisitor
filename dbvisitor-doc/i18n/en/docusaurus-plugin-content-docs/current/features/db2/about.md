---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: DB2
description: DB2 usage with dbVisitor.
---

# DB2

Use JdbcTemplate, method annotations, Mapper files, or the Builder API with DB2. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY and sequences |
| Pagination | Row-number pagination |
| Insert conflicts | MERGE |
| Data backfill | Return written fields with FINAL TABLE |

- [Programmatic API](/docs/features/db2/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](builder.md): Supported operations, datasource-specific behavior, and usage.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Data Backfill](./backfill.mdx): Read values returned by a write.
- [Type Support](./types.md): Choose Java property types.
- [Transaction Support](./transactions.mdx): Roll back multiple writes on failure.
