---
id: binding
sidebar_position: 5
title: JSON、ARRAY 与向量绑定
---

## 类型绑定与 schema

Java 输入类型与绑定示例见[参数绑定](../../../drivers/milvus/parameters.mdx#typed-values)。Binary 使用 byte[]/ByteBuffer/字节 List；FP16/BF16 数值输入经官方 Float16Utils 编码，byte[]/ByteBuffer 是小端原始编码；Sparse 使用非空 Map&lt;Number,Number>，索引为 [0,4294967295) 内的整数，权重有限。ByteBuffer 只消费 remaining，不改变其位置。同一个 byte[] 的含义由目标字段 schema 决定：FloatVector 按有符号数值逐元素转换，Binary/FP16/BF16 按打包编码解释，Int8Vector 则是一维一个有符号字节。

```sql
CREATE TABLE types_demo (
    id INT64 PRIMARY KEY AUTO_ID,
    tags ARRAY<VARCHAR(30)>(8) NULL,
    flags ARRAY<BOOL>(8),
    bits BINARY_VECTOR(16),
    half FLOAT16_VECTOR(2),
    brain BFLOAT16_VECTOR(2),
    sparse SPARSE_FLOAT_VECTOR
);
INSERT INTO types_demo (tags,flags,bits,half,brain,sparse) VALUES (?, ?, ?, ?, ?, ?);
SELECT id,bits FROM types_demo ORDER BY bits ~= ? LIMIT 10;
SELECT id,score FROM types_demo WHERE bits <%> ? < 0.5 LIMIT 10;
SELECT id,half FROM types_demo ORDER BY half <=> ? LIMIT 10;
SELECT id,brain FROM types_demo ORDER BY brain <-> ? LIMIT 10;
SELECT id,sparse FROM types_demo ORDER BY sparse <#> ? LIMIT 10;
SELECT id,tags FROM types_demo WHERE tags IS NOT NULL LIMIT 10;
```

ARRAY 的容量、元素类型及 VARCHAR 字节长度写入 SDK schema；读取通过 getArray 得到实际元素 JDBC 类型。数组本身可 NULL 或空数组，元素不可 NULL。nullable 由 isNullable 报告；SHOW TABLE 追加 NULLABLE、ELEMENT_TYPE、MAX_CAPACITY、MAX_LENGTH 列，原有列顺序不变。SHOW CREATE 保留这些定义。向量 NULL 从 Milvus 2.6.18 起可用，且不能用向量 IS NULL 过滤，见[官方版本说明](https://github.com/milvus-io/milvus/releases/tag/v2.6.18)与[nullable 文档](https://milvus.io/docs/v2.6.x/nullable-and-default.md)。
