---
id: jdbc
slug: /features/oracle/programmatic
sidebar_position: 10
title: 编程式 API
---

## 多结果 {#multiple-results}

`call` 可以收集单条 SELECT 返回的结果集。不能把多条 SELECT 用分号拼接后交给 `multipleExecute` 收集结果；需要多个查询结果时，应分别执行。

`RETURNING ... INTO` 使用 OUT 参数接收，不是上述 SELECT 结果集，用法见[数据回填](backfill.mdx)。
