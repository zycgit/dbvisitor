---
id: blmpop
sidebar_position: 4
title: BLMPOP
---

:::info[说明]
官方文档：[BLMPOP](https://redis.io/docs/latest/commands/blmpop/)。
:::

等待任一列表有元素后弹出。

## 语法

```text
BLMPOP timeout numkeys key [key ...] LEFT|RIGHT [COUNT count]
```

`numkeys` 与键个数一致；按键顺序选择第一个非空列表，只从该列表弹出。`COUNT` 是最多弹出数量，默认 1。

`timeout` 单位为秒；语句中使用非负整数，`0` 表示无限等待。示例先写入元素，避免进入等待。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | KEY 字段，STRING 类型，返回 Key,ValueList 结构中的 Key<br/>ELEMENT 字段，STRING 类型 |

:::caution[注意]
当前所有源列表为空（或阻塞超时）时可能报错，不会可靠地返回空结果集。需要处理此情况时使用 Redis 客户端。
:::

## 示例

```text
RPUSH demo:queue first second
BLMPOP 1 1 demo:queue LEFT COUNT 2
```
