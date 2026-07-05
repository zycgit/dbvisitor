---
id: about
sidebar_position: 1
title: 5.6 向量查询
description: 使用 LambdaTemplate 对向量字段进行写入、读取、KNN 排序查询和距离范围过滤。
---

# 5.6 向量查询

向量查询用于在支持向量能力的数据源中，按向量距离查找相似记录。dbVisitor 在构造器 API 中提供 `orderBy*` 和 `vectorBy*` 两类入口，分别用于 Top-K 近邻排序和距离范围过滤。

## 适合场景

- 表中包含 embedding、特征向量、图片向量、文本向量等字段。
- 需要查找最相似的 N 条记录。
- 需要查找距离小于某个阈值的全部记录。
- 需要把向量条件和普通字段条件组合使用。

## 不适合场景

- 数据源或方言不支持向量 SQL；调用时会报 `Vector not supported by this dialect.`。
- 需要由 dbVisitor 创建向量索引、调优索引参数或生成 embedding；这些能力属于数据库或模型侧。
- 需要复杂混合检索排序逻辑；可使用 [编程式 API](../jdbc/about) 编写完整 SQL。

## 查询方式

| 方式 | API | 典型问题 | 推荐阅读 |
| --- | --- | --- | --- |
| KNN 近邻排序 | `orderByL2`、`orderByCosine`、`orderByIP`、`orderByMetric` | 找最相似的 N 条记录 | [KNN 近邻排序](./knn) |
| 距离范围过滤 | `vectorByL2`、`vectorByCosine`、`vectorByIP` 等 | 找距离小于阈值的全部记录 | [距离范围过滤](./range) |
| 组合查询 | 普通字段条件 + `orderBy*` / `vectorBy*` | 先按业务条件缩小范围，再做向量查询 | [组合查询](./combined) |

```text title='查询方式选择'
需要固定返回 Top N
        |
        +-- 使用 orderBy* 排序
        +-- 配合 initPage 控制返回数量

需要按阈值筛选全部命中
        |
        +-- 使用 vectorBy* 条件
        +-- 阈值决定返回数量
```

## 基本流程

```text title='向量查询流程'
建表并准备向量字段
        |
配置实体字段和 TypeHandler
        |
写入或读取向量数据
        |
选择 orderBy* 或 vectorBy*
        |
组合普通字段条件、分页或排序
```

向量字段通常在 Java 中使用 `List<Float>` 表示，并通过 TypeHandler 转换为数据库向量类型。完整映射方式见 [向量类型映射](./mapping)。

## orderBy 和 vectorBy 的区别

| 对比项 | `orderBy*` | `vectorBy*` |
| --- | --- | --- |
| SQL 位置 | `ORDER BY` | `WHERE` |
| 典型问题 | 找最相似的 N 条 | 找距离小于阈值的全部记录 |
| 返回数量 | 通常配合 `initPage` 固定数量 | 由阈值和数据分布决定 |
| 向量参数 | 传入数据库可识别类型，例如 `PGobject` | 可通过映射 TypeHandler 转换，例如 `List<Float>` |
| 条件组合 | 和普通 WHERE 条件组合后再排序 | 本身就是 WHERE 条件 |

## 数据库支持

向量 SQL 由数据库方言生成。支持向量 SQL 的方言包括 PostgreSQL pgvector、Milvus、ElasticSearch 等。不同数据库支持的度量方式、索引和参数格式可能不同，实际能力以对应数据源和驱动为准。

## 深入阅读

- [向量类型映射](./mapping) — 建表、实体字段和 TypeHandler 配置。
- [KNN 近邻排序](./knn) — 使用 `orderBy*` 查找最相似的 N 条记录。
- [距离范围过滤](./range) — 使用 `vectorBy*` 按距离阈值筛选记录。
- [组合查询](./combined) — 向量查询和普通字段条件组合。
