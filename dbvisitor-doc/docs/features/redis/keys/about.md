---
id: about
slug: /features/redis/commands/keys
sidebar_position: 0
hide_table_of_contents: true
title: 键管理
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [COPY](copy.md) | 复制键，可指定目标逻辑数据库。 |
| [DEL](del.md) | 删除一个或多个键。 |
| [UNLINK](unlink.md) | 删除键，将内存回收交给后台处理。 |
| [DUMP](dump.md) | 读取键值的 Redis 序列化数据。 |
| [EXISTS](exists.md) | 统计指定键中存在的数量。 |
| [EXPIRE](expire.md) | 以秒为单位设置键的存活时间。 |
| [EXPIREAT](expireat.md) | 用 Unix 秒时间戳设置过期时刻。 |
| [EXPIRETIME](expiretime.md) | 读取键的秒级过期时间戳。 |
| [PEXPIRE](pexpire.md) | 以毫秒为单位设置键的存活时间。 |
| [PEXPIREAT](pexpireat.md) | 用 Unix 毫秒时间戳设置过期时刻。 |
| [PEXPIRETIME](pexpiretime.md) | 读取键的毫秒级过期时间戳。 |
| [KEYS](keys.md) | 查找符合模式的键；驱动通过循环 SCAN 完成遍历。 |
| [OBJECT ENCODING](object-encoding.md) | 查看键值的内部编码名称。 |
| [OBJECT FREQ](object-freq.md) | 查看 LFU 策略记录的访问频率计数。 |
| [OBJECT IDLETIME](object-idletime.md) | 查看键距上次访问的秒数。 |
| [OBJECT REFCOUNT](object-refcount.md) | 查看键值对象的引用计数。 |
| [PERSIST](persist.md) | 移除键的过期时间。 |
| [TTL](ttl.md) | 读取键剩余存活秒数。 |
| [PTTL](pttl.md) | 读取键剩余存活毫秒数。 |
| [RANDOMKEY](randomkey.md) | 随机读取当前逻辑数据库中的一个键名。 |
| [RENAME](rename.md) | 重命名键，覆盖已存在的目标键。 |
| [RENAMENX](renamenx.md) | 仅在目标键不存在时重命名。 |
| [SCAN](scan.md) | 读取一批键名及后续游标。 |
| [TOUCH](touch.md) | 更新键的最近访问记录。 |
| [TYPE](type.md) | 读取键对应的数据结构名称。 |
