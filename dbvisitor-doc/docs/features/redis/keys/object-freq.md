---
id: object-freq
sidebar_position: 14
title: OBJECT FREQ
---

:::info[说明]
官方文档：[OBJECT FREQ](https://redis.io/docs/latest/commands/object-freq/)。
:::

查看 LFU 策略记录的访问频率计数。

## 语法

```text
OBJECT FREQ key
```

示例要求键已存在，且服务端启用 LFU 淘汰策略；否则该命令不可用。不要为运行示例修改生产淘汰策略。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
OBJECT FREQ demo:message
```
