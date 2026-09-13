---
id: search
sidebar_position: 1
title: 搜索与计数
---

:::info[说明]
对应 REST 接口：`_search`、`_count`。
:::

## 搜索

```text
POST /user_info/_search {"query":{"term":{"uid":"1001"}},"size":10}
```

`uid` 应为 keyword 字段。返回命中的 `_ID`、`_DOC`，默认还会展开文档字段。只需要某个字段时，按列名读取，见[结果读取](../dbvisitor/results.mdx)。

## 查询总数

```text
POST /user_info/_count {"query":{"term":{"uid":"1001"}}}
```

总数使用 `_count`，不是当前搜索页的行数。dbVisitor Page 的用法见[分页查询](../dbvisitor/pagination.mdx)。

:::info[结果范围]
搜索只返回命中文档，不返回 `_score`、`highlight`、`aggregations` 等完整响应内容。
:::
