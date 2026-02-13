---
id: row_callback
sidebar_position: 3
hide_table_of_contents: true
title: 9.3 RowCallbackHandler
description: RowCallbackHandler 用于逐行处理查询结果，而非收集它们。适用于流式处理大规模数据，处理过程中数据不会堆积在内存中。
---

# RowCallbackHandler

`RowCallbackHandler` 用于**逐行处理**查询结果而非收集它们。与 RowMapper 返回结果不同，RowCallbackHandler 的 `processRow` 方法返回 `void`，
适合流式处理大规模数据（如逐行写入文件、发送消息等），数据不会堆积在内存中。

```java title='接口定义'
@FunctionalInterface
public interface RowCallbackHandler {
    void processRow(ResultSet rs, int rowNum) throws SQLException;
}
```

```java title='自定义实现示例'
public class UserRowCallbackHandler implements RowCallbackHandler {
    private final List<String> names = new ArrayList<>();

    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
        names.add(rs.getString("name"));
    }

    public List<String> getNames() { return names; }
}
```

## 如何使用

:::caution[注意]
RowCallbackHandler 使用 `query()` 方法（返回 `void`），而**非** `queryForList()`。数据的消费在 `processRow` 中完成。
:::

```java title='例：编程式 API'
UserRowCallbackHandler handler = new UserRowCallbackHandler();
// query() 返回 void，结果在 handler 内部处理
jdbc.query("select * from users", handler);
List<String> names = handler.getNames();
```

```java title='例：声明式 API'
@SimpleMapper
public interface UserMapper {
    @Query(value = "select * from users where id > #{id}",
           resultRowCallback = UserRowCallbackHandler.class)
    void listUsers(@Param("id") long searchId);
}
```

```java title='例：构造器 API'
UserRowCallbackHandler handler = new UserRowCallbackHandler();
// query() 返回 void
lambda.query(User.class)
      .le(User::getId, 100)
      .query(handler);
List<String> names = handler.getNames();
```

```xml title='例：在 Mapper File 中使用'
<select id="queryListByAge" resultRowCallback="com.example.dto.UserRowCallbackHandler">
    select * from users where age = #{age}
</select>
```

## 内置实现 {#inner}

无内置实现，用户可根据需求自行实现 `RowCallbackHandler` 接口。