---
id: about
slug: /features/redis/commands/list
sidebar_position: 0
hide_table_of_contents: true
title: List 列表
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [LMOVE](lmove.md) | 从源列表弹出一个元素，并放入目标列表的指定端。 |
| [BLMOVE](blmove.md) | 等待源列表有元素后移动到目标列表。 |
| [LMPOP](lmpop.md) | 从第一个非空列表弹出元素。 |
| [BLMPOP](blmpop.md) | 等待任一列表有元素后弹出。 |
| [LPOP](lpop.md) | 从列表左端弹出元素。 |
| [RPOP](rpop.md) | 从列表右端弹出元素。 |
| [BLPOP](blpop.md) | 等待列表有元素后从左端弹出。 |
| [BRPOP](brpop.md) | 等待列表有元素后从右端弹出。 |
| [RPOPLPUSH](rpoplpush.md) | 将源列表的尾部元素移到目标列表头部。 |
| [BRPOPLPUSH](brpoplpush.md) | 等待源列表有元素后将尾部元素移到目标列表头部。 |
| [LINDEX](lindex.md) | 按位置读取一个元素。 |
| [LINSERT](linsert.md) | 在第一个匹配元素之前或之后插入。 |
| [LLEN](llen.md) | 读取列表长度。 |
| [LPOS](lpos.md) | 查找元素所在的位置。 |
| [LPUSH](lpush.md) | 依次从左端插入元素。 |
| [LPUSHX](lpushx.md) | 仅在列表存在时从左端插入。 |
| [RPUSH](rpush.md) | 依次从右端插入元素。 |
| [RPUSHX](rpushx.md) | 仅在列表存在时从右端插入。 |
| [LRANGE](lrange.md) | 读取指定位置范围内的元素。 |
| [LREM](lrem.md) | 按值移除元素。 |
| [LSET](lset.md) | 覆盖指定位置的元素。 |
| [LTRIM](ltrim.md) | 只保留指定位置范围内的元素。 |
