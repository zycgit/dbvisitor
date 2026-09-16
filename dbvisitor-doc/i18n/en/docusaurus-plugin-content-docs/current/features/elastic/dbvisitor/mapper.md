---
id: mapper
slug: /features/elastic/mapper
sidebar_position: 20
title: Mapper API
---

## Method Annotations {#annotations}

Annotations contain an Elasticsearch request method, path, and JSON body. Mapper treats these requests and SQL alike as executable commands. See [Query Operations](query.mdx#query-apis) and [Data Writes](write.mdx#write-apis).

## Mapper Reads and Writes {#operations}

BaseMapper generates index requests from entity mappings, supporting primary-key operations, sample queries, and writes using Map arguments.

Elasticsearch text fields have no `VARCHAR(n)`-style length limit. A Java property's JDBC type cannot make an index reject overlong text; validate business length requirements before writing. See [Data Writes](write.mdx#write-apis) for other write differences.

## Key Strategies {#keys}

Supply `_id` yourself or retrieve a server-generated `_id`. Method annotations use generated-key retrieval with the default source, not `generatedKeySource="resultSet"`. See [Key Generation](generated-keys.mdx#key-source).

## Pagination {#pagination}

Method annotations, BaseMapper, and file calls support pagination using from / size. See [Pagination](pagination.mdx) for total counts and deep pagination.

## Execution Options {#options}

Use the default or `FORWARD_ONLY` result-set type. JDBC `SCROLL_INSENSITIVE` and `SCROLL_SENSITIVE` are unsupported; Elasticsearch scroll search does not provide a scrollable JDBC result set.

Requery or collect results into a List to read them again. See the core API's [Execution Options](../../../guides/core/mapper/annotation_query.mdx#options).
