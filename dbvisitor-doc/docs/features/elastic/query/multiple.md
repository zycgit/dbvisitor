---
id: multiple
sidebar_position: 2
title: 多路搜索与多文档读取
---

:::info[说明]
对应 REST 接口：`_msearch`、`_mget`。
:::

## 多路搜索

```text
POST /user_info/_msearch [{},{"query":{"term":{"uid":"1001"}}},{},{"query":{"term":{"uid":"1002"}}}]
```

请求体写成 JSON 数组，请求头与查询体交替排列；驱动转换为 NDJSON。每个子查询产生一个 JDBC 结果集，使用 `execute()` 和 `getMoreResults()` 读取，不是 JDBC batch。

## 按 ID 读取多个文档

```text
GET /user_info/_mget {"ids":["1001","1002"]}
```

这里的 ID 是文档 `_id`，不是 `_source` 内的业务字段。
