---
id: limited
sidebar_position: 1
hide_table_of_contents: true
title: 使用限制
description: 基于 dbvisitor-driver 的 JDBC 适配器在 JDBC 接口支持上的使用限制。
---

基于 dbvisitor-driver 的 JDBC 适配器在 JDBC 接口支持上有如下使用限制：

- 在使用 DatabaseMetaData 接口时获取的属性不可信。
- 在使用 resultSetType、resultSetConcurrency、resultSetHoldability、fetchDirection 参数时只能选择如下默认值：
    - resultSetType = TYPE_FORWARD_ONLY
    - resultSetConcurrency = CONCUR_READ_ONLY
    - resultSetHoldability = HOLD_CURSORS_OVER_COMMIT
    - fetchDirection = FETCH_FORWARD
- 在使用 ResultSet 时结果集时
    - 不支持如 ResultSet.update/insert/deleteXXX 系列方法。
- 在使用 Statement、PreparedStatement 接口时不支持如下参数的重载方法
    - xxx(String sql, int[] columnIndexes) 方法
    - xxx(String sql, String[] columnNames) 方法
- 不支持 的 JDBC 数据类型有
    - SQLXML、REF_CURSOR、RowId、Ref、Struct、DISTINCT
- 不支持 addBatch、clearBatch 批量化操作
- 不支持 savepoint 操作
- Array、Blob、Clob、NClob 类型的数据会预先读进内存，请注意数据大小
