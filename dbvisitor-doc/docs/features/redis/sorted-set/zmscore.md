---
id: zmscore
sidebar_position: 18
title: ZMSCORE
---

:::info[说明]
官方文档：[ZMSCORE](https://redis.io/docs/latest/commands/zmscore/)。
:::

依次读取多个成员的分数。

## 语法

```text
ZMSCORE key member [member ...]
```

每个成员对应一行，顺序与参数一致；不存在的成员分数为 null。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | SCORE 字段，DOUBLE 类型 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZMSCORE demo:ranking alice bob
```
