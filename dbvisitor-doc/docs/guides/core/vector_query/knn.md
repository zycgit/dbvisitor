---
id: knn
sidebar_position: 3
title: KNN 近邻排序
description: 使用 orderByL2、orderByCosine、orderByIP 和 orderByMetric 查询最相似的 N 条记录。
---

# KNN 近邻排序

KNN 近邻排序用于“找最相似的 N 条记录”。dbVisitor 使用 `orderBy*` 生成向量距离排序，通常配合 `initPage` 限制返回数量。

## 适合场景

- 搜索语义最接近的 N 篇文章、商品、图片或知识片段。
- 返回数量固定，例如 Top 5、Top 10。
- 需要普通字段先过滤候选集，再按向量距离排序。

## 不适合场景

- 需要返回所有距离小于阈值的记录；应使用 [距离范围过滤](./range)。
- 需要按多个检索分数进行复杂重排；可使用 [编程式 API](../jdbc/about) 编写完整 SQL。
- 数据源不支持向量排序 SQL。

## KNN 查询模式

```text title='KNN 查询'
全部记录或候选记录
        |
        | 计算 embedding 与查询向量的距离
        v
按距离升序排序
        |
        | initPage(N, 0)
        v
返回最相似的 N 条
```

`orderBy*` 位于 SQL 的 `ORDER BY` 部分。它不决定候选记录范围，候选范围由普通 `WHERE` 条件决定。

## 构造查询向量

`orderBy*` 会使用向量字段映射的 TypeHandler 转换参数。PostgreSQL 实体字段配置 `PgVectorTypeHandler` 后，可以直接传入 `List<Float>`。

```java title='pgvector 查询参数'
List<Float> target = List.of(0.1f, 0.2f, 0.3f);
```

## 查询最近的 N 条

```java title='Top-K 查询'
List<ProductVector> rows = lambda.query(ProductVector.class)
        .orderByL2(ProductVector::getEmbedding, target)
        .initPage(5, 0)
        .queryForList();
```

pgvector 下生成的 SQL 形态类似：

```sql
SELECT * FROM product_vector
ORDER BY embedding <-> ? ASC
LIMIT 5
```

`initPage(5, 0)` 表示只取前 5 条结果。在 PostgreSQL 上，不加 LIMIT 会对匹配结果排序而不限定条数。Milvus 的普通 KNN 无 LIMIT 查询使用搜索迭代器按需读取，不应将其理解为固定 Top-K；Hybrid Search 则必须显式指定 LIMIT。

## 选择距离度量

| 目标 | 方法 | 说明 |
| --- | --- | --- |
| L2 欧氏距离 | `orderByL2` | 通用近邻搜索。 |
| Cosine 余弦距离 | `orderByCosine` | 常用于文本语义向量。 |
| IP 内积距离 | `orderByIP` | 常用于推荐和排序场景。 |
| 动态选择度量 | `orderByMetric` | 度量方式来自配置或运行时参数。 |

```java title='按配置选择度量'
MetricType metric = MetricType.COSINE;

List<ProductVector> rows = lambda.query(ProductVector.class)
        .orderByMetric(metric, ProductVector::getEmbedding, target)
        .initPage(10, 0)
        .queryForList();
```

:::info[内积距离]
pgvector 的 `<#>` 运算符返回负内积。使用 `orderByIP` 升序排序时，内积更大的记录会排在前面。
:::

## 和普通条件组合

KNN 查询经常先用业务字段缩小候选集，再进行向量排序。

```java title='分类内 Top-K'
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

## 常见问题

### 没有实体映射时如何绑定向量

`queryFreedom`、Map 模式或未配置向量 TypeHandler 的字段没有可复用的字段转换规则。此时应使用 `SqlArg` 显式指定 TypeHandler，或传入数据库驱动能够识别的向量对象。

### 什么时候需要 initPage

KNN 的目标通常是固定数量的近邻结果。在 PostgreSQL 上，不设置 `initPage` 会失去 Top-K 限制；Milvus 的普通 KNN 可以使用无 LIMIT 迭代查询。应始终显式设置需要的近邻数量。

## 深入阅读

- [向量类型映射](./mapping) — 向量字段和 TypeHandler 准备。
- [组合查询](./combined) — 普通字段条件与向量排序组合。
- [构造器 API 查询](../lambda/query) — LambdaTemplate 查询基础。
