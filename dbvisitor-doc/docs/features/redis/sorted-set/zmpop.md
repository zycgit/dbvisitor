---
id: zmpop
sidebar_position: 1
title: ZMPOP
---

:::info[说明]
官方文档：[ZMPOP](https://redis.io/docs/latest/commands/zmpop/)。
:::

从第一个非空有序集合弹出低分或高分成员。

## 语法

```text
ZMPOP numkeys key [key ...] MIN|MAX [COUNT count]
```

`numkeys` 与键个数一致；从第一个非空键弹出，`MIN/MAX` 选择最低/最高分；`COUNT` 最多弹出数量，默认 1。

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
ZMPOP 1 demo:ranking MIN COUNT 2
```
