---
id: info
sidebar_position: 7
title: INFO
---

:::info[说明]
官方文档：[INFO](https://redis.io/docs/latest/commands/info/)。
:::

读取服务器信息，按指标展开为结果行。

## 语法

```text
INFO [section]
```

省略 `section` 读取默认信息，或指定一个分组（如 `server`、`memory`、`stats`）。`GROUP` 为分组名，`NAME` 为指标名，`VALUE` 为原始文本值。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | GROUP、NAME、VALUE 字段，均为 STRING 类型。 |

## 示例

```text
INFO server
```
