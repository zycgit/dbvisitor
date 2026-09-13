---
id: ltrim
sidebar_position: 22
title: LTRIM
---

:::info[说明]
官方文档：[LTRIM](https://redis.io/docs/latest/commands/ltrim/)。
:::

只保留指定位置范围内的元素。

## 语法

```text
LTRIM key start stop
```

`start/stop` 从 0 开始，包含结束位置；`-1` 表示最后一个元素。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 操作是否成功，成功返回 1 否则返回 0，当状态为 “OK” 时表示成功 |

## 示例

```text
RPUSH demo:queue first second third
LTRIM demo:queue 0 1
```
