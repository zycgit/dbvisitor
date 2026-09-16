---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: PostgreSQL
description: PostgreSQL usage with dbVisitor.
---

# PostgreSQL

Use JdbcTemplate, method annotations, Mapper files, or the Builder API with PostgreSQL. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY, SERIAL, and sequences |
| Pagination | LIMIT / OFFSET |
| Insert conflicts | ON CONFLICT |
| Data backfill | Return written fields with RETURNING |
| Vector operations | pgvector field mapping, distance thresholds, and indexes |

- [Mapper API](mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](builder.md): Supported operations, datasource-specific behavior, and usage.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Vector Operations](./vectors.mdx): Configure vector fields, distance thresholds, and search indexes.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Data Backfill](./backfill.mdx): Read values returned by a write.
- [Type Support](./types.md): Choose Java property types.
- [Transaction Support](./transactions.mdx): Roll back multiple writes on failure.
