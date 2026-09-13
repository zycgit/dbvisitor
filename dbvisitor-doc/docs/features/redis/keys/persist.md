---
id: persist
sidebar_position: 17
title: PERSIST
---

:::info[说明]
官方文档：[PERSIST](https://redis.io/docs/latest/commands/persist/)。
:::

移除键的过期时间。

## 语法

```text
PERSIST key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 成功移除键的过期时间返回 1；键不存在或没有过期时间返回 0 |

## 示例

```text
SET demo:message hello EX 60
PERSIST demo:message
```
