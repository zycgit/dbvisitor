---
id: redis
sidebar_position: 1
hide_table_of_contents: true
title: Redis 特异性
description: Redis 数据源使用 dbVisitor 的能力范围与限制。
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Redis 数据源特异性

dbVisitor 通过 [JDBC-Redis](../../../drivers/redis/about) 驱动，基于 JDBC 协议访问 Redis 数据源。

**支持的能力：**
- 使用 Redis 命令操作数据（[支持的命令列表](../../../drivers/redis/commands)）
- [JdbcTemplate](#exec-command)、[方法注解](#exec-annotation)、[Mapper 文件](#exec-file) 三种使用方式
- [规则](../../rules/about)、[参数传递](../../args/about)、[ResultSetExtractor/RowMapper](../../result/about) 等通用能力
- Mapper File 中的[动态 SQL](../../core/file/dynamic) 标签

**不支持：** 构造器 API、通用 Mapper、对象映射、结果集映射、executeBatch、存储过程

## 概念类比

不同 Redis 命令的执行结果分为三种：
- **更新数** — 类比 INSERT/UPDATE/DELETE，用 `executeUpdate` 获取
- **单行/多行结果** — 类比 SELECT 结果集

---

## 命令方式（JdbcTemplate）{#exec-command}

使用 JdbcTemplate 可以直接执行 Redis 命令，在此之前请确保已经正确配置好 Redis 数据源，具体请参考 [Redis 驱动使用指南](../../../drivers/redis/usecase)。

:::tip[提示]
更多使用方式请参考 [JdbcTemplate 类](../../core/jdbc/about#guide)，在使用过程中下面两个特性由于驱动原因无法支持：
- 批量化
- 存储过程
:::

```java title='创建 JdbcTemplate'
JdbcTemplate jdbc = new JdbcTemplate(dataSource);
// 或者
JdbcTemplate jdbc = new JdbcTemplate(connection);
```

### Key 读/写/删

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
int result2 = jdbc.executeUpdate("SET ? ?", new Object[] { "myKey", "myValue" });
```

```java title='删除 Key'
// 直接命令方式
int result1 = jdbc.executeUpdate("DEL myKey");
// 参数化命令方式
int result2 = jdbc.executeUpdate("DEL ?", "myKey");
```

### Key 读/写/删（多个 Key）

```java title='读取多个 Key'
Map<String, Integer> result = jdbc.queryForPairs("MGET ? ? ?", String.class, Integer.class,
                                                  new Object[] { "key1", "key2", "key3" });
```

```java title='写入多个 Key'
int result = jdbc.executeUpdate("MSET ? ? ? ? ? ?",
                                new Object[] { "key1", "value1", "key2", "value2", "key3", "value3" });
```

```java title='删除多个 Key'
int result = jdbc.executeUpdate("DEL ? ? ?", new Object[] { "key1", "key2", "key3" });
```

### 对象的读/写

```java title='使用 @BindTypeHandler 注解绑定 JSON 序列化'
@BindTypeHandler(JsonTypeHandler.class)
public class MyObject {
    ...
}
```

```java title='读写对象'
// 读取对象
MyObject obj = jdbc.queryForObject("GET ?", MyObject.class, "myObjectKey");
// 写入对象
jdbc.executeUpdate("SET ? ?", new Object[] { "myObjectKey", (MyObject) myObject });
```

---

## 注解方式 {#exec-annotation}

:::tip[提示]
对于 [核心 API 提供的注解](../../core/annotation/about) 方式除了 `@Call` 注解不支持之外，其它所有注解都可以在 Redis 数据源上正常使用。
:::

```java title='1. 定义对象并绑定 JSON 序列化'
@BindTypeHandler(JsonTypeHandler.class)
public class UserInfo {
    private String uid;
    private String name;
    ... // 省略 getter/setter 方法
}
```

```java title='2. 定义 Mapper 接口'
// 存取用户信息时 Key 的名字使用前缀 "user_" 和 UID 组合
@SimpleMapper()
public interface UserInfoMapper {
    @Insert("set #{'user_' + info.uid} #{info}")
    int saveUser(@Param("info") UserInfo info);

    @Query("get #{'user_' + uid}")
    UserInfo loadUser(@Param("uid") String uid);

    @Delete("del #{'user_' + uid}")
    int deleteUser(@Param("uid") String uid);
}
```

```java title='3. 创建并使用 Mapper'
Configuration config = new Configuration();
Session session = config.newSession(dataSource);

UserInfoMapper mapper = session.createMapper(UserInfoMapper.class);
```

---

## 文件方式（Mapper File）{#exec-file}

:::tip[提示]
在 XML Mapper 文件中，通过 select 标签配置对象序列化映射时，需要指定 resultType 属性才可以正确发现对象上的 BindTypeHandler 注解信息。
:::

<Tabs>
    <TabItem value="a" label="方式 1 借助 BindTypeHandler 注解" default>

        ```java title='1. 定义对象并绑定 JSON 序列化'
        @BindTypeHandler(JsonTypeHandler.class)
        public class UserInfo {
            private String uid;
            private String name;

            ... // 省略 getter/setter 方法
        }
        ```

        ```xml title='2. 定义 Mapper 文件'
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
        <mapper namespace="net.test.dto.UserInfoMapper">
            <insert id="saveUser">
                set #{'user_' + uid} #{info}
            </insert>

            <select id="loadUser" resultType="net.test.dto.UserInfo">
                get #{'user_' + uid}
            </select>

            <delete id="deleteUser">
                del #{'user_' + uid}
            </delete>
        </mapper>
        ```

    </TabItem>
    <TabItem value="b" label="方式 2 使用无侵入式">

        ```java title='1. 定义对象，不使用 BindTypeHandler 注解'
        public class UserInfo {
            private String uid;
            private String name;

            ... // 省略 getter/setter 方法
        }
        ```

        ```xml title='2. 定义 Mapper 文件'
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
        <mapper namespace="net.test.dto.UserInfoMapper">
            <insert id="saveUser">
                set #{'user_' + uid}
                #{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}
            </insert>

            <!-- 需要同时指定 resultTypeHandler 和 resultType 属性 -->
            <select id="loadUser1"
                    resultTypeHandler="net.hasor.dbvisitor.types.handler.json.JsonTypeHandler"
                    resultType="net.test.dto.UserInfo">
                get #{'user_' + uid}
            </select>

            <select id="loadUser2" resultType="net.test.dto.UserInfo">
                get #{'user_' + uid}
            </select>

            <delete id="deleteUser">
                del #{'user_' + uid}
            </delete>
        </mapper>
        ```

    </TabItem>
</Tabs>

```java title='3. 定义 Mapper 接口'
@RefMapper("dbvisitor/mapper/UserInfoMapper.xml")
public interface UserInfoMapper {
    int saveUser(@Param("info") UserInfo info);

    UserInfo loadUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
```

```java title='4. 创建并使用 Mapper'
Configuration config = new Configuration();
Session session = config.newSession(dataSource);

UserInfoMapper mapper = session.createMapper(UserInfoMapper.class);
```

---

## Redis 数据类型操作 {#redis-type}

Redis 有 5 个主要的数据类型，下面介绍如何使用 dbVisitor 对这些数据类型进行读写操作。

:::tip[提示]
Redis 主要数据类型命令参考：[String](../../../drivers/redis/commands#string)、[Hash](../../../drivers/redis/commands#hash)、[List](../../../drivers/redis/commands#list)、[Set](../../../drivers/redis/commands#set)、[StoreSet](../../../drivers/redis/commands#storeset)
:::

### 字符串 {#string}

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

```java title="通过 APPEND/GETRANGE 命令对字符串进行追加/截取操作"
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

### 哈希 {#hash}

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
- 参考：[HSET 命令](https://redis.io/docs/latest/commands/hset/)、[PAIRS 规则](../../rules/dynamic_rule#pairs)

### 列表 {#list}

```java title="读写列表元素"
jdbc.executeUpdate("LPUSH myListKey value1 value2 value3");
String result = jdbc.queryForString("LPOP myListKey"); // value3
String result = jdbc.queryForString("RPOP myListKey"); // value1
```
- 参考：[LPUSH 命令](https://redis.io/docs/latest/commands/lpush/)、[LPOP 命令](https://redis.io/docs/latest/commands/lpop/)、[RPOP 命令](https://redis.io/docs/latest/commands/rpop/)

```java title="通过 LPUSH/BLPOP 实现消息队列"
// 左进右出阻塞队列
jdbc.executeUpdate("LPUSH myListKey value1");
String result = jdbc.queryForString("BRPOP myListKey");
// 右进左出阻塞队列
jdbc.executeUpdate("RPUSH myListKey value1");
String result = jdbc.queryForString("BLPOP myListKey");
```
- 参考：[LPUSH 命令](https://redis.io/docs/latest/commands/lpush/)、[BRPOP 命令](https://redis.io/docs/latest/commands/brpop/)

```java title="借助 @{pairs} 规则将 List 持久化到列表"
List<String> listData = new ArrayList<>();
listData.add("value1");
listData.add("value2");
jdbc.executeUpdate("LPUSH myListKey @{pairs, :arg0, :v}", SqlArg.valueOf(listData));
```
- 参考：[LPUSH 命令](https://redis.io/docs/latest/commands/lpush/)、[PAIRS 规则](../../rules/dynamic_rule#pairs)

### 集合 {#set}

```java title="通过 SADD/SMEMBERS 命令读写集合元素"
jdbc.executeUpdate("SADD mySetKey value1 value2 value3");
jdbc.queryForList("SMEMBERS mySetKey", String.class);
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[SMEMBERS 命令](https://redis.io/docs/latest/commands/smembers/)

```java title="利用 SET 的 Key 唯一特性为文章打标"
jdbc.executeUpdate("SADD article_1 tag1 tag2");
jdbc.executeUpdate("SADD article_1 tag1 tag3");
jdbc.queryForList("SMEMBERS article_1", String.class); // 结果为 [tag1, tag2, tag3]
```

```java title="借助 @{pairs} 规则将 Map 的 Keys 持久化到 Set 中"
Map<String,String> hashData = new HashMap<>();
hashData.put("field1", "value1");
hashData.put("field2", "value2");
jdbc.executeUpdate("SADD myKey1 @{pairs, :arg0, :k}", SqlArg.valueOf(hashData));
jdbc.queryForList("SMEMBERS myKey1", String.class); // 结果为 [field1, field2]
```
- 参考：[SADD 命令](https://redis.io/docs/latest/commands/sadd/)、[PAIRS 规则](../../rules/dynamic_rule#pairs)

### 有序集合 {#sorted_set}

Sorted Set 和 Set 类似，但每个元素都有一个分数（score），可根据分数对元素进行排序。

```java title="通过 ZADD/ZRANGEBYSCORE 命令读写集合元素"
jdbc.execute("ZADD mySetKey 3 value3 2 value2 1 value1");
jdbc.queryForList("ZRANGEBYSCORE mySetKey -inf +inf ", String.class); // [value1, value2, value3]
```
- 参考：[ZADD 命令](https://redis.io/docs/latest/commands/zadd/)、[ZRANGEBYSCORE 命令](https://redis.io/docs/latest/commands/zrangebyscore/)

```java title="使用 Map 存储元素和分数，将这个带有分数的 Map 存入 ZSet"
Map<String,Double> hashData = new HashMap<>();
hashData.put("field1", 3.0);
hashData.put("field2", 2.0);
hashData.put("field3", 1.0);
jdbc.executeUpdate("ZADD myKey1 @{pairs, :arg0, :v :k}", SqlArg.valueOf(hashData));
jdbc.queryForList("ZRANGEBYSCORE myKey1 -inf +inf ", String.class); // [field3, field2, field1]
```
- 参考：[ZADD 命令](https://redis.io/docs/latest/commands/zadd/)、[PAIRS 规则](../../rules/dynamic_rule#pairs)

```java title="借助元组类型和 @{pairs} 规则将带有分数的数据存入 ZSet 中"
List<Tuple> data = new ArrayList<>();
data.add(Tuple.of("field1", 3.0));
data.add(Tuple.of("field2", 2.0));
data.add(Tuple.of("field3", 1.0));
jdbc.queryForString("ZADD myKey1 @{pairs, :arg0, :v.arg1 :v.arg0 }", SqlArg.valueOf(data));
jdbc.queryForList("ZRANGEBYSCORE myKey1 -inf +inf ", String.class); // [field3, field2, field1]
```
- 参考：[ZADD 命令](https://redis.io/docs/latest/commands/zadd/)、[PAIRS 规则](../../rules/dynamic_rule#pairs)
- 除了元组类型，还可以使用 Map 类型、用户自定义类型来存储元素和分数
  - `:v.arg1` 使用了 [名称参数](../../args/named) 方式来传递参数
