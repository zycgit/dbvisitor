---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: PostgreSQL
description: PostgreSQL usage with dbVisitor.
---

# PostgreSQL

Use JdbcTemplate, method annotations, Mapper files, or the Fluent API with PostgreSQL. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY, SERIAL, and sequences |
| Pagination | LIMIT / OFFSET |
| Insert conflicts | ON CONFLICT |
| Data backfill | Return written fields with RETURNING |
| Vector operations | pgvector field mapping, distance thresholds, and indexes |

- [Type Support](./types.md): Choose Java property types.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Data Backfill](./backfill.mdx): Read values returned by a write.
- [Vector Operations](./vectors.mdx): Configure vector fields, distance thresholds, and search indexes.
- [Multiple-Write Consistency](./write.mdx): Roll back multiple writes on failure.
