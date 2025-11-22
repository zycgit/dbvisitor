---
id: redis_type
sidebar_position: 5
hide_table_of_contents: true
title: Redis 数据类型
description: 使用 dbVisitor 读写 Redis 的 String 类型数据。
---

Redis 有 5 个主要的数据类型，本文将会介绍如何使用 dbVisitor 对这些数据类型进行读写操作。

:::tip[提示]
Redis 主要数据类型命令参考：[String](../../drivers/redis/commands#string)、[Hash](../../drivers/redis/commands#hash)、[List](../../drivers/redis/commands#list)、[Set](../../drivers/redis/commands#set)、[StoreSet](../../drivers/redis/commands#storeset)
:::

## 字符串 {#string}

```java title="通过 GET/SET 命令读写字符串"
jdbc.executeUpdate("SET ? ?", new Object[] { "myKey", "myValue" });
String value = jdbc.queryForString("GET ?", "myKey");
```

```java title="在 SET 值时指定过期时间"
jdbc.executeUpdate("SETEX ? ? ?", new Object[] { "myKey", 60, "myValue" });
jdbc.executeUpdate("SET ? ? EX ?", new Object[] { "myKey", "myValue", 60 });
```
- 参考：[SET 命令](https://redis.io/docs/latest/commands/set/)

```java title="通过 INCR/DECR 命令对数值进行自增/自减操作"
jdbc.executeUpdate("SET myKey 0");                      // 初始值 0
String result = jdbc.queryForString("INCR ?", "myKey"); // 自增后的值为 1
String result = jdbc.queryForString("DECR ?", "myKey"); // 自减后的值为 0
```
- 参考：[INCR 命令](https://redis.io/docs/latest/commands/incr/)、[DECR 命令](https://redis.io/docs/latest/commands/decr/)

```java title="通过 APPEND/SUBSTR 命令对字符串进行追加/截取操作"
jdbc.executeUpdate("SET myKey myValue");                                              // 初始值 myValue
String result = jdbc.queryForString("APPEND ? ?", new Object[] { "myKey", "123" });   // 追加后的值为 myValue123
String result = jdbc.queryForString("GETRANGE ? ? ?", new Object[] { "myKey", 0, 5 });// 截取后的值为 myVal
```
- 参考：[APPEND 命令](https://redis.io/docs/latest/commands/append/)、[GETRANGE 命令](https://redis.io/docs/latest/commands/getrange/)

```java title="使用 JSON 序列化方式读写对象"
// MyObject 类型需要标记 @BindTypeHandler(JsonTypeHandler.class) 注解
jdbc.queryForObject("GET myObjectKey", MyObject.class);
jdbc.executeUpdate("SET myObjectKey ?", (MyObject) myObject);
```
- 参考：[GET 命令](https://redis.io/docs/latest/commands/get/)、[SET 命令](https://redis.io/docs/latest/commands/set/)

## 哈希 {#hash}

```java title="通过 HSET/HGET 命令读写哈希字段"
jdbc.executeUpdate("HSET myHashKey field1 value1");
jdbc.queryForString("HGET ? ?", new Object[] { "myHashKey", "field1" });
```
- 参考：[HSET 命令](https://redis.io/docs/latest/commands/hset/)、[HGET 命令](https://redis.io/docs/latest/commands/hget/)

```java title="通过 HKEYS 命令读取哈希 Key 下所有字段"
List<String> keys = jdbc.queryForList("HKEYS myHashKey", String.class);
```
- 参考：[HKEYS 命令](https://redis.io/docs/latest/commands/hkeys/)

```java title="通过 HGETALL 命令读取哈希 Key 下所有字段和值"
Map<String, String> hashFields = jdbc.queryForPairs("HGETALL myHashKey", String.class, String.class);
```
- 参考：[HGETALL 命令](https://redis.io/docs/latest/commands/hgetall/)

```java title="借助 @{pairs} 规则将 Map 持久化到哈希Key"
Map<String,String> hashData = new HashMap<>();
hashData.put("field1", "value1");
hashData.put("field2", "value2");
jdbc.executeUpdate("HSET myKey1 @{pairs, :arg0, :k :v}", SqlArg.valueOf(hashData));
```
- 参考：[HSET 命令](https://redis.io/docs/latest/commands/hset/)、[PAIRS 规则](../../rules/args_rule#pairs)

## 列表 {#list}

```java title="读写列表元素"
jdbc.executeUpdate("LPUSH myListKey value1 value2 value3");
String result = jdbc.queryForString("LPOP myListKey"); // value3
String result = jdbc.queryForString("RPOP myListKey"); // value1
```
- 参考：[LPUSH 命令](https://redis.io/docs/latest/commands/lpush/)、[LPOP 命令](https://redis.io/docs/latest/commands/lpop/)、[RPOP 命令](https://redis.io/docs/latest/commands/rpop/)

```java title="通过 LPUSH/BLPOP 实现消息队列"
// 左进右出阻塞队列
jdbc.executeUpdate("LPUSH myListKey value1");
String result = jdbc.queryForString("BRPOP myListKey"); // BRPOP 可提供阻塞式队列，RPOP 则是普通队列
// 右进左出阻塞队列
jdbc.executeUpdate("RPUSH myListKey value1");
String result = jdbc.queryForString("BLPOP myListKey"); // BLPOP 可提供阻塞式队列，LPOP 则是普通队列
```
- 参考1：[LPUSH 命令](https://redis.io/docs/latest/commands/lpush/)、[BRPOP 命令](https://redis.io/docs/latest/commands/brpop/)、[RPOP 命令](https://redis.io/docs/latest/commands/rpop/)
- 参考2：[RPUSH 命令](https://redis.io/docs/latest/commands/rpush/)、[BLPOP 命令](https://redis.io/docs/latest/commands/blpop/)、[LPOP 命令](https://redis.io/docs/latest/commands/lpop/)

```java title="借助 @{pairs} 规则将 List 持久化到列表"
List<String> listData = new ArrayList<>();
listData.add("value1");
listData.add("value2");
jdbc.executeUpdate("LPUSH myListKey @{pairs, :arg0, :v}", SqlArg.valueOf(listData));
```
- 参考：[LPUSH 命令](https://redis.io/docs/latest/commands/lpush/)、[PAIRS 规则](../../rules/args_rule#pairs)

## 集合 {#set}

```java title="通过 SADD/SMEMBERS 命令读写集合元素"
jdbc.executeUpdate("SADD mySetKey value1 value2 value3");
jdbc.queryForList("SMEMBERS mySetKey", String.class);
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[SMEMBERS 命令](https://redis.io/docs/latest/commands/smembers/)

```java title="利用 SET 的 Key 唯一特为文章打标"
jdbc.executeUpdate("SADD article_1 tag1 tag2");
jdbc.executeUpdate("SADD article_1 tag1 tag3");
jdbc.queryForList("SMEMBERS article_1", String.class); // 结果为 [tag1, tag2, tag3]
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[SMEMBERS 命令](https://redis.io/docs/latest/commands/smembers/)

```java title="借助 @{pairs} 规则将 Map 的 Keys 持久化到 Set 中"
Map<String,String> hashData = new HashMap<>();
hashData.put("field1", "value1");
hashData.put("field2", "value2");
jdbc.executeUpdate("SADD myKey1 @{pairs, :arg0, :k}", SqlArg.valueOf(hashData));
jdbc.queryForList("SMEMBERS myKey1", String.class); // 结果为 [field1, field2]
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[PAIRS 规则](../../rules/args_rule#pairs)

## 有序集合 {#sorted_set}

Sorted Set 数据结构和 Set 数据结构类似，都可以存储多个元素，但是每个元素都有一个分数（score），根据分数可以对元素进行排序。

```java title="通过 ZADD/ZRANGEBYSCORE 命令读写集合元素"
jdbc.execute("ZADD mySetKey 3 value3 2 value2 1 value1");
jdbc.queryForList("ZRANGEBYSCORE mySetKey -inf +inf ", String.class); // 结果为 [value1, value2, value3]
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[SMEMBERS 命令](https://redis.io/docs/latest/commands/smembers/)

```java title="使用 Map 存储元素和分数，将这个带有分数的 Map 存入 ZSet"
Map<String,Double> hashData = new HashMap<>();
hashData.put("field1", 3.0);
hashData.put("field2", 2.0);
hashData.put("field3", 1.0);
jdbc.executeUpdate("ZADD myKey1 @{pairs, :arg0, :v :k}", SqlArg.valueOf(hashData));
jdbc.queryForList("ZRANGEBYSCORE myKey1 -inf +inf ", String.class); // 结果为 [field3, field2, field1]
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[PAIRS 规则](../../rules/args_rule#pairs)

```java title="借助元组类型和 @{pairs} 规则将带有分数的数据存入 ZSet 中"
List<Tuple> data = new ArrayList<>();
data.add(Tuple.of("field1", 3.0));
data.add(Tuple.of("field2", 2.0));
data.add(Tuple.of("field3", 1.0));
jdbc.queryForString("ZADD myKey1 @{pairs, :arg0, :v.arg1 :v.arg0 }", SqlArg.valueOf(data));
jdbc.queryForList("ZRANGEBYSCORE myKey1 -inf +inf ", String.class); // 结果为 [field3, field2, field1]
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[PAIRS 规则](../../rules/args_rule#pairs)
- 除了元组类型，还可以使用 Map 类型、用户自定义类型来存储元素和分数
  - `:v.arg1` 是使用了 [名称参数](../../args/named) 方式来传递参数
