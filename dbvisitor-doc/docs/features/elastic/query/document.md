---
id: document
sidebar_position: 3
title: 读取文档与查询解释
---

:::info[说明]
对应 REST 接口：`_source`、`_explain`。
:::

## 按 ID 读取文档源

```text
GET /user_info/_source/1001
```

这里使用文档 `_id`，不是业务字段 uid。

## 查看匹配解释

```text
GET /user_info/_explain/1001 {"query":{"term":{"name":"mali"}}}
```

用于解释指定文档是否匹配查询，不是搜索命中文档列表。
