---
id: lpos
sidebar_position: 14
title: LPOS
---

:::info[说明]
官方文档：[LPOS](https://redis.io/docs/latest/commands/lpos/)。
:::

查找元素所在的位置。

## 语法

```text
LPOS key element [RANK rank] [COUNT count] [MAXLEN len]
```

`RANK` 指定从第几次匹配开始，负数从末尾找；`COUNT` 限制返回位置数，`0` 表示全部；`MAXLEN` 限制搜索长度。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | RESULT 字段，LONG 类型 |

:::caution[注意]
当前 `COUNT` 需配合 `RANK` 或 `MAXLEN` 才生效，如示例中的 `RANK 1 COUNT 2`。不带有效 COUNT 的单结果路径，在元素不存在时可能报错；不能据此判断为位置 0。
:::

## 示例

```text
RPUSH demo:queue first second first
LPOS demo:queue first RANK 1 COUNT 2
```
