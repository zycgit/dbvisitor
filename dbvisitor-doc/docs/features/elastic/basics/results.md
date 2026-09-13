---
id: results
sidebar_position: 3
title: 命令返回结果
---

| 请求 | 返回形式 |
| --- | --- |
| `_search`、`_mget` | 文档结果集；按字段读取见[结果读取](../dbvisitor/results.mdx)。 |
| `_count` | 计数结果集。 |
| `_msearch` | 每个子查询一个结果集。 |
| 文档写入 | 更新计数，可请求生成键。 |
| 通用 REST | JSON 对象按顶层字段映射，数组按元素映射。 |
| HEAD | `STATUS` 列。 |

不能只按 GET/POST 判断返回类型。不确定时使用 `execute()`，再判断结果集或更新计数。

搜索结果不保留 `_scroll_id`、sort、highlight、fields、`_score` 或顶层 aggregations。需要这些数据时使用官方客户端。
