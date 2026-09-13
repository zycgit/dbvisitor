---
id: hkeys
sidebar_position: 12
title: HKEYS
---

:::info[说明]
官方文档：[HKEYS](https://redis.io/docs/latest/commands/hkeys/)。
:::

列出散列的字段名。

## 语法

```text
HKEYS key
```

结果不保证字段顺序；需要字段和值对应时使用 HGETALL，不要分别读取 HKEYS、HVALS 再按下标配对。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | FIELD 字段，STRING 类型 |

## 示例

```text
HSET demo:user name mali age 18
HKEYS demo:user
```
