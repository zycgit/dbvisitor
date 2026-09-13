---
id: rpop
sidebar_position: 6
title: RPOP
---

:::info[说明]
官方文档：[RPOP](https://redis.io/docs/latest/commands/rpop/)。
:::

从列表右端弹出元素。

## 语法

```text
RPOP key [count]
```

省略 `count` 时弹出一个元素；指定时最多弹出 `count` 个。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
RPOP demo:queue 2
```
