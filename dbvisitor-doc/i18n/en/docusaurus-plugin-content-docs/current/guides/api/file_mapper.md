---
id: file_mapper
sidebar_position: 5
hide_table_of_contents: true
title: 4.5 文件 Mapper
description: 当遇到大段查询语句时将复杂的 SQL 放置于独立的 Mapper 文件成为最佳选择。
---

# 文件 Mapper

当遇到大段查询语句时 [编程式 API](../api/program_api)、[声明式 API](../api/declarative_api) 或 [查询构造器](../api/wrapper_api)
都会因为逻辑太过复杂而导致整体可读性的下降，此时将复杂的 SQL 放置于独立的 Mapper 文件成为最佳选择。

:::tip[特点]
- 注重通过 [Mapper XML](../core/file/about) 文件来集中管理 SQL，并允许基于 Mapper 接口来执行它们。从而可以更好的管理复杂 SQL 语句。
- Mapper 文件是 [声明式 API](../api/declarative_api) 的一个扩展，因此推荐混合使用它们。
:::

```xml title='1. 建立 mapper 到 UserMapper 接口的关系, 文件位于：classpath:/mapper/userMapper.xml'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.UserMapper">
    <insert id="insertBean">
        insert into users (id,name) values (#{id}, #{name})
    </insert>

    <select id="listUsers" resultType="com.example.User">
        select * from users
    </select>
</mapper>
```

```java title='2. 利用 @RefMapper 注解将接口和 mapper 联系起来'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper{
    // 映射到 insert into users ... 语句
    int insertBean(User user);

    // 映射到 select * from users ... 语句
    List<User> listUsers();
}
```

```java title='3. 创建 Mapper 代理对象'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);
或者
Session session = config.newSession(connection);

// 3，创建 BaseMapper
UserMapper mapper = session.createMapper(UserMapper.class);
```

:::info[有关 文件 Mapper 的详细信息，请参阅：]
- [Mapper 配置文件](../core/file/about)
:::
