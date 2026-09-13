---
id: llen
sidebar_position: 13
title: LLEN
---

:::info[说明]
官方文档：[LLEN](https://redis.io/docs/latest/commands/llen/)。
:::

读取列表长度。

## 语法

```text
LLEN key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
RPUSH demo:queue first second
LLEN demo:queue
```
