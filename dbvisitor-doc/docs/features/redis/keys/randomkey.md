---
id: randomkey
sidebar_position: 20
title: RANDOMKEY
---

:::info[说明]
官方文档：[RANDOMKEY](https://redis.io/docs/latest/commands/randomkey/)。
:::

随机读取当前逻辑数据库中的一个键名。

## 语法

```text
RANDOMKEY
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | KEY 字段，STRING 类型 |

## 示例

```text
RANDOMKEY
```
