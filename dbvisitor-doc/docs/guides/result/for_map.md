---
id: for_map
sidebar_position: 2
hide_table_of_contents: true
title: 9.2 List/Map
description: 使用 Map 接收查询结果是最通用的方式，每行数据用 Map 表示列名到值的映射，无需定义实体类。
---

# 使用 List/Map 接收数据

使用 `Map` 接收查询结果是最通用的方式，每行数据以 `Map<String, Object>` 表示列名到值的映射，无需预先定义实体类。

```java title='单行查询'
Map<String, Object> data = jdbc.queryForMap("select * from users where id = 1");
```

## 如何使用

```java title='例：编程式 API'
List<Map<String, Object>> result = jdbc.queryForList("select * from users");
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id > #{id}")
    List<Map<String, Object>> listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器 API'
List<Map<String, Object>> result = lambda.query(User.class)
                                         .le(User::getId, 100)
                                         .queryForMapList();
```

```xml title='例：在 Mapper File 中使用'
<select id="queryListByAge" resultType="map">
    select * from users where age = #{age}
</select>
```

:::info[resultType 可选值]
| 值 | 对应类型 | 说明 |
|---|---|---|
| `map` | LinkedCaseInsensitiveMap（默认） | 列名大小写不敏感，受全局配置影响 |
| `hashmap` | HashMap | 无序，列名大小写敏感 |
| `linkedmap` | LinkedHashMap | 保持插入顺序，列名大小写敏感 |
| `caseinsensitivemap` | LinkedCaseInsensitiveMap | 列名大小写不敏感 |
:::
