---
id: by-query
sidebar_position: 2
title: 按条件更新与删除
---

:::info[说明]
对应 REST 接口：`_update_by_query`、`_delete_by_query`。
:::

```text
POST /user_info/_update_by_query?refresh=true {"query":{"term":{"uid": ?}},"script":{"source":"ctx._source.name = params.name","params":{"name": ?}}}
POST /user_info/_delete_by_query?refresh=true {"query":{"term":{"uid": ?}}}
```

更新请求按顺序绑定 uid 和新 name，删除请求绑定 uid。条件匹配的所有文档都会被处理；业务字段相同不代表文档 `_id` 相同。

:::caution[部分成功]
按条件写入可能发生版本冲突或部分成功，已完成的修改不会自动回滚。这里的返回计数不是完整任务报告，不要使用异步任务参数来期待驱动自动等待任务完成。
:::
