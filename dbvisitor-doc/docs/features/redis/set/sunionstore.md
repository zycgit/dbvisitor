---
id: sunionstore
sidebar_position: 17
title: SUNIONSTORE
---

:::info[说明]
官方文档：[SUNIONSTORE](https://redis.io/docs/latest/commands/sunionstore/)。
:::

将集合并集保存到目标键。

## 语法

```text
SUNIONSTORE destination key [key ...]
```

`destination` 会被结果覆盖，结果为空时移除目标键。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 结果集合中的元素数量。 |

## 示例

```text
SADD demo:tags java jdbc
SADD demo:other rust
SUNIONSTORE demo:all demo:tags demo:other
```
