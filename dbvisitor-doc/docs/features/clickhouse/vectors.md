---
id: vectors
sidebar_position: 50
title: 向量操作
---

## 向量类型映射 {#vector-mapping}

向量保存为 `Array(Float32)`，实体使用 `List<Float>` 并配置 `ChVectorTypeHandler`，配置见[向量类型处理器](../../guides/types/vector-handler.md)。向量不能为 NULL，写入值与查询向量的维度应一致。

## KNN 近邻排序 {#knn-ordering}

构造器 API 生成距离函数排序，并通过 `initPage(k, 0)` 限制返回条数；不会自动创建向量索引。

目前接入 L2、余弦和内积排序，未接入 Hamming、Jaccard 和 BM25。

| 方法 | 排序表达式 |
| --- | --- |
| `orderByL2` | `L2Distance(embedding, ?)` |
| `orderByCosine` | `cosineDistance(embedding, ?)` |
| `orderByIP` | `-dotProduct(embedding, ?)` |

默认升序返回距离最小的记录；内积取负数，因此内积大的记录排在前面。

## 距离范围过滤 {#range-filters}

`vectorByL2`、`vectorByCosine`、`vectorByIP` 使用上表表达式并比较 `< threshold`。余弦相似度大于 `0.8` 对应距离阈值 `0.2`；内积大于 `0.8` 对应负内积阈值 `-0.8`。

Hamming、Jaccard 和 BM25 尚不能通过构造器 API 进行范围过滤。

## 组合查询 {#vector-scalar}

标量条件生成普通 WHERE 条件，可以与向量范围过滤组合，再按距离排序。请先设置条件，再调用排序方法；示例见[组合条件](../../guides/core/vector_query/combined.md)。
