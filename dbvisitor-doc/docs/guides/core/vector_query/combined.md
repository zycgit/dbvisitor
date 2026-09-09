---
id: combined
sidebar_position: 5
title: 组合查询
description: 将普通字段条件、KNN 排序和距离范围过滤组合成业务查询。
---

# 组合查询

以下示例以 PostgreSQL pgvector 为背景。Milvus 等数据源有不同的度量与组合限制，应阅读相应驱动手册。

组合查询用于把普通字段条件和向量查询放在同一个查询中。常见做法是先用业务字段缩小候选集，再使用 `orderBy*` 做近邻排序，或使用 `vectorBy*` 做距离范围过滤。

## 适合场景

- 只在某个分类、租户、状态或时间范围内做相似性查询。
- 需要同时满足业务过滤条件和向量相似条件。
- 需要在候选集内返回 Top-K 或阈值范围结果。

## 不适合场景

- 需要跨多路召回结果做复杂重排；可使用 [编程式 API](../jdbc/about) 编写完整 SQL。
- 业务过滤条件与向量查询没有关联，分别查询再合并更清晰。
- 需要数据库之外的模型打分、重排或融合。

## 组合模式

```text title='普通条件 + 向量查询'
普通字段条件
tenant_id / category / status / create_time
        |
        v
候选记录集合
        |
        +-- orderBy*  -> 候选集内 Top-K
        |
        +-- vectorBy* -> 候选集内距离阈值过滤
```

普通字段条件始终属于 `WHERE`。`orderBy*` 属于 `ORDER BY`，`vectorBy*` 也属于 `WHERE`。

## 普通条件 + KNN 排序

```java title='分类内 Top-K'
PGobject target = new PGobject();
target.setType("vector");
target.setValue("[0.1,0.2,0.3]");

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, "book")
        .orderByCosine(ProductVector::getEmbedding, target)
        .initPage(10, 0)
        .queryForList();
```

SQL 形态类似：

```sql
SELECT * FROM product_vector
WHERE category = ?
ORDER BY embedding <=> ? ASC
LIMIT 10
```

这种写法适合“在指定范围内找最相似的 N 条”。

## 普通条件 + 距离范围过滤

```java title='分类内范围过滤'
List<Float> target = Arrays.asList(0.1f, 0.2f, 0.3f);

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, "book")
        .vectorByCosine(ProductVector::getEmbedding, target, 0.2)
        .queryForList();
```

SQL 形态类似：

```sql
SELECT * FROM product_vector
WHERE category = ?
  AND embedding <=> ? < ?
```

这种写法适合“在指定范围内找出全部足够相似的记录”。

## 动态组合条件

构造器 API 的条件方法可以按 boolean 参数动态控制是否进入 SQL。下例假定向量字段配置了 `PgVectorTypeHandler`，`request.getVector()` 返回 `List<Float>`；范围条件通过字段的 TypeHandler 绑定参数。

```java title='动态组合'
boolean hasCategory = request.getCategory() != null;
boolean hasVector = request.getVector() != null;

List<ProductVector> rows = lambda.query(ProductVector.class)
        .eq(hasCategory, ProductVector::getCategory, request.getCategory())
        .vectorByL2(hasVector, ProductVector::getEmbedding, request.getVector(), 5.0)
        .queryForList();
```

固定 Top-K 查询也可以动态选择是否追加向量排序：

```java title='动态 KNN'
var query = lambda.query(ProductVector.class)
        .eq(ProductVector::getCategory, "book");
if (target != null) {
    query.orderByL2(ProductVector::getEmbedding, target);
} else {
    query.orderByAsc(ProductVector::getId);
}
List<ProductVector> rows = query.initPage(10, 0).queryForList();
```

## 选择组合方式

| 目标 | 推荐写法 |
| --- | --- |
| 分类内找最相似的 10 条 | `eq(category)` + `orderBy*` + `initPage(10, 0)` |
| 分类内找距离小于阈值的全部记录 | `eq(category)` + `vectorBy*` |
| 查询条件可选 | 使用带 `boolean` 参数的条件方法 |
| 需要复杂重排 | 使用编程式 API 写完整 SQL |

## 深入阅读

- [KNN 近邻排序](./knn) — `orderBy*` 的用法和参数要求。
- [距离范围过滤](./range) — `vectorBy*` 的用法和阈值含义。
- [条件构造器](../lambda/where_builder) — 普通字段条件和动态条件。
