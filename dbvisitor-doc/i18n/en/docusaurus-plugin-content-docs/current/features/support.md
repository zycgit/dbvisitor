---
id: support
sidebar_position: 1
hide_table_of_contents: true
title: Data Source Support Matrix
description: Compares built-in dialect and JDBC adapter capabilities across data sources.
---

# Data Source Support Matrix

These matrices compare dbVisitor integration capabilities, not the native feature sets of databases. Select a data source for its usage, execution behavior and restrictions.

## Built-in Dialect Capabilities {#dialect}

✅ Supported dialect capability · ❌ No corresponding dialect capability · — No additional write-conflict strategy · ¹ Requires mapped primary-key columns.

| Column | Meaning |
| --- | --- |
| Config Key | Built-in dialect alias |
| Builder API | A built-in dialect for LambdaTemplate / BaseMapper; individual methods may have restrictions |
| Pagination | Generates pagination SQL or native-command options; does not imply cursor traversal or snapshot isolation |
| Write Conflicts | Additional Ignore / Update strategies; matching and write semantics depend on the data source |
| Sequence | Sequence access through SeqSqlDialect |
| Vector | Vector ordering and range conditions through VectorSqlDialect |
| Null Ordering | Translation of OrderNullsStrategy |

| Data Source | Config Key | Builder API | Pagination | Write Conflicts | Sequence | Vector | Null Ordering |
|-------|--------|:--------:|:--:|------|:--:|:--:|:----:|
| [MySQL](mysql/about.md) | mysql  | ✅ | ✅ | Ignore Update | ❌ | ❌ | ✅ |
| MariaDB | mariadb | ✅ | ✅ | Ignore Update | ❌ | ❌ | ✅ |
| [PostgreSQL](postgresql/about.md) | postgresql | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | ❌ |
| KingbaseES | kingbase | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | ❌ |
| [Oracle](oracle/about.md) | oracle | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [Dameng](dm/about.md) | dm     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [SQL Server](mssql/about.md) | sqlserver | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| SQL Server (jTDS) | jtds   | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [DB2](db2/about.md) | db2    | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [H2](h2/about.md) | h2     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| Apache Derby | derby  | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| HSQL | hsql   | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| Hive | hive   | ✅ | ❌ | — | ❌ | ❌ | ❌ |
| Apache Impala | impala | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| IBM Informix | informix | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| SQLite | sqlite | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| Xugu | xugu   | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [ClickHouse](clickhouse/about.md) | clickhouse | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [Redis](redis/about.md) | —      | ❌ | ❌ | — | ❌ | ❌ | ❌ |
| [MongoDB](mongo/about.md) | mongo  | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [ElasticSearch 6](elastic/about.md) | elastic6 | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [ElasticSearch 7](elastic/about.md) | elastic7 | ✅ | ✅ | — | ❌ | ✅ | ❌ |
| [ElasticSearch 8](elastic/about.md) | elastic8 | ✅ | ✅ | — | ❌ | ✅ | ❌ |
| [Milvus](milvus/about.md) | milvus | ✅ | ✅ | Update¹ | ❌ | ✅ | ❌ |

## JDBC Adapter Capabilities

This matrix covers dbVisitor-provided adapters. Relational JDBC capabilities depend on the selected vendor driver, server version and statement. JDBC Batch means addBatch / executeBatch, not multi-statement execution or an upper-level API executing items individually.

| Adapter | JDBC Batch | Stored Procedures | getGeneratedKeys | Named / Indexed Generated-Key Overloads |
| --- | :---: | :---: | :---: | :---: |
| [jdbc-redis](redis/dbvisitor/results.mdx) | ❌ | ❌ | ❌ | ❌ |
| [jdbc-mongo](mongo/dbvisitor/results.mdx) | ❌ | ❌ | ✅¹ | ❌ |
| [jdbc-elastic](elastic/dbvisitor/results.mdx) | ❌ | ❌ | ✅¹ | ❌ |
| [jdbc-milvus](../drivers/milvus/limitations.md) | ❌ | ❌ | ✅¹ | ❌ |

¹ Requires Statement.RETURN_GENERATED_KEYS and a supported write command; see the data source documentation for returned columns and Mapper backfill configuration.
