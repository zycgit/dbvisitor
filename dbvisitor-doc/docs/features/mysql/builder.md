---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 生成 `INSERT IGNORE`；`Update` 生成 `INSERT ... ON DUPLICATE KEY UPDATE`。冲突由表上的主键或唯一约束判断，不只取决于实体中标记的主键。

`IGNORE` 还可能将数据错误转为警告，不适合用来校验输入。配置和示例见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

`initPage(pageSize, pageNumber)` 生成 `LIMIT offset, size`，页码从 0 开始。分页迭代逐页查询；需要稳定翻页时，按唯一字段排序。

总数由独立的计数查询取得。示例见[分页查询](pagination.mdx)。
