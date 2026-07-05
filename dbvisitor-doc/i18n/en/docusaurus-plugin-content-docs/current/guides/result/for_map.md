---
id: for_map
sidebar_position: 2
hide_table_of_contents: true
title: 9.2 List/Map
description: List/Map is a common structure that represents datasets as collections of maps, making it adaptable to varying shapes.
---

# Receive Data with List/Map

Using `Map` to receive query results is the most versatile approach. Each row is represented as a `Map<String, Object>` mapping column names to values, without needing to predefine entity classes.

```java title='Single row query'
Map<String, Object> data = jdbc.queryForMap("select * from users where id = 1");
```

## How to Use

```java title='Example: Programmatic API'
List<Map<String, Object>> result = jdbc.queryForList("select * from users");
```

```java title='Example: Declarative API'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id > #{id}")
    List<Map<String, Object>> listUsers(@Param("id") long searchId);
}
```

```java title='Example: Fluent API'
List<Map<String, Object>> result = lambda.query(User.class)
                                         .le(User::getId, 100)
                                         .queryForMapList();
```

```xml title='Example: Mapper File'
<select id="queryListByAge" resultType="map">
    select * from users where age = #{age}
</select>
```

:::info[resultType options]
| Value | Corresponding Type | Description |
|-------|-------------------|-------------|
| `map` | LinkedCaseInsensitiveMap (default) | Column name case-insensitive, affected by global configuration |
| `hashmap` | HashMap | Unordered, column name case-sensitive |
| `linkedmap` | LinkedHashMap | Maintains insertion order, column name case-sensitive |
| `caseinsensitivemap` | LinkedCaseInsensitiveMap | Column name case-insensitive |
:::

## Column Name Case Sensitivity

Different databases may return column names with different casing. Oracle often returns uppercase column names, while MySQL often returns lowercase names. dbVisitor uses a case-insensitive Map by default so both `row.get("id")` and `row.get("ID")` can work.

```java title='Default: case-insensitive column names'
List<Map<String, Object>> rows = jdbc.queryForList("select * from users");
Object id = rows.get(0).get("id");
Object sameId = rows.get(0).get("ID");
```

If you want to preserve the exact column-name casing returned by the driver, disable this behavior.

```java title='Strict case sensitivity'
JdbcTemplate jdbc = ...;
jdbc.setResultsCaseInsensitive(false);

List<Map<String, Object>> rows = jdbc.queryForList("select * from users");
```

After disabling it, results are stored in `LinkedHashMap`, and column-name keys become case-sensitive.
