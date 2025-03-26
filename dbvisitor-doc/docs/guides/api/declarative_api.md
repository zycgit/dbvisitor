---
id: declarative_api
sidebar_position: 2
hide_table_of_contents: true
title: 4.2 声明式 API
description: 声明式 API 的特点是通过创建带有注释的接口来定义要执行的 SQL 和返回结果。
---

# 声明式 API

声明式 API 的特点是通过创建 [带有注释的 Java 接口](../core/annotation/about) 来定义要执行的 SQL 和返回结果。通过接口可以对 SQL 的维护 **更加集中**，使代码结构变得 **更加清晰**。

:::tip[特点]
- 注重将查询需求抽象化为接口。利用 [注释声明](../core/annotation/about) 来执行查询。
- 声明式 API 可以和 [BaseMapper 接口](./base_mapper) 联合使用以增强自身。
:::

:::info
1. 若是需要大段 SQL 可以通过字符串数组方式将其拆分多行。
2. 如果是基于 Java 13+ 编译构建还可以通过 Text Blocks(文本块)语法来更加直观的管理大段 SQL。
:::

```java title='定义声明接口'
@SimpleMapper
public interface UserMapper {
    @Execute("create table user_info (id int primary key, name varchar(50))")
    void createTable();

    // 使用位置参数
    @Insert("insert into user_info (id,name) values (?, ?)")
    void insertPositional(int id, String name);

    // 使用名称参数
    @Insert("insert into user_info (id,name) values (#{id}, #{name})")
    void insertNamed(@Param("id") int id, @Param("name") String name);

    // 使用 Bean 的属性名作为名称参数
    @Insert("insert into user_info (id,name) values (#{id}, #{name})")
    void insertBean(User user);

    // 查询结果映射到任何类型
    @Query("select * from user_info")
    List<User> listUsers();
}
```

```java title='创建声明接口对象'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);
或者
Session session = config.newSession(connection);

// 3，创建声明 API
UserMapper mapper = session.createMapper(UserMapper.class);
```

```java title='执行声明方法'
UserMapper userMapper = ...

userMapper.createTable();

// 使用位置参数
userMapper.insertPositional(1, "Bob");

// 使用名称参数
Map<String, Object> queryArg = CollectionUtils.asMap("id", 2, "name", "Alice");
userMapper.insertNamed(queryArg);

// 使用 Bean 的属性名作为名称参数
userMapper.insertBean(new User(3, "David"));

// 查询结果映射到任何类型
List<User> users = userDAO.listUsers();
```

:::info[有关声明式 API 的详细信息，请参阅：]
- [方法注解](../core/annotation/about)
:::
