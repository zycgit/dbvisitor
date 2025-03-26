---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 方法注解
description: dbVisitor 提供了一些注解用于标记在接口的方法上，这些方法充当数据库访问的媒介，从而避免复杂的 JDBC 操作代码。
---

dbVisitor 提供了一些注解用于标记在接口的方法上，这些方法充当数据库访问的媒介，从而避免复杂的 JDBC 操作代码。

```java title='使用 方法注解 的特点是 SQL 和调用逻辑相互分离'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id > ?")
    List<User> listUsers(long searchId);
}
```

```java title='2. 创建通用 Mapper'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);
或者
Session session = config.newSession(connection);

// 3，创建 Mapper
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> result = mapper.listUsers(2);
```

## 使用指引

Mapper 接口可以定义若干方法，通过标记查询注解便可操作数据库。
- [@Query](./query)，执行一个可以返回结果集的查询语句并返回查询结果。
- [@Insert](./insert)，用于执行 INSERT 语句。
- [@Update](./update)，用于执行 UPDATE 语句。
- [@Delete](./delete)，用于执行 DELETE 语句。
- [@Execute](./execute)，可以用于执行任何类型的语句例如：INSERT、UPDATE、DELETE 或 DDL 操作。
- [@Call](./call)，用于执行 存储过程调用。
- [@Segment](./segment)，用于定义一个 SQL 片段，在其所在 Mapper 的其它 SQL 方法中可以通过 macro 宏规则引用该片段。
- [规则](../../rules/about)，在 SQL 中通过规则赋予 SQL 更加强大的特性。
