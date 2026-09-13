---
id: results
sidebar_position: 3
title: 写入结果
---

写入成功与搜索立即可见是两件事。

- 单文档写入后需要立即搜索，可使用 `refresh=wait_for`。
- 按条件更新或删除使用 `refresh=true`，不要照搬 `wait_for`。
- 连接参数 `indexRefresh=true` 为相关写请求追加 `refresh=true`；显式请求参数优先。

UPDATE/DELETE 类请求未指定 refresh，且未启用 indexRefresh 时，返回 `Statement.SUCCESS_NO_INFO`，表示成功但没有确定计数。显式设置 refresh 后，单文档操作按 result 计算，按条件操作读取 updated/deleted。

`refresh=false` 也会进入计数读取分支，但不保证搜索立即可见。
