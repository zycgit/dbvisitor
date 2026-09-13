---
id: lrange
sidebar_position: 19
title: LRANGE
---

:::info[说明]
官方文档：[LRANGE](https://redis.io/docs/latest/commands/lrange/)。
:::

读取指定位置范围内的元素。

## 语法

```text
LRANGE key start stop
```

`start/stop` 从 0 开始，包含结束位置；`-1` 表示最后一个元素。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
LRANGE demo:queue 0 1
```
