---
id: row_callback
sidebar_position: 3
hide_table_of_contents: true
title: 9.3 RowCallbackHandler
description: RowCallBackHandler 是用于处理查询结果的每一条记录，而非获取它们。通常可以用来流式的处理大规模数据。数据在处理过程中不会遗留在内存中。
---

# RowCallbackHandler

RowCallBackHandler 是用于处理查询结果的每一条记录，而非获取它们。通常可以用来流式的处理大规模数据。数据在处理过程中不会遗留在内存中。

```java title='举例'
public class UserRowCallBackHandler implements RowCallBackHandler {
    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
        ...
    }
}
```

## 如何使用

```java title='例：编程式 API'
UserRowCallBackHandler userCallBackHandler = ...
List<User> result = jdbc.queryForList("select * from users", userCallBackHandler);
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query(value = "select * from users where id > #{id}",
           resultRowCallback = UserRowCallBackHandler.class)
    void listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器'
List<UserVO> result = adapter.queryByEntity(User.class)
                             .le(User::getId, 100)              // 匹配 ID 小于等于 100
                             .queryForList(userCallBackHandler);// 使用 CallBackHandler 处理结果集
```

```xml title='例：在 Mapper File 中使用'
<select id="queryListByAge" resultRowCallback="com.example.dto.UserRowCallBackHandler">
    select * from users where age = #{age}
</select>
```

## 内置实现 {#inner}

无内置实现