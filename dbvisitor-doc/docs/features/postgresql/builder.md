---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 使用 `ON CONFLICT DO NOTHING`；`Update` 使用 `ON CONFLICT (主键列) DO UPDATE`。

使用 `Update` 时，实体主键必须对应实际的主键或唯一约束。需要按其他唯一键判断冲突或附加更新条件时，使用手写 SQL。示例见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

`initPage(pageSize, pageNumber)` 生成 `LIMIT size OFFSET offset`，页码从 0 开始；总数单独查询。分页迭代沿用此方式，不是服务端游标。

按唯一字段排序可避免同值记录在翻页时顺序不确定。示例见[分页查询](pagination.mdx)。
