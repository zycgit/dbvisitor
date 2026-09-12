---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: SQL Server
description: SQL Server usage with dbVisitor.
---

# SQL Server

Use JdbcTemplate, method annotations, Mapper files, or the Fluent API with SQL Server. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY; read sequences before insertion |
| Pagination | ROW_NUMBER() pagination |
| Insert conflicts | Insert or update with MERGE |
| Data backfill | Return written fields with OUTPUT |

- [Type Support](./types.md): Choose Java property types.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Data Backfill](./backfill.mdx): Read values returned by a write.
- [Cross-Database Table Mapping](./mapping.mdx): Specify a database and schema.
- [Multiple-Write Consistency](./write.mdx): Roll back multiple writes on failure.
