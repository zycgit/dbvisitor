---
id: copy
sidebar_position: 1
title: COPY
---

:::info[说明]
官方文档：[COPY](https://redis.io/docs/latest/commands/COPY/)。
:::

复制键，可指定目标逻辑数据库。

## 语法

```text
COPY source destination [DB database] [REPLACE]
```

`DB` 指定目标逻辑数据库；不写时使用当前库。`REPLACE` 允许覆盖已存在的目标键。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果操作成功，则为 1；如果操作失败，则为 0 |

## 示例

```text
SET demo:original hello
COPY demo:original demo:copy REPLACE
```
