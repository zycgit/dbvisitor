---
id: smismember
sidebar_position: 9
title: SMISMEMBER
---

:::info[说明]
官方文档：[SMISMEMBER](https://redis.io/docs/latest/commands/smismember/)。
:::

依次判断多个元素是否属于集合。

## 语法

```text
SMISMEMBER key member [member ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | RESULT 字段，BOOLEAN 类型 |

## 示例

```text
SADD demo:tags java jdbc
SMISMEMBER demo:tags java rust
```
