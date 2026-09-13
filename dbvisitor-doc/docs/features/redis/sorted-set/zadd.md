---
id: zadd
sidebar_position: 7
title: ZADD
---

:::info[说明]
官方文档：[ZADD](https://redis.io/docs/latest/commands/zadd/)。
:::

添加成员或修改分数。

## 语法

```text
ZADD key [NX | XX] [GT | LT] [CH] [INCR] score member [score member ...]
```

`NX/XX` 只新增/只更新；`GT/LT` 只提高/降低已有分数；`NX` 不与 `GT/LT` 同用。`CH` 将已改分数的成员也计入数量。`INCR` 只接受一组分数和成员，并返回新分数。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，DOUBLE 类型（当使用 INCR 时）<br/>RESULT 字段，LONG 类型（当不使用 INCR 时） |

:::caution[注意]
`INCR` 配合条件选项且条件不满足时，当前驱动可能报错，不能把它当作正常的 null 返回。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:ranking XX GT CH 30 bob
```
