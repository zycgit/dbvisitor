---
id: limited
sidebar_position: 1
hide_table_of_contents: true
title: Usage Limits
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
    - xxx(String sql, String[] columnNames) methods
- Unsupported JDBC data types:
    - SQLXML, REF_CURSOR, RowId, Ref, Struct, DISTINCT
- JDBC addBatch, clearBatch and executeBatch are not supported. Multiple statements and multi-row writes in a single command are not JDBC Batch.
- Savepoint operations are not supported
- Array, Blob, Clob, and NClob type data is pre-read into memory; please be aware of data size
