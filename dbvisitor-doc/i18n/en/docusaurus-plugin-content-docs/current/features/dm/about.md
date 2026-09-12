---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: DM
description: DM usage with dbVisitor.
---

# DM

Use JdbcTemplate, method annotations, Mapper files, or the Fluent API with DM. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | Identity columns and sequences |
| Pagination | LIMIT pagination |
| Insert conflicts | Duplicate-key ignore and MERGE update |

- [Type Support](./types.md): Choose Java property types.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Multiple-Write Consistency](./write.mdx): Roll back multiple writes on failure.
