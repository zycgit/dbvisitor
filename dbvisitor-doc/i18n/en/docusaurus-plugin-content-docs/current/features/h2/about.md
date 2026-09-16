---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: H2
description: H2 usage with dbVisitor.
---

# H2

Use JdbcTemplate, method annotations, Mapper files, or the Builder API with H2. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY and sequences |
| Pagination | LIMIT / OFFSET |
| Insert conflicts | MERGE |

- [Programmatic API](/docs/features/h2/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](builder.md): Supported operations, datasource-specific behavior, and usage.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Type Support](./types.md): Choose Java property types.
- [Transaction Support](./transactions.mdx): Roll back multiple writes on failure.

For an in-memory database, use `jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1`. This keeps its data until the JVM exits. See [Core API](../../guides/overview) for common operations.
