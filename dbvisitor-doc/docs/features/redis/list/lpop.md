---
id: lpop
sidebar_position: 5
title: LPOP
---

:::info[说明]
官方文档：[LPOP](https://redis.io/docs/latest/commands/lpop/)。
:::

从列表左端弹出元素。

## 语法

```text
LPOP key [count]
```

省略 `count` 时弹出一个元素；指定时最多弹出 `count` 个。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
LPOP demo:queue 2
```
