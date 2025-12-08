---
id: exec_annotation
sidebar_position: 5
hide_table_of_contents: true
title: 注解方式
description: 在接口的方法上通过使用 dbVisitor 提供的注解来定义 MongoDB 命令，这些方法充当数据库访问的媒介，从而避免复杂的操作代码。
---

在接口的方法上通过使用 dbVisitor 提供的注解来定义 MongoDB 命令，这些方法充当数据库访问的媒介，从而避免复杂的操作代码。

:::tip[提示]
对于 [核心 API 提供的注解](../annotation/about) 方式除了除 `@Call` 注解不支持之外，其它所有注解都可以在 MongoDB 数据源上正常使用。
:::

```java title='1. 定义对象'
@Table("user_info")
public class UserInfo {
    @Column(value = "uid", primary = true)
    private String uid;
    @Column("name")
    private String name;
    ... // 省略 getter/setter 方法
}
```

```java title='2. 定义 Mapper 接口'
@SimpleMapper()
public interface UserInfoMapper {
    // 生成命令：test.user_info.insert(...)
    @Insert("test.user_info.insert(#{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})")
    int saveUser(@Param("info") UserInfo info);

    // 生成命令：test.user_info.find(...)
    @Query("test.user_info.find({uid: #{uid}})")
    UserInfo loadUser(@Param("uid") String uid);

    // 生成命令：test.user_info.remove(...)
    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
```

```java title='3. 创建通用 Mapper'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);
或者
Session session = config.newSession(connection);

// 3，创建 Mapper
UserInfoMapper mapper = session.createMapper(UserInfoMapper.class);
```
