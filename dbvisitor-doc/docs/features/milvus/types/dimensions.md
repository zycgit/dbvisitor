---
id: dimensions
sidebar_position: 2
title: 长度、容量与维度
---

## 长度、容量与维度

`VARCHAR(n)` 的 n 是 UTF-8 字节上限，不是 Java 字符数；包含中文或其他多字节字符时尤其需要按编码长度规划。`ARRAY<element_type>(max_capacity)` 限制元素个数，不是字节数，也不表示每个数组必须填满容量。

向量的 `dim` 含义如下：

| 字段类型 | 维度与输入长度 |
| --- | --- |
| FloatVector | 数值元素个数，必须与 dim 相等 |
| Int8Vector | 每维一个有符号字节，字节数等于 dim |
| BinaryVector | dim 为位数，输入字节数乘 8 必须等于 dim；例如 dim=16 输入两个字节 |
| Float16Vector | 每维两个字节；编码长度为 dim × 2，数值列表长度为 dim |
| BFloat16Vector | 每维两个字节；编码长度为 dim × 2，数值列表长度为 dim |
| SparseFloatVector | 使用索引到权重的映射，不按 Map 条目数解释稠密维度 |

FloatVector 的值必须有限；FP16/BF16 还会检查编码后的值，有限的 Float 输入也可能在半精度转换时溢出而被拒绝。向量维度和字段数的服务端上限仍取决于部署配置。
