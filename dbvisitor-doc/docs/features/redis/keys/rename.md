---
id: rename
sidebar_position: 21
title: RENAME
---

:::info[说明]
官方文档：[RENAME](https://redis.io/docs/latest/commands/rename/)。
:::

重命名键，覆盖已存在的目标键。

## 语法

```text
RENAME key newkey
```

源键不存在时报错。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 成功返回 1；目标键已存在时会被覆盖，源键不存在时报错 |

## 示例

```text
SET demo:original hello
RENAME demo:original demo:renamed
```
