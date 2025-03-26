---
id: for_mapper
sidebar_position: 1
hide_table_of_contents: true
title: 9.1 RowMapper
description: RowMapper 是在 ResultSet 读取每一行时进行映射操作。使用 RowMapper 的好处是无需关心 ResultSet 的处理过程，只需要将编程工作专注在每一行数据的处理上。
---

# RowMapper

RowMapper 是在 ResultSet 读取每一行时进行映射操作。使用 RowMapper 的好处是无需关心 ResultSet 的处理过程，只需要将编程工作专注在每一行数据的处理上。

```java title='举例'
public class UserRowMapper implements RowMapper<User> {
    public User mapRow(ResultSet rs,int rowNum)throws SQLException{
        User user =new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        return user;
    }
};
```

## 如何使用

```java title='例：编程式 API'
UserRowMapper userRowMapper = ...
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

```java title='例：构造器'
List<User> result = adapter.queryByEntity(User.class)
                           .le(User::getId, 100)        // 匹配 ID 小于等于 100
                           .queryForList(userRowMapper);// 使用 RowMapper 处理结果集
```

```xml title='例：在 Mapper File'
<select id="queryListByAge" resultRowMapper="com.example.dto.UserRowMapper">
    select * from users where age = #{age}
</select>
```

## 内置实现 {#inner}

在 dbVisitor 中一共内置了 4 种 RowMapper，除此之外用户可以自己所以扩展。

```java title='ColumnMapRowMapper'
// 将行转换为 Map
String querySql = "select * from users where age > 40";
RowMapper rowMapper = new ColumnMapRowMapper();
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

```java title='SingleColumnRowMapper'
// 查询结果中只有一列的行
String querySql = "select name from users where age > 40";
RowMapper rowMapper = new SingleColumnRowMapper();
List<String> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

```java title='BeanMappingRowMapper'
// 基于对象映射处理行数据，并将每一行数据都转换为 Bean
String querySql = "select * from users where age > 40";
RowMapper rowMapper = new MappingRowMapper<>(TestUser.class);
List<TestUser> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

```java title='MapMappingRowMapper'
// 基于对象映射处理行数据，并将每一行数据都转换为 Map
String querySql = "select * from users where age > 40";
RowMapper rowMapper = new MapMappingRowMapper<>(TestUser.class);
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

:::info[提示]
ColumnMapRowMapper 和 MapMappingRowMapper 都以 Map 作为结果返回它们的差异是：
- ColumnMapRowMapper，可以处理任意列的查询，例如：select *
- MapMappingRowMapper，基于构造方法中的实体类型，决定结果集中最多包含的列成员，无论语句中是否使用 select *
:::