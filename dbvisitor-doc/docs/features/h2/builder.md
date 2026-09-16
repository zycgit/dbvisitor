---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 使用只包含插入分支的 `MERGE`；`Update` 使用 `MERGE INTO ... KEY (...)`。两种策略都按实体映射中的主键列匹配。

实体必须标记主键；返回值按 H2 的 MERGE 计数，不能将其一律当作新增条数。示例见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

`initPage(pageSize, pageNumber)` 生成 `LIMIT size OFFSET offset`，页码从 0 开始；总数单独查询。分页迭代逐页执行查询，稳定翻页需要明确排序。

示例见[分页查询](pagination.mdx)。
