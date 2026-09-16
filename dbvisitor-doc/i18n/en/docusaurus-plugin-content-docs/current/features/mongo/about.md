---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MongoDB
---

Use MongoDB commands with JdbcTemplate and Mappers. The builder API also supports basic document operations. See [JDBC MongoDB](../../drivers/mongo/connection.mdx) for connections.

## Using dbVisitor

- [Programmatic API](/docs/features/mongo/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](dbvisitor/mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](dbvisitor/builder.md): Supported operations, datasource-specific behavior, and usage.
- [Query Operations](dbvisitor/query.mdx): Execute queries, combine filters, and map results.
- [Data Writes](dbvisitor/write.mdx): Insert, update, delete, and handle write results.
- [Pagination](dbvisitor/pagination.mdx): Fetch a range and obtain totals.
- [Vector Operations](dbvisitor/vectors.md): Field mapping, search metrics, and supported queries.
- [Key Generation](dbvisitor/generated-keys.mdx): Assign identifiers or read generated IDs.
- [Parameters and Rules](dbvisitor/parameters.md): Bind values and select native command fragments.
- [Type Support](dbvisitor/types.md): Choose Java types for stored values.
- [Reading Results](dbvisitor/results.mdx): Read result columns and map returned values.
- [Transaction Support](dbvisitor/transactions.md): Transaction API behavior and isolation-level settings.

## Command Basics

[Command Format and Parameters](basics/commands.md) · [Hint Support](basics/hints.md)

## Query Commands

[Document Queries](query/find.md) · [Count and Distinct](query/count.md) · [Aggregation Pipelines](query/aggregate.mdx)

## Write Commands

[Insert Documents](write/insert.md) · [Update and Replace](write/update.md) · [Delete Documents](write/delete.md) · [Bulk Write](write/bulk.md)

## Management Commands

[Collections and Views](admin/collections.md) · [Database Management](admin/databases.md) · [Index Management](admin/indexes.md)<br />
[User Management](admin/users.md) · [USE and SHOW](admin/other.md) · [Database Commands](admin/run-command.md)
