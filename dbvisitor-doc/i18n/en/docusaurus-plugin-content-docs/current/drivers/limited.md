---
id: limited
sidebar_position: 1
hide_table_of_contents: true
title: Adapter Limitations
description: Usage limits of JDBC adapters based on dbvisitor-driver regarding JDBC interface support.
---
JDBC adapters based on dbvisitor-driver have the following usage limits regarding JDBC interface support:

- DatabaseMetaData provides partial metadata and capability information, not a complete relational schema discovery API. ORM, BI and migration tools that model databases from metadata need a separate compatibility assessment.
- When using resultSetType, resultSetConcurrency, resultSetHoldability, and fetchDirection parameters, only the following default values are supported:
    - resultSetType = TYPE_FORWARD_ONLY
    - resultSetConcurrency = CONCUR_READ_ONLY
    - resultSetHoldability = HOLD_CURSORS_OVER_COMMIT
    - fetchDirection = FETCH_FORWARD
- When using ResultSet:
    - The ResultSet.update/insert/deleteXXX series of methods are not supported.
- When using Statement and PreparedStatement interfaces, overloaded methods with the following parameters are not supported:
    - xxx(String sql, int[] columnIndexes) methods
- `String[] columnNames` selects generated-key columns actually returned by the adapter, not arbitrary fields. Requesting an unavailable column fails when reading generated keys; the write may already have completed.
- Unsupported JDBC data types:
    - SQLXML, REF_CURSOR, RowId, Ref, Struct, DISTINCT
- JDBC addBatch, clearBatch and executeBatch are not supported. Multiple statements and multi-row writes in a single command are not JDBC Batch.
- Savepoint operations are not supported
- Array, Blob, Clob, and NClob type data is pre-read into memory; please be aware of data size

## Databases, tables and columns

Use `connection.getMetaData()`. An empty result below still has the standard JDBC column layout.

| Driver | `getCatalogs()` | `getTables()` | `getColumns()` |
| --- | --- | --- | --- |
| Milvus | The connected database | Collections in the current database | Fields declared in the collection schema |
| MongoDB | Databases the current user can list | Collections and views | Empty; documents are not sampled to infer fields |
| Elasticsearch | Empty; no separate database namespace | Indices | Fields declared in mappings |
| Redis | The selected database number, such as `0` | Empty; keys are not tables | Empty |

These four drivers do not expose a separate schema namespace; `getSchemas()` returns an empty result. `getTableTypes()` lists the table kinds the driver exposes; Redis returns an empty result.

`catalog` matches an exact name. Name patterns use `%` for any string, `_` for one character and `\` for escaping. A `null` filter imposes no restriction; an empty string selects objects without that catalog or schema namespace. Unknown details such as field precision are not guessed. Permission and connection errors are reported as exceptions, not empty results.
