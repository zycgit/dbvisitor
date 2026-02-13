---
id: for_mapper
sidebar_position: 1
hide_table_of_contents: true
title: 9.1 RowMapper
description: RowMapper 在 ResultSet 读取每一行时进行映射操作，无需关心 ResultSet 的遍历过程，只需专注每行数据的转换。
---

# RowMapper

`RowMapper` 在 ResultSet 读取每一行时进行映射操作。使用 RowMapper 无需关心 ResultSet 的遍历过程，只需将编程工作专注在每行数据的转换上。

```java title='接口定义'
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```

```java title='自定义实现示例'
public class UserRowMapper implements RowMapper<User> {
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        return user;
    }
}
```

## 如何使用

```java title='例：编程式 API'
UserRowMapper userRowMapper = new UserRowMapper();
List<User> result = jdbc.queryForList("select * from users", userRowMapper);
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query(value = "select * from users where id > #{id}",
           resultRowMapper = UserRowMapper.class)
    List<User> listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器 API'
List<User> result = lambda.query(User.class)
                          .le(User::getId, 100)
                          .queryForList(userRowMapper);
```

```xml title='例：在 Mapper File 中使用'
<select id="queryListByAge" resultRowMapper="com.example.dto.UserRowMapper">
    select * from users where age = #{age}
</select>
```

## 内置实现 {#inner}

dbVisitor 内置了 5 种 RowMapper，用户也可自行扩展。

```java title='ColumnMapRowMapper'
// 将每行转换为 Map<String, Object>，适用于任意查询
String querySql = "select * from users where age > 40";
RowMapper<Map<String, Object>> rowMapper = new ColumnMapRowMapper();
List<Map<String, Object>> result = jdbc.queryForList(querySql, rowMapper);
```

```java title='SingleColumnRowMapper'
// 查询结果只有一列时，直接转换为目标类型
String querySql = "select name from users where age > 40";
RowMapper<String> rowMapper = new SingleColumnRowMapper<>(String.class);
List<String> result = jdbc.queryForList(querySql, rowMapper);
```

```java title='BeanMappingRowMapper'
// 基于 ORM 映射将每行转换为 Bean
String querySql = "select * from users where age > 40";
RowMapper<TestUser> rowMapper = new BeanMappingRowMapper<>(TestUser.class);
List<TestUser> result = jdbc.queryForList(querySql, rowMapper);
```

```java title='MapMappingRowMapper'
// 基于 ORM 映射将每行转换为 Map（仅包含映射声明的列）
String querySql = "select * from users where age > 40";
RowMapper<Map<String, Object>> rowMapper = new MapMappingRowMapper<>(TestUser.class);
List<Map<String, Object>> result = jdbc.queryForList(querySql, rowMapper);
```

```java title='TypeHandlerColumnRowMapper'
// 查询结果只有一列时，使用指定的 TypeHandler 进行类型转换
String querySql = "select create_time from users where age > 40";
RowMapper<LocalDateTime> rowMapper = new TypeHandlerColumnRowMapper<>(new LocalDateTimeOfTimestampTypeHandler());
List<LocalDateTime> result = jdbc.queryForList(querySql, rowMapper);
```

:::info[ColumnMapRowMapper 与 MapMappingRowMapper 的差异]
两者都返回 `Map`，但行为不同：
- **ColumnMapRowMapper** — 将查询返回的所有列都放入 Map，适合 `select *` 等任意查询。
- **MapMappingRowMapper** — 根据构造方法中的实体类型，只保留 ORM 映射中声明的列，即使查询了更多列也会被忽略。
:::