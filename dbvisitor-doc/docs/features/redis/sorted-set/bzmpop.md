---
id: bzmpop
sidebar_position: 2
title: BZMPOP
---

:::info[说明]
官方文档：[BZMPOP](https://redis.io/docs/latest/commands/bzmpop/)。
:::

等待有序集合有成员后弹出。

## 语法

```text
BZMPOP timeout numkeys key [key ...] MIN|MAX [COUNT count]
```

`numkeys` 与键个数一致；从第一个非空键弹出，`MIN/MAX` 选择最低/最高分；`COUNT` 最多弹出数量，默认 1。

`timeout` 单位为秒；语句中使用非负整数，`0` 表示无限等待。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | KEY 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型 |

:::caution[注意]
当前所有源集合为空（或阻塞超时）时可能报错，不会可靠地返回空结果集。需要处理此情况时使用 Redis 客户端。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
BZMPOP 1 1 demo:ranking MIN COUNT 2
```
