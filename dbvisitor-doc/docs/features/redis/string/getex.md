---
id: getex
sidebar_position: 9
title: GETEX
---

:::info[说明]
官方文档：[GETEX](https://redis.io/docs/latest/commands/getex/)。
:::

读取字符串，同时设置或移除过期时间。

## 语法

```text
GETEX key [EX seconds | PX milliseconds | EXAT unix-seconds | PXAT unix-milliseconds | PERSIST]
```

`EX/PX` 设置存活秒数/毫秒数；`EXAT/PXAT` 设置 Unix 过期时间戳；`PERSIST` 移除过期时间。

键不存在时，返回一行，`VALUE` 为 null。

过期参数支持 64 位整数，可使用字面值或绑定 Java `long`；`PXAT` 可直接传入 Unix 毫秒时间戳。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，STRING 类型 |

## 示例

```text
SET demo:message hello
GETEX demo:message EX 60
```
