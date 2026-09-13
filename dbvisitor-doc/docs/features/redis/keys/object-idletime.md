---
id: object-idletime
sidebar_position: 15
title: OBJECT IDLETIME
---

:::info[说明]
官方文档：[OBJECT IDLETIME](https://redis.io/docs/latest/commands/object-idletime/)。
:::

查看键距上次访问的秒数。

## 语法

```text
OBJECT IDLETIME key
```

使用 LFU 淘汰策略时不支持此命令。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
OBJECT IDLETIME demo:message
```
