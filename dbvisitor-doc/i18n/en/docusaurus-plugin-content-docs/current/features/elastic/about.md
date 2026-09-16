---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Elasticsearch
---

Use REST requests with JdbcTemplate and Mappers. The builder API also supports basic document operations. See [JDBC Elasticsearch](../../drivers/elastic/connection.mdx) for connections.

## Using dbVisitor

- [Programmatic API](/docs/features/elastic/programmatic): queries, writes, multiple results, and stored routine differences.
- [Mapper API](dbvisitor/mapper.md): annotations, Mapper reads and writes, and execution differences.
- [Builder API](dbvisitor/builder.md): Supported operations, datasource-specific behavior, and usage.
- [Query Operations](dbvisitor/query.mdx): Execute queries, combine filters, and map results.
- [Data Writes](dbvisitor/write.mdx): Insert, update, delete, and handle write results.
- [Pagination](dbvisitor/pagination.mdx): Fetch a range and obtain totals.
- [Vector Operations](dbvisitor/vectors.mdx): Configure vector fields and similarity searches.
- [Name Sensitivity](dbvisitor/name-sensitivity.md): Configure index names, field names, and result column matching.
- [Key Generation](dbvisitor/generated-keys.mdx): Assign identifiers or read generated IDs.
- [Parameters and Rules](dbvisitor/parameters.md): Bind values and select native command fragments.
- [Type Support](dbvisitor/types.md): Choose Java types for stored values.
- [Reading Results](dbvisitor/results.mdx): Read result columns and map returned values.
- [Transaction Support](dbvisitor/transactions.md): Transaction API behavior and isolation-level settings.

## Command Basics

[Request Format and Parameters](basics/requests.md) · [Hint Support](basics/hints.md) · [Command Results](basics/results.md)

## Query Commands

[Search and Count](query/search.md) · [Multi-search and Multi-get](query/multiple.md) · [Document Source and Query Explanation](query/document.md)

## Write Commands

[Document Writes and Deletes](write/documents.md) · [Update and Delete by Query](write/by-query.md) · [Write Results](write/results.md)

## Management Commands

[Indexes and Mappings](admin/indexes.md) · [Cluster Information](admin/cluster.md)
