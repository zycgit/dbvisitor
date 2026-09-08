## 限制

不支持的类型有：

- SQLXML、REF_CURSOR、RowId、Ref、Struct、DISTINCT
- Array、Blob、Clob、NClob 类型的数据会预先读进内存，请注意数据大小。

不支持的特性有：

- savepoint
- resultSetType、resultSetConcurrency、resultSetHoldability、fetchDirection 只能选择如下默认值：
    - resultSetType = TYPE_FORWARD_ONLY
    - resultSetConcurrency = CONCUR_READ_ONLY
    - resultSetHoldability = HOLD_CURSORS_OVER_COMMIT
    - fetchDirection = FETCH_FORWARD
- prepareStatement(String sql, int[] columnIndexes)
- prepareStatement(String sql, String[] columnNames)
- DatabaseMetaData 接口中获取的属性不可信。
- CursorName 不支持

## 已实现接口的行为

- `PreparedStatement` 的绑定值在执行后保留；重新绑定会替换对应值，`clearParameters()` 显式清除绑定。每次请求持有独立的参数描述快照。
- `setObject` 支持按序号和 CallableStatement 按名称传入 `SQLType`。显式指定 DECIMAL/NUMERIC 的 scale 时，在绑定阶段按 `HALF_UP` 舍入；非法转换抛出 `SQLException`，不覆盖此前绑定。
- `setAsciiStream()` 按 US-ASCII 解码并绑定为字符串，不作为二进制参数下发；带长度的重载限制读取的字符数。`setTime(..., Calendar)` 使用指定时区转换，不调用 `java.sql.Time.toInstant()`。
- CallableStatement 的 `getLong()` 按序号、名称读取均保留 64 位数值；SQL NULL 返回 `0`，并通过 `wasNull()` 区分。
- `getParameterMetaData()` 当前基于绑定信息，不提供 SQL 占位符预解析，不能用未绑定时的返回值推断语句的占位符数量。
- 多结果通过 `getResultSet()`、`getUpdateCount()` 和 `getMoreResults()` 访问。下一项为更新行数或结果耗尽时，`getMoreResults()` 返回 `false`；只有同时满足 `getUpdateCount() == -1` 才表示结果耗尽。
- 支持 `CLOSE_CURRENT_RESULT`、`KEEP_CURRENT_RESULT`、`CLOSE_ALL_RESULTS`。Statement 重新执行或关闭、Connection 关闭时，会释放其关联结果集。
- `closeOnCompletion()` 在依赖结果集均关闭且没有尚未处理的结果时关闭 Statement，不会在 `executeQuery()` 返回前关闭它，也不会丢弃后续更新行数或错误。已读取更新行数时，关闭最后一个主键结果集仍可触发自动关闭。
- `getGeneratedKeys()` 在没有主键结果时返回空 ResultSet；是否实际生成和返回主键仍取决于具体适配器，不代表所有数据源支持主键生成。
- `ResultSet` 按列名读取时忽略大小写；同名列选择第一列，按下标读取仍区分各列。位置查询可能预读下一行，并保留当前行用于读取，不提供滚动结果集。
- `Year`、`YearMonth` 和 `Month` 的日期/Calendar 转换使用实际年份与月份，不应用旧 Date API 的 1900 年偏移或零起始月份。
- `unwrap()` 和 `isWrapperFor()` 按实际对象类型判断；不能解包时分别抛出 `SQLException` 和返回 `false`，不会把普通 Statement 误判成 PreparedStatement。
- `Array.getArray()` 返回 Java 数组，不再返回 List；切片下标从 1 开始。Array、Blob、Clob/NClob 在 `free()` 后不可继续访问；释放后关闭已打开的 LOB 写入流不会恢复数据。LOB 的非法范围抛出 `SQLException`，不泄漏数组或字符串越界异常。
- 适配器使用 `AdapterResultCursor` 时必须通过 `pushFinish()` 标记完成；临时无数据会等待后续数据、完成或关闭，不再被当作结果耗尽。

上述修正不增加 batch、savepoint、非默认结果集模式、完整 DatabaseMetaData 或新的数据库端能力。
