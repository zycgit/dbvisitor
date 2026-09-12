---
id: compatibility
sidebar_position: 21
title: Versions and Supported Scope
---

## Versions

| Component | Version |
| --- | --- |
| Java | 17+ |
| Milvus Java SDK | 2.6.22, V2 API |
| Milvus server | 2.6.x, minimum 2.6.2 |

Versions before 2.6.2 are not supported. Milvus 3.0 is outside this support range.

Some statements require newer server versions: nullable vectors require 2.6.18+, and TRUNCATE requires 2.6.11+. See the relevant statement pages for feature-specific requirements.

## Supported Scope

SQL commands cover entity reads and writes, vector and hybrid searches, schema and index management, import jobs, and administration. Each [statement page](about.md) lists its syntax and corresponding SDK methods; Import uses the official REST API.

For endpoints, authentication, TLS and Zilliz Cloud, see [Connecting](../../drivers/milvus/connection.mdx). For JDBC interface support, see [Usage Limits](../../drivers/milvus/limitations.md).
