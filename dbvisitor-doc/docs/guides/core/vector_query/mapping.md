---
id: mapping
sidebar_position: 2
title: 向量类型映射
description: 配置向量字段、实体类型和 TypeHandler，让向量数据可以被写入、读取和查询。
---

# 向量类型映射

向量查询依赖数据库字段、Java 字段和 TypeHandler 三者保持一致。字段类型决定数据库如何存储向量，Java 类型决定业务代码如何传递向量，TypeHandler 负责两者之间的转换。

## 适合场景

- 使用 PostgreSQL pgvector、Milvus、ElasticSearch 等支持向量能力的数据源。
- 希望在实体中用 `List<Float>` 表示向量字段。
- 希望后续通过 `orderBy*` 或 `vectorBy*` 对该字段进行向量查询。

## 不适合场景

- 向量字段只在手写 SQL 中使用，不需要对象映射。
- 数据库驱动已经要求使用专用向量对象，并且业务代码也直接维护该对象。
- 向量维度、索引和 embedding 生成流程尚未确定。

## 映射关系

```text title='向量字段映射'
数据库字段
embedding vector(128)
        |
        | TypeHandler
        v
Java 字段
List<Float> embedding
```

dbVisitor 不生成 embedding，也不替代数据库的向量索引。它负责把向量字段纳入对象映射，并在构造器 API 中生成对应的向量查询 SQL。

## 建表

以下示例使用 PostgreSQL + pgvector。

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_vector (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100),
    category  VARCHAR(50),
    embedding vector(128)
);
```

向量字段维度需要和实际写入的 embedding 维度一致。维度不一致时，错误通常由数据库或驱动在写入、查询时抛出。

## 实体映射

```java title='ProductVector.java'
@Table("product_vector")
public class ProductVector {
    @Column(primary = true)
    private Integer id;

    private String name;

    private String category;

    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;

    // getter / setter ...
}
```

`PgVectorTypeHandler` 负责 `List<Float>` 和 pgvector 文本格式之间的转换。其他数据库需要使用对应的向量 TypeHandler，或直接使用驱动支持的参数类型。

## 写入向量

完成映射后，新增向量字段与普通实体字段一致。

```java title='写入向量'
ProductVector row = new ProductVector();
row.setName("sample");
row.setCategory("book");
row.setEmbedding(Arrays.asList(0.1f, 0.2f, 0.3f));

lambda.insert(ProductVector.class)
      .applyEntity(row)
      .executeSumResult();
```

## 更新向量

```java title='更新向量'
List<Float> newVector = Arrays.asList(0.9f, 0.8f, 0.7f);

lambda.update(ProductVector.class)
      .eq(ProductVector::getId, 1001)
      .updateTo(ProductVector::getEmbedding, newVector)
      .doUpdate();
```

## 读取向量

```java title='读取向量'
ProductVector loaded = lambda.query(ProductVector.class)
        .eq(ProductVector::getId, 1001)
        .queryForObject();

List<Float> vector = loaded.getEmbedding();
```

## 参数类型

`vectorBy*` 的向量参数会经过实体映射中的 TypeHandler，通常可以直接传 `List<Float>`。`orderBy*` 的向量参数直接进入 SQL 参数绑定，PostgreSQL pgvector 场景中通常传入 `PGobject`。

```java title='pgvector 查询参数'
PGobject target = new PGobject();
target.setType("vector");
target.setValue("[0.1,0.2,0.3]");
```

如果数据库驱动提供专用向量类型，可以使用该类型作为查询参数。

## 深入阅读

- [KNN 近邻排序](./knn) — 使用 `orderBy*` 做 Top-K 查询。
- [距离范围过滤](./range) — 使用 `vectorBy*` 做阈值过滤。
- [对象映射](../mapping/about) — 字段映射和 TypeHandler 配置基础。
