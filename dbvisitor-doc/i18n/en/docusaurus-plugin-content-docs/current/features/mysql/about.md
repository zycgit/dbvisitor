---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MySQL
description: MySQL usage with dbVisitor.
---

# MySQL

Use JdbcTemplate, method annotations, Mapper files, or the Builder API with MySQL. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | AUTO_INCREMENT backfill |
| Pagination | LIMIT pagination |
| Insert conflicts | INSERT IGNORE / ON DUPLICATE KEY UPDATE |

- [Programmatic API](/docs/features/mysql/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](builder.md): Supported operations, datasource-specific behavior, and usage.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Type Support](./types.md): Choose Java property types.
- [Transaction Support](./transactions.mdx): Roll back multiple writes on failure.
