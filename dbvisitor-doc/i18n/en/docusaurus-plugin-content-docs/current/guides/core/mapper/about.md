---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Mapper 接口
description: 通用 BaseMapper 接口提供了一组常见的数据库操作方法利用对象映射信息完成对数据库的操作。
---

通用 BaseMapper 接口提供了一组常见的数据库操作方法利用对象映射信息完成对数据库的操作。

```java title='1. 声明实体类'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    ...
}
```

```java title='2. 创建通用 Mapper'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);
或者
Session session = config.newSession(connection);

// 3，创建 BaseMapper
BaseMapper<User> mapper = session.createBaseMapper(User.class);
```

## 使用指引

BaseMapper 接口提供了若干方法，下面以主要场景作为介绍
- [基础操作](./common)，常见的基础 CRUD 操作和分页查询。
- [调用构造器 API](./lambda)，在 Mapper 上使用条件构造器。
- [执行 Mapper 文件中的 SQL语句](./file)，用于定义一个 SQL 片段，在其所在 Mapper 的其它 SQL 方法中可以通过 macro 宏规则引用该片段。
