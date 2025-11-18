---
id: exec_command
sidebar_position: 2
hide_table_of_contents: true
title: 执行命令
description: 使用 JdbcTemplate 执行原始的 Redis 命令并进行读写数据。
---

使用 JdbcTemplate 可以直接执行 Redis 命令并进行读写数据操作，在此之前请确保已经正确配置好 Redis 数据源，具体请参考 [Redis 驱动使用指南](../../drivers/redis/usecase)。

:::tip[贴士]
[支持的 Redis 命令列表](../../drivers/redis/commands)，下面是一些常见使用案例：
:::

## Key 读/写/删

```java title='读取 Key'
// 直接命令方式
String result1 = jdbc.queryForString("GET myKey");
// 参数化命令方式
String result2 = jdbc.queryForString("GET ?", "myKey");
```

```java title='写入 Key'
// 直接命令方式
int result1 = jdbc.executeUpdate("SET myKey myValue");
// 参数化命令方式
int result2 = jdbc.executeUpdate("SET ? ?", "myKey", "myValue");
```

```java title='删除 Key'
// 直接命令方式
int result1 = jdbc.executeUpdate("DEL myKey");
// 参数化命令方式
int result2 = jdbc.executeUpdate("DEL ?", "myKey");
```

## Key 读/写/删 (多个 Key)

```java title='读取多个 Key'
// 直接命令方式
Map<String, Integer> result1= jdbc.queryForPairs("MGET key1 key2 key3", String.class, Integer.class);
// 参数化命令方式
Map<String, Integer> result2 = jdbc.queryForPairs("MGET ? ? ?", String.class, Integer.class, new Object[] { "key1", "key2", "key3" });
```

```java title='写入多个 Key'
// 直接命令方式
int result1 = jdbc.executeUpdate("MSET key1 123 key2 456 key3 789");
// 参数化命令方式
int result2 = jdbc.executeUpdate("MSET ? ? ? ? ? ?", new Object[] { "key1", "value1", "key2", "value2", "key3", "value3" });
```

```java title='删除多个 Key'
// 直接命令方式
int result1 = jdbc.executeUpdate("DEL key1 key2 key3");
// 参数化命令方式
int result2 = jdbc.executeUpdate("DEL ? ? ?", new Object[] { "key1", "key2", "key3" });
```

## 对象的 读/写/删

```java title='使用 @BindTypeHandler 注解绑定 JSON 序列化'
@BindTypeHandler(JsonTypeHandler.class)
public class MyObject {
    ...
}
```

```java title='读取对象'
// 直接命令方式
MyObject result1 = jdbc.queryForObject("GET myObjectKey", MyObject.class);
// 参数化命令方式
MyObject result2 = jdbc.queryForObject("GET ?", MyObject.class, "myObjectKey");
```

```java title='写入对象'
// 参数化命令方式
int result1 = jdbc.executeUpdate("SET ? ?", new Object[] { "myObjectKey", (MyObject) myObject });
```

```java title='删除对象'
// 直接命令方式
int result1 = jdbc.executeUpdate("DEL myObjectKey");
// 参数化命令方式
int result2 = jdbc.executeUpdate("DEL ?", "myObjectKey");
```

## 其它说明

其它查询方式请参考 [JdbcTemplate 类](../jdbc/query)