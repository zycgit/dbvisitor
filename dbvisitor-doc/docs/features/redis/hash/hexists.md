---
id: hexists
sidebar_position: 2
title: HEXISTS
---

:::info[说明]
官方文档：[HEXISTS](https://redis.io/docs/latest/commands/hexists/)。
:::

判断字段是否存在。

## 语法

```text
HEXISTS key field
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，BOOLEAN 类型 |

## 示例

```text
HSET demo:user name mali
HEXISTS demo:user name
```
