---
id: vectors
sidebar_position: 4
title: 向量值
---

## 向量字段类型

| SQL 类型 | 说明 |
| --- | --- |
| `FLOAT_VECTOR(dim)` | 读写与搜索支持数值 List 和一维数值基本类型数组；元素转换为 Float。 |
| `BINARY_VECTOR(dim)` | 按位打包的二进制向量；支持读写、KNN 和范围搜索。 |
| `FLOAT16_VECTOR(dim)` | 半精度浮点向量；支持读写、KNN 和范围搜索。 |
| `BFLOAT16_VECTOR(dim)` | BFloat16 向量；支持读写、KNN 和范围搜索。 |
| `SPARSE_FLOAT_VECTOR` | 稀疏浮点向量，无需维度；旧 `(dim)` 仅作兼容解析。 |
| `INT8_VECTOR(dim)` | 每维一个 -128～127 的有符号整数，读写使用字节编码，支持 KNN/范围和 Hybrid 搜索；使用 HNSW 索引，不自动量化浮点向量。 |

## 向量数据格式支持

在 `INSERT`, `SEARCH` (SELECT ... ORDER BY vector), `DELETE` 等包含向量操作的语句中，支持多种向量表达形式：

1. **SQL 数组字面量**:
   - `[0.1, 0.2]`
2. **JDBC 参数绑定**:
   - `?` (PreparedStatement)
   - FloatVector 支持数值列表 `List<? extends Number>`，例如 `List<Byte>`、`List<Short>`、`List<Integer>`、`List<Long>`、`List<Float>`、`List<Double>`。
   - 支持六种一维数值基本类型数组：`byte[]`、`short[]`、`int[]`、`long[]`、`float[]`、`double[]`。使用 `PreparedStatement.setObject(index, vector)` 绑定。
   - 适用于 FloatVector 的 INSERT/UPSERT，以及 SELECT、UPDATE、DELETE 的 KNN 和范围向量条件（`vector_range` 或距离比较表达式）。UPDATE 的 SET 字段通过原生 Partial Update 发送。
   - 向量查询和 INSERT/UPSERT 将各元素转换为 Float；较大的整数和 double 可能损失精度。`byte[]` 按有符号数值逐元素转换，不表示 BinaryVector 的位数据。
   - 不支持将 `boolean[]`、`char[]`、包装类型数组（例如 `Float[]`）或多维 Java 数组作为单个 FloatVector。维度必须与字段定义匹配。
3. **单查询向量限制**:
   - `ORDER BY vector_col <-> ?` 只接受一个查询向量，例如 `[1, 1]`；有无 LIMIT 均遵守此规则。
   - 嵌套向量列表（例如 `[[1, 1], [99, 99]]`，包括只包装一个向量的 `[[1, 1]]`）会报参数错误。该限制也适用于 UPDATE/DELETE 的距离排序和向量范围条件。
   - Milvus SDK 的多查询向量批量搜索不通过此 ORDER BY 语法提供；它也不等同于 JDBC `addBatch`/`executeBatch`。SELECT 仍返回一个结果集，输出字段由 SELECT 指定。
