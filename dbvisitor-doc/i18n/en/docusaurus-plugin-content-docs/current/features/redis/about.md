---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Redis
---

Use Redis commands with JdbcTemplate and Mappers. See [JDBC Redis](../../drivers/redis/connection.mdx) for connections.

## Using dbVisitor

- [Programmatic API](/docs/features/redis/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](dbvisitor/mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](dbvisitor/builder.md): Supported operations, datasource-specific behavior, and usage.
- [Query Operations](dbvisitor/query.mdx): Execute queries, combine filters, and map results.
- [Data Writes](dbvisitor/write.mdx): Insert, update, delete, and handle write results.
- [Pagination](dbvisitor/pagination.mdx): Fetch a range and obtain totals.
- [Keys and Identifiers](dbvisitor/generated-keys.mdx): Choose Redis keys and generate identifiers.
- [Parameters and Rules](dbvisitor/parameters.md): Bind values and select native command fragments.
- [Type Support](dbvisitor/types.md): Choose Java types for stored values.
- [Reading Results](dbvisitor/results.mdx): Read result columns and map returned values.
- [Result Handling](dbvisitor/result-handling.md): Check availability of the three handlers across API entry points.
- [Transaction Support](dbvisitor/transactions.md): Transaction API behavior and isolation-level settings.

## Business Scenarios

[Product Cache](scenarios/product-cache.md) · [Login State](scenarios/login-state.md) · [Shopping Cart](scenarios/shopping-cart.md)<br />
[View Counter](scenarios/view-counter.md) · [User Likes](scenarios/article-likes.md) · [Points Ranking](scenarios/points-ranking.md)

## Command Basics

[Command Format and Parameters](basics/commands.md) · [Command Results](basics/results.md)

## Command Reference

[String](string/about.md) · [Hash](hash/about.md) · [List](list/about.md) · [Set](set/about.md) · [Sorted Set](sorted-set/about.md)<br />
[Key Management](keys/about.md) · [Server Commands](server/about.md)
