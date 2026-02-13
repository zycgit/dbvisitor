---
id: declarative_api
sidebar_position: 2
hide_table_of_contents: true
title: 4.2 声明式 API
description: 声明式 API 的特点是通过创建带有注释的接口来定义要执行的 SQL 和返回结果。
---

# 声明式 API

声明式 API 通过创建 [带有注解的 Java 接口](../core/annotation/about) 来定义 SQL 与返回结果。
SQL 和调用逻辑彼此分离，使代码结构 **更加清晰**，SQL 维护 **更加集中**。

:::tip[特点]
- **接口即 DAO**：每个方法对应一条 SQL，通过 `@Insert`、`@Update`、`@Delete`、`@Query`、`@Execute` 注解声明。
- 支持 **位置参数** `?` 和 **命名参数** `#{name}` ，命名参数可通过 `@Param` 注解或直接使用 Bean 属性。
- 支持 **多行 SQL**：通过字符串数组 `value={}` 拆分长 SQL，Java 13+ 还可使用 Text Blocks。
- 可结合 **[BaseMapper 接口](./base_mapper)** 联合使用，在同一接口中混用注解方法和通用 CRUD。
- 结果自动映射到 **Bean、Map、基本类型** 等，支持 JOIN 查询映射到 DTO。
:::

## 定义接口

```java title='使用 @SimpleMapper 标记接口，方法注解声明 SQL'
@SimpleMapper
public interface UserMapper {
    @Execute("create table user_info (id int primary key, name varchar(50))")
    void createTable();

    // 位置参数
    @Insert("insert into user_info (id,name) values (?, ?)")
    int insertPositional(int id, String name);

    // 命名参数 + @Param
    @Insert("insert into user_info (id,name) values (#{id}, #{name})")
    int insertNamed(@Param("id") int id, @Param("name") String name);

    // Bean 属性作为命名参数
    @Insert("insert into user_info (id,name) values (#{id}, #{name})")
    int insertBean(User user);

    // 更新
    @Update("update user_info set name = #{name}, age = #{age} where id = #{id}")
    int updateUserInfo(@Param("id") int id, @Param("name") String name, @Param("age") int age);

    // 删除
    @Delete("delete from user_info where id = #{id}")
    int deleteById(@Param("id") int id);

    // 查询 - 返回列表
    @Query("select * from user_info")
    List<User> listUsers();

    // 查询 - 返回单个对象
    @Query("select * from user_info where id = #{id}")
    User selectById(@Param("id") int id);

    // 多行 SQL（字符串数组方式）
    @Insert({ "insert into user_info",
              "(id, name, age, email, create_time)",
              "values",
              "(#{id}, #{name}, #{age}, #{email}, #{createTime})" })
    int insertUserMultiLine(User user);
}
```

## 创建和使用

```java title='通过 Configuration 和 Session 创建 Mapper 代理'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);

// 3，创建声明式 Mapper
UserMapper mapper = session.createMapper(UserMapper.class);
```

```java title='调用接口方法'
// 插入
mapper.insertNamed(1, "Bob");
mapper.insertBean(new User(2, "Alice"));

// 查询
User user = mapper.selectById(1);
List<User> users = mapper.listUsers();

// 更新与删除
mapper.updateUserInfo(1, "Robert", 30);
mapper.deleteById(2);
```

:::info[有关声明式 API 的详细信息，请参阅：]
- [方法注解](../core/annotation/about)
:::
