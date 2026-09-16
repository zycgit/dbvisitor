---
id: limited
sidebar_position: 1
hide_table_of_contents: true
title: 3. 适配器限制
description: 基于 dbvisitor-driver 的 JDBC 适配器在 JDBC 接口支持上的使用限制。
---

基于 dbvisitor-driver 的 JDBC 适配器在 JDBC 接口支持上有如下使用限制：

- DatabaseMetaData 只提供部分元数据和能力描述，不提供完整的关系型数据库 schema 查询能力。依赖元数据自动建模的 ORM、BI 或迁移工具需要单独确认兼容性。
- 在使用 resultSetType、resultSetConcurrency、resultSetHoldability、fetchDirection 参数时只能选择如下默认值：
    - resultSetType = TYPE_FORWARD_ONLY
    - resultSetConcurrency = CONCUR_READ_ONLY
    - resultSetHoldability = HOLD_CURSORS_OVER_COMMIT
    - fetchDirection = FETCH_FORWARD
- 在使用 ResultSet 时
    - 不支持如 ResultSet.update/insert/deleteXXX 系列方法。
- 在使用 Statement、PreparedStatement 接口时不支持如下参数的重载方法
    - xxx(String sql, int[] columnIndexes) 方法
- 支持按 `String[] columnNames` 选择适配器实际返回的生成键列；不能用它回填任意普通字段。未返回的列会在读取生成键时报错，写入可能已经完成。
- 不支持的 JDBC 数据类型有
    - SQLXML、REF_CURSOR、RowId、Ref、Struct、DISTINCT
- 不支持 JDBC addBatch、clearBatch、executeBatch 批量操作。多语句执行和单条命令的多行写入不是 JDBC Batch。
- 不支持 savepoint 操作
- Array、Blob、Clob、NClob 类型的数据会预先读进内存，请注意数据大小

## 元信息接口约定

支持查询哪些对象，由具体驱动决定，详见各驱动的介绍页。空结果仍保留标准 JDBC 列结构。

`catalog` 按名称匹配；名称模式中的 `%` 匹配任意字符串，`_` 匹配一个字符，`\` 用于转义。传入 `null` 不限制该项，空字符串表示没有该库或 schema 层级。字段精度等信息未知时不填推测值；权限或连接错误会抛出异常，不作为空结果处理。
