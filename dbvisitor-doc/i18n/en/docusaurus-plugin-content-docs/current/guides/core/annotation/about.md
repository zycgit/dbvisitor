---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Method Annotations
description: dbVisitor provides some annotations to mark methods on interfaces, acting as a medium for database access, thus avoiding complex JDBC operation code.
---

dbVisitor provides some annotations to mark methods on interfaces, acting as a medium for database access, thus avoiding complex JDBC operation code.

```java title='1. The feature of using Method Annotations is the separation of SQL and calling logic'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id > ?")
    List<User> listUsers(long searchId);
}
```

```java title='2. Create Generic Mapper'
// 1. Create Configuration
Configuration config = new Configuration();

// 2. Create Session
Session session = config.newSession(dataSource);
// OR
Session session = config.newSession(connection);

// 3. Create Mapper
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> result = mapper.listUsers(2);
```

In actual usage, the way to obtain a Session may vary depending on your project architecture. The above code demonstrates a primitive way to create a Session.
You can choose the appropriate way to obtain a Session according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

Relevant Classes
- net.hasor.dbvisitor.mapper.SimpleMapper
- net.hasor.dbvisitor.session.Configuration
- net.hasor.dbvisitor.session.Session

## User Guide

Mapper interfaces can define multiple methods, and database operations can be performed by marking query annotations.
- [@Query](./query), execute a query statement that can return a result set and return the query result.
- [@Insert](./insert), used to execute INSERT statements.
- [@Update](./update), used to execute UPDATE statements.
- [@Delete](./delete), used to execute DELETE statements.
- [@Execute](./execute), can be used to execute any type of statement, such as INSERT, UPDATE, DELETE, or DDL operations.
- [@Call](./call), used to execute stored procedure calls.
- [@Segment](./segment), used to define a SQL segment, which can be referenced by other SQL methods in the same Mapper through macro rules.
- [Rules](../../rules/about), utilize rules to endow SQL with more powerful features in SQL.
