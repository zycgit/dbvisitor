---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 和 `Update` 均生成 `MERGE`，按实体映射的主键列匹配。前者跳过已有记录，后者更新已有记录并插入新记录。

主键映射应与表约束一致，其他唯一键冲突不会自动改为主键匹配。示例见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

构造器使用 `ROWNUMBER() OVER()` 为查询结果编号，再筛选当前页；总数单独查询。

:::caution 稳定翻页
当前分页改写没有把排序放入 `OVER(...)` 中。需要严格按指定顺序翻页时，使用显式的 `ROW_NUMBER() OVER(ORDER BY ...)` SQL，见[分页查询](pagination.mdx#dbvisitor-用法)。
:::
