---
id: vectors
slug: /features/mongo/vectors
sidebar_position: 50
title: 向量操作
---

## 向量类型映射 {#vector-mapping}

向量保存为数值型 BSON 数组，通过数组映射读写。实体中的 `List<Float>` 属性使用 [BSON 集合处理器](../../../guides/types/json-serialization.md#bson)，不要把向量存为 JSON 字符串；直接执行命令时，数组绑定见[数组类型](types.md#array-values)。

## 向量检索 {#vector-search}

当前 MongoDB 适配不提供构造器 API 的 KNN 排序、距离范围过滤及向量与标量组合检索。表中的“向量类型映射”支持仅指向量值的读写，不表示已接入 MongoDB 的原生向量检索。
