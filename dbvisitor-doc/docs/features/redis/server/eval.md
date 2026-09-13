---
id: eval
sidebar_position: 8
title: EVAL
---

:::info[说明]
官方文档：[EVAL](https://redis.io/docs/latest/commands/eval/)。
:::

在 Redis 服务端执行 Lua 脚本。

## 语法

```text
EVAL script numkeys [key ...] [arg ...]
```

`numkeys` 表示后续键参数的数量，在 Lua 中通过 `KEYS` 读取；其余参数通过 `ARGV` 读取。脚本访问的键应全部显式传入。

## 返回结果

返回一行 `VALUE`。`getObject()` 保留 SDK 返回的数字、字符串、null 或列表；嵌套列表不会展开为表格列。

脚本、键或参数绑定为 `byte[]` 时使用二进制接口；字符串回复保留为 `byte[]`，可用 `getBytes()` 读取。

## 示例

```java
Integer sum = jdbc.queryForObject(
        "EVAL 'return tonumber(ARGV[1]) + tonumber(ARGV[2])' 0 ? ?",
        new Object[] { 10, 5 }, Integer.class);
```

业务值通过参数绑定传入，不要拼接到 Lua 源码中。脚本错误作为 JDBC 异常抛出。脚本执行具有原子性，但发生错误时不会回滚已经执行的写入。
