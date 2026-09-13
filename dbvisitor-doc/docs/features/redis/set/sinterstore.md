---
id: sinterstore
sidebar_position: 7
title: SINTERSTORE
---

:::info[说明]
官方文档：[SINTERSTORE](https://redis.io/docs/latest/commands/sinterstore/)。
:::

将集合交集保存到目标键。

## 语法

```text
SINTERSTORE destination key [key ...]
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
SINTERSTORE demo:common demo:tags demo:other
```
