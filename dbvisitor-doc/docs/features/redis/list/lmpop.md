---
id: lmpop
sidebar_position: 3
title: LMPOP
---

:::info[说明]
官方文档：[LMPOP](https://redis.io/docs/latest/commands/lmpop/)。
:::

从第一个非空列表弹出元素。

## 语法

```text
LMPOP numkeys key [key ...] LEFT|RIGHT [COUNT count]
```

`numkeys` 与键个数一致；按键顺序选择第一个非空列表，只从该列表弹出。`COUNT` 是最多弹出数量，默认 1。

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
LMPOP 1 demo:queue LEFT COUNT 2
```
