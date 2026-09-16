---
id: about
slug: /features/redis/commands/keys
sidebar_position: 0
hide_table_of_contents: true
title: 键管理
---

选择命令查看语法、参数、返回结果与示例。执行方式见[查询操作](../dbvisitor/query.mdx)和[数据写入](../dbvisitor/write.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

## 查找与遍历

[EXISTS](exists.md) · [TYPE](type.md)<br />
[KEYS](keys.md) · [SCAN](scan.md) · [RANDOMKEY](randomkey.md)

## 复制、重命名与删除

[COPY](copy.md) · [RENAME](rename.md) · [RENAMENX](renamenx.md)<br />
[DEL](del.md) · [UNLINK](unlink.md)

## 设置键过期

[EXPIRE](expire.md) · [PEXPIRE](pexpire.md)<br />
[EXPIREAT](expireat.md) · [PEXPIREAT](pexpireat.md)

## 查询与移除键过期

[TTL](ttl.md) · [PTTL](pttl.md)<br />
[EXPIRETIME](expiretime.md) · [PEXPIRETIME](pexpiretime.md) · [PERSIST](persist.md)

## 编码与访问记录

[OBJECT ENCODING](object-encoding.md) · [OBJECT REFCOUNT](object-refcount.md)<br />
[OBJECT FREQ](object-freq.md) · [OBJECT IDLETIME](object-idletime.md) · [TOUCH](touch.md)

## 序列化

[DUMP](dump.md)
