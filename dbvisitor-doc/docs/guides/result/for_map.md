---
id: for_map
sidebar_position: 2
hide_table_of_contents: true
title: 9.2 List/Map
description: List/Map 的特征是由多个 Map 组成一个集合。是一种常见的数据结构，可以用来通用化的表示不同结构的数据集。
---

# 使用 List/Map 接收数据

List/Map 的特征是由多个 Map 组成一个集合。是一种常见的数据结构，可以用来通用化的表示不同结构的数据集。

```java title='举例'
Map<String, Object> data = null;

data = jdbc.queryForMap("select * from users where name = 'Bob'");
data = jdbc.queryForMap("select * from address");
```

## 如何使用

```java title='例：编程式 API'
List<Map<String, Object>> result = jdbc.queryForList("select * from users");
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query(value = "select * from users where id > #{id}")
    List<Map> listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器'
List<Map<String, Object>> result = adapter.queryByEntity(User.class)
                                          .le(User::getId, 100) // 匹配 ID 小于等于 100
                                          .queryForMapList();   // 返回 List/Map
```

```xml title='例：在 Mapper File'
<select id="queryListByAge" resultType="map">
    select * from users where age = #{age}
</select>
```

:::info[resultType 属性值可以采用]
- map，使用内置 Map 策略，结果集列名大小写敏感性的设置会影响它，默认是 LinkedCaseInsensitiveMap。
- hashmap，使用 HashMap
- linkedmap，使用 LinkedHashMap
- caseinsensitivemap，使用 LinkedCaseInsensitiveMap
:::

## 列名大小写敏感性 {#case}

默认情况下 Map 的 key 对于大小写不敏感。

```java title='如下查询'
List<Map<String, Object>> result = jdbc.queryForList("select * from users");
```

users 表在相同表结构和数据在不同数据库中执行结果会有不同，例如：
- 在 Oracle 返回的列名默认：大写
- 在 MySQL 返回的列名默认：小写

dbVisitor 默认会采用 `LinkedCaseInsensitiveMap` 工具类以不区分大小写的方式存储查询结果的列名。
- 即 resultsCaseInsensitive 参数默认值为 true

如果希望使用大小写敏感的方式存储查询结果，需要将其设置为 false。

```java
JdbcTemplate jdbc = ...
// highlight-next-line
jdbc.setResultsCaseInsensitive(false);

List<Map<String, Object>> result = jdbc.queryForList("select * from users");
```
