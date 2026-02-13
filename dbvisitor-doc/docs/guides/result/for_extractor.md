---
id: for_extractor
sidebar_position: 4
hide_table_of_contents: true
title: 9.4 ResultSetExtractor
description: 使用 ResultSetExtractor 接口，自定义 ResultSet 结果集的处理。
---

# ResultSetExtractor

`ResultSetExtractor` 接口提供对 `ResultSet` 的**完全控制**，适用于需要自定义遍历逻辑的场景（如转换为 Map、聚合统计等）。
与 RowMapper 每行调用不同，ResultSetExtractor 直接接管整个 ResultSet。

```java title='接口定义'
@FunctionalInterface
public interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException;
}
```

```java title='自定义实现示例'
public class UserResultSetExtractor implements ResultSetExtractor<Map<Integer, String>> {
    public Map<Integer, String> extractData(ResultSet rs) throws SQLException {
        Map<Integer, String> hashMap = new HashMap<>();
        while (rs.next()) {
            hashMap.put(rs.getInt("id"), rs.getString("name"));
        }
        return hashMap;
    }
}
```

## 如何使用

```java title='例：编程式 API'
// 使用 query() 方法，返回值由 Extractor 泛型决定
Map<Integer, String> result = jdbc.query("select * from users", userExtractor);
// 带参数
Map<Integer, String> result = jdbc.query("select * from users where age > ?", 18, userExtractor);
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query(value = "select * from users where id > #{id}",
           resultSetExtractor = UserResultSetExtractor.class)
    Map<Integer, String> listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器 API'
// 使用 query() 方法（非 queryForList）
Map<Integer, String> result = lambda.query(User.class)
                                    .le(User::getId, 100)
                                    .query(userExtractor);
```

```xml title='例：在 Mapper File 中使用'
<select id="queryListByAge" resultSetExtractor="com.example.dto.UserResultSetExtractor">
    select * from users where age = #{age}
</select>
```

:::caution[注意]
ResultSetExtractor 使用 `query()` 方法而**非** `queryForList()`。`queryForList()` 用于 RowMapper。
:::

## 内置实现 {#inner}

dbVisitor 内置了 9 个 ResultSetExtractor 实现：

**基于对象映射**
- **BeanMappingResultSetExtractor** — 利用 ORM 映射信息将结果集转换为 `List<Bean>`，效用等同于 `BeanMappingRowMapper`。
- **MapMappingResultSetExtractor** — 利用 ORM 映射信息将结果集转换为 `List<Map>`，效用等同于 `MapMappingRowMapper`。

**基于查询结果**
- **ColumnMapResultSetExtractor** — 将每行转为 `Map<String, Object>`，收集为 `List<Map>`，效用等同于 `ColumnMapRowMapper`。
- **PairsResultSetExtractor** — 将每行的前两列作为键值对，收集为 `Map<K, V>`。键值类型通过 TypeHandler 转换。

```java title='PairsResultSetExtractor 示例'
ResultSetExtractor<Map<Integer, String>> extractor =
    new PairsResultSetExtractor<>(TypeHandlerRegistry.DEFAULT, Integer.class, String.class);
Map<Integer, String> result = jdbc.query("select id, name from users", extractor);
// 结果：{1="mali", 2="dative", 3="jon wes"}
```

**处理多结果集**
- **CallableMultipleResultSetExtractor** — 处理 `CallableStatement` 返回的所有数据（出参、结果集、多执行结果）。
- **PreparedMultipleResultSetExtractor** — 处理 `PreparedStatement` 返回的多结果集。

**包裹已有行处理器**
- **RowMapperResultSetExtractor** — 将 RowMapper 适配为 ResultSetExtractor，返回 `List<T>`。
- **FilterResultSetExtractor** — 在 RowMapperResultSetExtractor 基础上增加 `Predicate` 过滤。
- **RowCallbackHandlerResultSetExtractor** — 将 RowCallbackHandler 适配为 ResultSetExtractor。

```java title='FilterResultSetExtractor 示例'
ResultSetExtractor<List<User>> extractor =
    new FilterResultSetExtractor<>(userRowMapper, user -> user.getAge() > 24);
List<User> result = jdbc.query("select * from users", extractor);
```
