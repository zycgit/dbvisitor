---
id: range
sidebar_position: 4
title: 距离范围过滤
description: 使用 vectorByL2、vectorByCosine、vectorByIP 等方法按向量距离阈值筛选记录。
---

# 距离范围过滤

距离范围过滤用于按距离阈值筛选记录。下面的完整范围查询示例以 PostgreSQL pgvector 为背景；Milvus 范围搜索由 ANN 搜索实现，不能据此承诺穷尽全部实体，且不同度量的阈值方向和组合限制不同，参阅 [Milvus 向量查询](../../../features/milvus/dbvisitor/usage.mdx#vector-query)。dbVisitor 使用 `vectorBy*` 生成向量距离条件，该条件位于 SQL 的 `WHERE` 部分。

## 适合场景

- 需要找出所有满足相似度阈值的记录。
- 返回数量由阈值和数据分布决定。
- 向量条件需要和普通字段条件一起作为过滤条件。

## 不适合场景

- 需要固定返回最相似的 N 条记录；应使用 [KNN 近邻排序](knn)。
- 阈值难以确定，只希望拿到排序后的候选集。
- 数据源不支持向量范围过滤 SQL。

## 范围过滤模式

```text title='范围过滤'
全部记录或候选记录
        |
        | 计算 embedding 与查询向量的距离
        v
保留距离 < 阈值的记录
        |
        v
返回全部命中记录
```

`vectorBy*` 本身就是查询条件。它可以和 `eq`、`likeRight`、`gt` 等普通条件共同组成 `WHERE`。

## 基本用法

```java title='L2 距离过滤'
List<Float> target = Arrays.asList(0.1f, 0.2f, 0.3f);

List<ProductVector> rows = lambda.query(ProductVector.class)
        .vectorByL2(ProductVector::getEmbedding, target, 5.0)
        .queryForList();
```

pgvector 下生成的 SQL 形态类似：

```sql
SELECT * FROM product_vector
WHERE embedding <-> ? < ?
```

`vectorBy*` 的向量参数会经过实体映射中的 TypeHandler，因此通常可以直接传 `List<Float>`。

## 可用过滤方法

| 目标 | 方法 | 说明 |
| --- | --- | --- |
| L2 欧氏距离 | `vectorByL2` | 距离越小越相似。 |
| Cosine 余弦距离 | `vectorByCosine` | 常用于文本语义向量。 |
| IP 内积距离 | `vectorByIP` | 常用于推荐和排序场景。 |
| Hamming 距离 | `vectorByHamming` | 常用于二进制向量。 |
| Jaccard 距离 | `vectorByJaccard` | 常用于集合相似度。 |
| BM25 | `vectorByBM25` | 适用于支持 BM25 的数据源能力。 |

## 动态启用过滤

所有 `vectorBy*` 方法都支持第一个 `boolean` 参数，用于控制条件是否进入 SQL。

```java title='按条件启用向量过滤'
boolean enableVectorFilter = request.hasVector();
List<Float> target = request.getVector();

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, request.getCategory())
        .vectorByL2(enableVectorFilter, ProductVector::getEmbedding, target, 5.0)
        .queryForList();
```

当 `enableVectorFilter` 为 `false` 时，向量距离条件不会进入 SQL。

## 设置阈值

阈值不是 dbVisitor 计算出来的固定值，而是业务根据向量模型、距离度量和数据分布确定的条件。常见做法是先离线观察距离分布，再选择能够覆盖目标召回率的阈值。

```text title='阈值影响'
阈值较小 -> 命中更少，结果更接近
阈值较大 -> 命中更多，结果更宽松
```

不同距离度量的取值含义不同，不能直接复用同一个阈值。

## 深入阅读

- [向量类型映射](mapping) — TypeHandler 和查询参数准备。
- [KNN 近邻排序](knn) — 返回固定数量的近邻结果。
- [组合查询](combined) — 普通条件与向量范围过滤组合。
