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

在实际使用过程中 Session 根据您的项目架构获取方式可能不同，上述代码演示了一种原始的创建 Session 的方式。
你可以根据您的项目架构选择合适的方式获取 Session，详细信息请参考：**[框架整合](../../yourproject/buildtools#integration)**

相关的类
- net.hasor.dbvisitor.mapping.Table
- net.hasor.dbvisitor.mapping.Column
- net.hasor.dbvisitor.session.Configuration
- net.hasor.dbvisitor.session.Session
- net.hasor.dbvisitor.mapper.BaseMapper

## 使用指引

BaseMapper 接口提供了若干方法，下面以主要场景作为介绍
- [基础操作](./common)，常见的基础 CRUD 操作和分页查询。
- [调用构造器 API](./lambda)，在 Mapper 上使用条件构造器。
- [执行 Mapper 文件中的命令](./file)，用于定义一个命令，在其所在 Mapper 接口上可以引用该命令。
