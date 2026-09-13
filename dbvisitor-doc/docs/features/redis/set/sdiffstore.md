---
id: sdiffstore
sidebar_position: 4
title: SDIFFSTORE
---

:::info[说明]
官方文档：[SDIFFSTORE](https://redis.io/docs/latest/commands/sdiffstore/)。
:::

将集合差集保存到目标键。

## 语法

```text
SDIFFSTORE destination key [key ...]
```

`destination` 会被结果覆盖，结果为空时移除目标键。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 结果集合中的元素数量。 |

## 示例

```text
SADD demo:tags java jdbc
SADD demo:other java
SDIFFSTORE demo:diff demo:tags demo:other
```
