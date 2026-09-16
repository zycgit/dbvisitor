---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: SQL Server
description: SQL Server usage with dbVisitor.
---

# SQL Server

Use JdbcTemplate, method annotations, Mapper files, or the Builder API with SQL Server. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY; read sequences before insertion |
| Pagination | ROW_NUMBER() pagination |
| Insert conflicts | Insert or update with MERGE |
| Data backfill | Return written fields with OUTPUT |

- [Programmatic API](/docs/features/mssql/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](builder.md): Supported operations, datasource-specific behavior, and usage.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Cross-Database Table Mapping](./mapping.mdx): Specify a database and schema.
- [Name Sensitivity](./name-sensitivity.md): Distinguish database name rules from result column matching.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Data Backfill](./backfill.mdx): Read values returned by a write.
- [Type Support](./types.md): Choose Java property types.
- [Transaction Support](./transactions.mdx): Roll back multiple writes on failure.
