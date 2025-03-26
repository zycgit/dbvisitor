---
id: for_extractor
sidebar_position: 4
hide_table_of_contents: true
title: 9.4 ResultSetExtractor
description: 使用 ResultSetExtractor 接口，自定义 ResultSet 结果集的处理。
---

# ResultSetExtractor

使用 ResultSetExtractor 接口，自定义 ResultSet 结果集的处理。

```java title='举例'
public class UserResultSetExtractor implements ResultSetExtractor<Map<Integer, String>> {
    public Map<Integer, String> extractData(ResultSet rs) throws SQLException {
        Map<Integer, String> hashMap = new HashMap<>();

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            hashMap.put(id, name);
        }

        return hashMap;
    }
};
```

```text title='执行结果可以为'
{1=mali, 2=dative, 3=jon wes, 4=mary, 5=matt}
```

## 如何使用

```java title='例：编程式 API'
Map<Integer, String> result = jdbc.queryForList("select * from users", userExtractor);
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query(value = "select * from users where id > #{id}",
           resultSetExtractor = UserResultSetExtractor.class)
    Map<Integer, String> listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器'
Map<Integer, String> result = adapter.queryByEntity(User.class)
                                     .le(User::getId, 100)        // 匹配 ID 小于等于 100
                                     .queryForList(userExtractor);// 使用 RowMapper 处理结果集
```

```xml title='例：在 Mapper File 中使用'
<select id="queryListByAge" resultSetExtractor="com.example.dto.UserResultSetExtractor">
    select * from users where age = #{age}
</select>
```

## 内置实现 {#inner}

在 dbVisitor 中一共内置了 8 个实现，除此之外用户可以自己所以扩展。

- 基于对象映射
  - BeanMappingResultSetExtractor 效用等同 BeanMappingRowMapper
  - MapMappingResultSetExtractor 效用等同 MapMappingRowMapper
- 基于查询结果
  - ColumnMapResultSetExtractor 效用等同 ColumnMapRowMapper
- 处理多结果集
  - CallableMultipleResultSetExtractor 可以用来处理 CallableStatement 执行时返回的所有数据包括，出参、结果集、多个执行结果。
  - PreparedMultipleResultSetExtractor 可以用来处理 PreparedStatement 执行时返回的所有数据包括，结果集、多个执行结果。
- 包裹已有行处理器
  - RowMapperResultSetExtractor 提供了一个构造方法可以将一个 RowMapper 作为参数。
  - FilterResultSetExtractor 是 RowMapperResultSetExtractor 的扩展它增加了一个 Predicate 参数用来决定数据收集的时是否保留到结果中。
  - RowCallbackHandlerResultSetExtractor 提供了一个构造方法可以将一个 RowCallbackHandler 作为参数。
